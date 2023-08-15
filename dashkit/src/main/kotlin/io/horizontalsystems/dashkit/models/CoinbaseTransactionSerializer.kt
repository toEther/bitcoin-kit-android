package io.definenulls.dashkit.models

import io.definenulls.bitcoincore.io.BitcoinOutput
import io.definenulls.bitcoincore.serializers.TransactionSerializer

class CoinbaseTransactionSerializer {

    fun serialize(coinbaseTransaction: CoinbaseTransaction): ByteArray {
        val output = BitcoinOutput()

        output.write(TransactionSerializer.serialize(coinbaseTransaction.transaction))
        output.writeVarInt(coinbaseTransaction.coinbaseTransactionSize)
        output.writeUnsignedShort(coinbaseTransaction.version)
        output.writeUnsignedInt(coinbaseTransaction.height)
        output.write(coinbaseTransaction.merkleRootMNList)

        if (coinbaseTransaction.version >= 2) {
            output.write(coinbaseTransaction.merkleRootQuorums)
        }

        return output.toByteArray()
    }

}
