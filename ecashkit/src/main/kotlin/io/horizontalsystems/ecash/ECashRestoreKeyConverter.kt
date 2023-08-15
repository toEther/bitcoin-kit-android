package io.definenulls.ecash

import io.definenulls.bitcoincore.extensions.toHexString
import io.definenulls.bitcoincore.managers.IRestoreKeyConverter
import io.definenulls.bitcoincore.models.PublicKey

class ECashRestoreKeyConverter: IRestoreKeyConverter {
    override fun keysForApiRestore(publicKey: PublicKey): List<String> {
        return listOf(publicKey.publicKeyHash.toHexString())
    }

    override fun bloomFilterElements(publicKey: PublicKey): List<ByteArray> {
        return listOf(publicKey.publicKeyHash, publicKey.publicKey)
    }
}
