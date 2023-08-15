package io.definenulls.bitcoincore.core

interface IHasher {
    fun hash(data: ByteArray) : ByteArray
}
