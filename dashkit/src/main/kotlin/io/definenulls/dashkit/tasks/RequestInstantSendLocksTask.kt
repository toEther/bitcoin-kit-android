package io.definenulls.dashkit.tasks

import io.definenulls.bitcoincore.models.InventoryItem
import io.definenulls.bitcoincore.network.messages.GetDataMessage
import io.definenulls.bitcoincore.network.messages.IMessage
import io.definenulls.bitcoincore.network.peer.task.PeerTask
import io.definenulls.dashkit.InventoryType
import io.definenulls.dashkit.messages.ISLockMessage

class RequestInstantSendLocksTask(hashes: List<ByteArray>) : PeerTask() {

    val hashes = hashes.toMutableList()
    var isLocks = mutableListOf<ISLockMessage>()

    override fun start() {
        requester?.send(GetDataMessage(hashes.map { InventoryItem(InventoryType.MSG_ISLOCK, it) }))
    }

    override fun handleMessage(message: IMessage) = when (message) {
        is ISLockMessage -> handleISLockVote(message)
        else -> false
    }

    private fun handleISLockVote(isLockMessage: ISLockMessage): Boolean {
        val hash = hashes.firstOrNull { it.contentEquals(isLockMessage.hash) } ?: return false

        hashes.remove(hash)
        isLocks.add(isLockMessage)

        if (hashes.isEmpty()) {
            listener?.onTaskCompleted(this)
        }

        return true
    }

}
