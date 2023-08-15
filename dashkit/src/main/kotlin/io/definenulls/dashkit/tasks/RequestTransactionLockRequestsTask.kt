package io.definenulls.dashkit.tasks

import io.definenulls.bitcoincore.models.InventoryItem
import io.definenulls.bitcoincore.network.messages.GetDataMessage
import io.definenulls.bitcoincore.network.messages.IMessage
import io.definenulls.bitcoincore.network.peer.task.PeerTask
import io.definenulls.bitcoincore.storage.FullTransaction
import io.definenulls.dashkit.InventoryType
import io.definenulls.dashkit.messages.TransactionLockMessage

class RequestTransactionLockRequestsTask(hashes: List<ByteArray>) : PeerTask() {

    val hashes = hashes.toMutableList()
    var transactions = mutableListOf<FullTransaction>()

    override fun start() {
        val items = hashes.map { hash ->
            InventoryItem(InventoryType.MSG_TXLOCK_REQUEST, hash)
        }

        requester?.send(GetDataMessage(items))
    }

    override fun handleMessage(message: IMessage) = when (message) {
        is TransactionLockMessage -> handleTransactionLockRequest(message.transaction)
        else -> false
    }

    private fun handleTransactionLockRequest(transaction: FullTransaction): Boolean {
        val hash = hashes.firstOrNull { it.contentEquals(transaction.header.hash) } ?: return false

        hashes.remove(hash)
        transactions.add(transaction)

        if (hashes.isEmpty()) {
            listener?.onTaskCompleted(this)
        }

        return true
    }

}
