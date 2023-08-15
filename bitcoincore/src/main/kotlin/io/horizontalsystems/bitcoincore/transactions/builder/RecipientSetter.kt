package io.definenulls.bitcoincore.transactions.builder

import io.definenulls.bitcoincore.core.IPluginData
import io.definenulls.bitcoincore.core.IRecipientSetter
import io.definenulls.bitcoincore.core.PluginManager
import io.definenulls.bitcoincore.transactions.builder.MutableTransaction
import io.definenulls.bitcoincore.utils.IAddressConverter

class RecipientSetter(
        private val addressConverter: IAddressConverter,
        private val pluginManager: PluginManager
) : IRecipientSetter {

    override fun setRecipient(mutableTransaction: MutableTransaction, toAddress: String, value: Long, pluginData: Map<Byte, IPluginData>, skipChecking: Boolean) {
        mutableTransaction.recipientAddress = addressConverter.convert(toAddress)
        mutableTransaction.recipientValue = value

        pluginManager.processOutputs(mutableTransaction, pluginData, skipChecking)
    }

}
