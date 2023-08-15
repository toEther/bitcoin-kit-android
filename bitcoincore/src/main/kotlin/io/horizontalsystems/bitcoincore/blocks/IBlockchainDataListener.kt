package io.definenulls.bitcoincore.blocks

import io.definenulls.bitcoincore.models.Block
import io.definenulls.bitcoincore.models.Transaction

interface IBlockchainDataListener {
    fun onBlockInsert(block: Block)
    fun onTransactionsUpdate(inserted: List<Transaction>, updated: List<Transaction>, block: Block?)
    fun onTransactionsDelete(hashes: List<String>)
}
