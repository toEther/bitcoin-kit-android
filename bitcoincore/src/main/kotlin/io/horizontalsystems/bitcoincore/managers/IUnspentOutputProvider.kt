package io.definenulls.bitcoincore.managers

import io.definenulls.bitcoincore.storage.UnspentOutput

interface IUnspentOutputProvider {
    fun getSpendableUtxo(): List<UnspentOutput>
}
