package io.definenulls.bitcoincore.network.messages

import io.definenulls.bitcoincore.io.BitcoinInputMarkable
import io.definenulls.bitcoincore.models.NetworkAddress

class AddrMessage(var addresses: List<NetworkAddress>) : IMessage {
    override fun toString(): String {
        return "AddrMessage(count=${addresses.size})"
    }
}

class AddrMessageParser : IMessageParser {
    override val command = "addr"

    override fun parseMessage(input: BitcoinInputMarkable): IMessage {
        val count = input.readVarInt() // do not store count

        val addresses = List(count.toInt()) {
            NetworkAddress(input, false)
        }

        return AddrMessage(addresses)
    }
}
