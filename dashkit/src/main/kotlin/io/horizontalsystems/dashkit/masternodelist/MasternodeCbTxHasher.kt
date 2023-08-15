package io.definenulls.dashkit.masternodelist

import io.definenulls.bitcoincore.core.HashBytes
import io.definenulls.bitcoincore.core.IHasher
import io.definenulls.dashkit.models.CoinbaseTransaction
import io.definenulls.dashkit.models.CoinbaseTransactionSerializer

class MasternodeCbTxHasher(private val coinbaseTransactionSerializer: CoinbaseTransactionSerializer, private val hasher: IHasher) {

    fun hash(coinbaseTransaction: CoinbaseTransaction): HashBytes {
        val serialized = coinbaseTransactionSerializer.serialize(coinbaseTransaction)

        return HashBytes(hasher.hash(serialized))
    }

}
