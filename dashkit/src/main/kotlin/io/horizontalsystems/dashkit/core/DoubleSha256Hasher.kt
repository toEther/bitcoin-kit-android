package io.definenulls.dashkit.core

import io.definenulls.bitcoincore.core.IHasher
import io.definenulls.bitcoincore.utils.HashUtils

class SingleSha256Hasher : IHasher {
    override fun hash(data: ByteArray): ByteArray {
        return HashUtils.sha256(data)
    }
}