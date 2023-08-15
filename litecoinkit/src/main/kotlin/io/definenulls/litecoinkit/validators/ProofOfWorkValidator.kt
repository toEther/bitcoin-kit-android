package io.definenulls.litecoinkit.validators

import io.definenulls.bitcoincore.blocks.validators.BlockValidatorException
import io.definenulls.bitcoincore.blocks.validators.IBlockChainedValidator
import io.definenulls.bitcoincore.crypto.CompactBits
import io.definenulls.bitcoincore.extensions.toHexString
import io.definenulls.bitcoincore.io.BitcoinOutput
import io.definenulls.bitcoincore.models.Block
import io.definenulls.litecoinkit.ScryptHasher
import java.math.BigInteger

class ProofOfWorkValidator(private val scryptHasher: ScryptHasher) : IBlockChainedValidator {

    override fun validate(block: Block, previousBlock: Block) {
        val blockHeaderData = getSerializedBlockHeader(block)

        val powHash = scryptHasher.hash(blockHeaderData).toHexString()

        check(BigInteger(powHash, 16) < CompactBits.decode(block.bits)) {
            throw BlockValidatorException.InvalidProofOfWork()
        }
    }

    private fun getSerializedBlockHeader(block: Block): ByteArray {
        return BitcoinOutput()
                .writeInt(block.version)
                .write(block.previousBlockHash)
                .write(block.merkleRoot)
                .writeUnsignedInt(block.timestamp)
                .writeUnsignedInt(block.bits)
                .writeUnsignedInt(block.nonce)
                .toByteArray()
    }

    override fun isBlockValidatable(block: Block, previousBlock: Block): Boolean {
        return true
    }

}
