package io.horizontalsystems.bitcoinkit.network

import io.horizontalsystems.bitcoinkit.crypto.BloomFilter
import io.horizontalsystems.bitcoinkit.messages.*
import io.horizontalsystems.bitcoinkit.models.InventoryItem
import io.horizontalsystems.bitcoinkit.models.MerkleBlock
import io.horizontalsystems.bitcoinkit.models.Transaction
import io.horizontalsystems.bitcoinkit.network.PeerTask.IPeerTaskDelegate
import io.horizontalsystems.bitcoinkit.network.PeerTask.IPeerTaskRequester
import io.horizontalsystems.bitcoinkit.network.PeerTask.PeerTask
import java.util.logging.Logger

class Peer(val host: String, private val network: NetworkParameters, private val listener: Listener) : PeerConnection.Listener, IPeerTaskDelegate, IPeerTaskRequester {

    private val logger = Logger.getLogger("Peer")

    interface Listener {
        fun connected(peer: Peer)
        fun disconnected(peer: Peer, e: Exception?)
        fun onReady(peer: Peer)
        fun onReceiveInventoryItems(peer: Peer, inventoryItems: List<InventoryItem>)
        fun onTaskCompleted(peer: Peer, task: PeerTask)
        fun handleMerkleBlock(peer: Peer, merkleBlock: MerkleBlock)
        fun onReceiveBestBlockHeight(peer: Peer, lastBlockHeight: Int)
    }

    private val peerConnection = PeerConnection(host, network, this)
    private var tasks = mutableListOf<PeerTask>()

    var connected = false
    var synced = false
    var blockHashesSynced = false

    val ready: Boolean
        get() = connected && tasks.isEmpty()

    fun start() {
        peerConnection.start()
    }

    fun close(disconnectError: Exception? = null) {
        peerConnection.close(disconnectError)
    }

    override fun onMessage(message: Message) {
        when (message) {
            is PingMessage -> peerConnection.sendMessage(PongMessage(message.nonce))
            is PongMessage -> handlePongMessage(message)
            is VersionMessage -> {
                val reason = reasonToClosePeer(message)
                if (reason.isEmpty()) {
                    peerConnection.sendMessage(VerAckMessage())
                    listener.onReceiveBestBlockHeight(this, message.lastBlock)
                } else {
                    //close with reason
                    logger.info("Closing Peer with reason: $reason")
                    close()
                }
            }
            is VerAckMessage -> handleVerackMessage()
            is MerkleBlockMessage -> handleMerkleBlockMessage(message)
            is TransactionMessage -> handleTransactionMessage(message)
            is InvMessage -> handleInvMessage(message)
            is GetDataMessage -> {
                for (inv in message.inventory) {
                    if (tasks.any { it.handleGetDataInventoryItem(inv) }) {
                        continue
                    }
                }
            }
        }
    }

    private fun handleVerackMessage() {
        connected = true

        listener.connected(this)
    }

    private fun reasonToClosePeer(message: VersionMessage): String {
        var reason = ""
        if (message.lastBlock <= 0) {
            reason = "Peer last block is not greater than 0."
        } else if (!message.hasBlockChain(network)) {
            reason = "Peer does not have a copy of the block chain."
        } else if (!message.supportsBloomFilter(network)) {
            reason = "Peer does not support Bloom Filter."
        }
        return reason
    }

    fun filterLoad(bloomFilter: BloomFilter) {
        peerConnection.sendMessage(FilterLoadMessage(bloomFilter))
    }

    fun sendMempoolMessage() {
        peerConnection.sendMessage(MempoolMessage())
    }

    fun addTask(task: PeerTask) {
        tasks.add(task)

        task.delegate = this
        task.requester = this

        task.start()
    }

    override fun disconnected(e: Exception?) {
        connected = false
        listener.disconnected(this, e)
    }

    override fun handleMerkleBlock(merkleBlock: MerkleBlock) {
        listener.handleMerkleBlock(this, merkleBlock)
    }

    override fun onTaskCompleted(task: PeerTask) {
        tasks.firstOrNull { it == task }?.let { completedTask ->
            tasks.remove(completedTask)
            listener.onTaskCompleted(this, task)
        }

        if (tasks.isEmpty()) {
            listener.onReady(this)
        }
    }

    override fun getBlocks(hashes: List<ByteArray>) {
        peerConnection.sendMessage(GetBlocksMessage(hashes, network))
    }

    override fun getData(items: List<InventoryItem>) {
        peerConnection.sendMessage(GetDataMessage(items))
    }

    override fun ping(nonce: Long) {
        peerConnection.sendMessage(PingMessage(nonce))
    }

    override fun sendTransactionInventory(hash: ByteArray) {
        peerConnection.sendMessage(InvMessage(InventoryItem.MSG_TX, hash))
    }

    override fun send(transaction: Transaction) {
        peerConnection.sendMessage(TransactionMessage(transaction))
    }

    fun handleRelayedTransaction(hash: ByteArray): Boolean {
        return tasks.any { it.handleRelayedTransaction(hash) }
    }

    fun isRequestingInventory(hash: ByteArray): Boolean {
        return tasks.any { it.isRequestingInventory(hash) }
    }

    private fun handleInvMessage(message: InvMessage) {
        for (task in tasks) {
            if (task.handleInventoryItems(message.inventory)) {
                return
            }
        }

        listener.onReceiveInventoryItems(this, message.inventory)
    }

    private fun handleMerkleBlockMessage(message: MerkleBlockMessage) {
        for (task in tasks) {
            if (task.handleMerkleBlock(message.merkleBlock)) {
                return
            }
        }
    }

    private fun handleTransactionMessage(message: TransactionMessage) {
        for (task in tasks) {
            if (task.handleTransaction(message.transaction)) {
                return
            }
        }
    }

    private fun handlePongMessage(message: PongMessage) {
        for (task in tasks) {
            if (task.handlePong(message.nonce)) {
                return
            }
        }
    }

}