package io.definenulls.dashkit.masternodelist

import io.definenulls.bitcoincore.core.IHasher
import io.definenulls.bitcoincore.utils.HashUtils
import io.definenulls.dashkit.IMerkleHasher

class MerkleRootHasher: IHasher, IMerkleHasher {

    override fun hash(data: ByteArray): ByteArray {
        return HashUtils.doubleSha256(data)
    }

    override fun hash(first: ByteArray, second: ByteArray): ByteArray {
        return HashUtils.doubleSha256(first + second)
    }
}
