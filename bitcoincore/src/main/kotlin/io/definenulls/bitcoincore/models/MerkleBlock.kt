package io.definenulls.bitcoincore.models

import io.definenulls.bitcoincore.core.HashBytes
import io.definenulls.bitcoincore.storage.BlockHeader
import io.definenulls.bitcoincore.storage.FullTransaction

class MerkleBlock(val header: BlockHeader, val associatedTransactionHashes: Map<HashBytes, Boolean>) {

    var height: Int? = null
    var associatedTransactions = mutableListOf<FullTransaction>()
    val blockHash = header.hash

    val complete: Boolean
        get() = associatedTransactionHashes.size == associatedTransactions.size

}
