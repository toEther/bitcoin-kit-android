package io.definenulls.bitcoincore.transactions

import io.definenulls.bitcoincore.core.IPluginData
import io.definenulls.bitcoincore.managers.BloomFilterManager
import io.definenulls.bitcoincore.models.TransactionDataSortType
import io.definenulls.bitcoincore.storage.FullTransaction
import io.definenulls.bitcoincore.storage.UnspentOutput
import io.definenulls.bitcoincore.transactions.builder.TransactionBuilder

class TransactionCreator(
        private val builder: TransactionBuilder,
        private val processor: PendingTransactionProcessor,
        private val transactionSender: TransactionSender,
        private val bloomFilterManager: BloomFilterManager) {

    @Throws
    fun create(toAddress: String, value: Long, feeRate: Int, senderPay: Boolean, sortType: TransactionDataSortType, pluginData: Map<Byte, IPluginData>): FullTransaction {
        return create {
            builder.buildTransaction(toAddress, value, feeRate, senderPay, sortType, pluginData)
        }
    }

    @Throws
    fun create(unspentOutput: UnspentOutput, toAddress: String, feeRate: Int, sortType: TransactionDataSortType): FullTransaction {
        return create {
            builder.buildTransaction(unspentOutput, toAddress, feeRate, sortType)
        }
    }

    private fun create(transactionBuilderFunction: () -> FullTransaction): FullTransaction {
        transactionSender.canSendTransaction()

        val transaction = transactionBuilderFunction.invoke()

        try {
            processor.processCreated(transaction)
        } catch (ex: BloomFilterManager.BloomFilterExpired) {
            bloomFilterManager.regenerateBloomFilter()
        }

        try {
            transactionSender.sendPendingTransactions()
        } catch (e: Exception) {
            // ignore any exception since the tx is inserted to the db
        }

        return transaction
    }

    open class TransactionCreationException(msg: String) : Exception(msg)
    class TransactionAlreadyExists(msg: String) : TransactionCreationException(msg)

}
