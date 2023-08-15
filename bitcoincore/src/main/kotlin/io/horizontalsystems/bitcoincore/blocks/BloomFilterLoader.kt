package io.definenulls.bitcoincore.blocks

import io.definenulls.bitcoincore.crypto.BloomFilter
import io.definenulls.bitcoincore.managers.BloomFilterManager
import io.definenulls.bitcoincore.network.peer.Peer
import io.definenulls.bitcoincore.network.peer.PeerGroup
import io.definenulls.bitcoincore.network.peer.PeerManager

class BloomFilterLoader(private val bloomFilterManager: BloomFilterManager, private val peerManager: PeerManager)
    : PeerGroup.Listener, BloomFilterManager.Listener {

    override fun onPeerConnect(peer: Peer) {
        bloomFilterManager.bloomFilter?.let {
            peer.filterLoad(it)
        }
    }

    override fun onFilterUpdated(bloomFilter: BloomFilter) {
        peerManager.connected().forEach {
            it.filterLoad(bloomFilter)
        }
    }
}
