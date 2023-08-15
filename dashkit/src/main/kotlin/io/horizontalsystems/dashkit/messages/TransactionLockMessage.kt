package io.definenulls.dashkit.messages

import io.definenulls.bitcoincore.extensions.toReversedHex
import io.definenulls.bitcoincore.io.BitcoinInputMarkable
import io.definenulls.bitcoincore.network.messages.IMessage
import io.definenulls.bitcoincore.network.messages.IMessageParser
import io.definenulls.bitcoincore.serializers.TransactionSerializer
import io.definenulls.bitcoincore.storage.FullTransaction

class TransactionLockMessage(var transaction: FullTransaction) : IMessage {
    override fun toString(): String {
        return "TransactionLockMessage(${transaction.header.hash.toReversedHex()})"
    }
}

class TransactionLockMessageParser : IMessageParser {
    override val command: String = "ix"

    override fun parseMessage(input: BitcoinInputMarkable): IMessage {
        val transaction = TransactionSerializer.deserialize(input)
        return TransactionLockMessage(transaction)
    }
}
