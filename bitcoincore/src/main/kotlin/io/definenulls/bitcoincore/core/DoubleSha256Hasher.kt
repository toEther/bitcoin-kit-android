package io.definenulls.bitcoincore.core

import io.definenulls.bitcoincore.utils.HashUtils

class DoubleSha256Hasher : IHasher {
    override fun hash(data: ByteArray): ByteArray {
        return HashUtils.doubleSha256(data)
    }
}
