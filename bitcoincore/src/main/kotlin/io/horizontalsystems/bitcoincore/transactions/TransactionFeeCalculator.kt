package io.definenulls.bitcoincore.transactions

import io.definenulls.bitcoincore.core.IPluginData
import io.definenulls.bitcoincore.core.IPublicKeyManager
import io.definenulls.bitcoincore.core.IRecipientSetter
import io.definenulls.bitcoincore.models.TransactionDataSortType
import io.definenulls.bitcoincore.transactions.builder.InputSetter
import io.definenulls.bitcoincore.transactions.builder.MutableTransaction
import io.definenulls.bitcoincore.transactions.scripts.ScriptType
import io.definenulls.bitcoincore.utils.AddressConverterChain

class TransactionFeeCalculator(
    private val recipientSetter: IRecipientSetter,
    private val inputSetter: InputSetter,
    private val addressConverter: AddressConverterChain,
    private val publicKeyManager: IPublicKeyManager,
    private val changeScriptType: ScriptType
) {

    fun fee(value: Long, feeRate: Int, senderPay: Boolean, toAddress: String?, pluginData: Map<Byte, IPluginData>): Long {
        val mutableTransaction = MutableTransaction()

        recipientSetter.setRecipient(mutableTransaction, toAddress ?: sampleAddress(), value, pluginData, true)
        inputSetter.setInputs(mutableTransaction, feeRate, senderPay, TransactionDataSortType.None)

        val inputsTotalValue = mutableTransaction.inputsToSign.map { it.previousOutput.value }.sum()
        val outputsTotalValue = mutableTransaction.recipientValue + mutableTransaction.changeValue

        return inputsTotalValue - outputsTotalValue
    }

    private fun sampleAddress(): String {
        return addressConverter.convert(publicKey = publicKeyManager.changePublicKey(), scriptType = changeScriptType).stringValue
    }
}
