package io.definenulls.bitcoincore.managers

import io.definenulls.bitcoincore.models.BlockHash
import io.definenulls.bitcoincore.models.PublicKey
import io.reactivex.Single

interface IBlockDiscovery {
    fun discoverBlockHashes(): Single<Pair<List<PublicKey>, List<BlockHash>>>
}
