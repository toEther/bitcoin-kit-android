package io.definenulls.bitcoincore.blocks

import io.definenulls.bitcoincore.network.peer.Peer

interface IPeerSyncListener {
    fun onAllPeersSynced() = Unit
    fun onPeerSynced(peer: Peer) = Unit
}
