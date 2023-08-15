package io.definenulls.bitcoincore.blocks.validators

import io.definenulls.bitcoincore.models.Block

class BlockValidatorSet : IBlockValidator {
    private var validators = mutableListOf<IBlockValidator>()

    override fun validate(block: Block, previousBlock: Block) {
        validators.forEach {
            it.validate(block, previousBlock)
        }
    }

    fun addBlockValidator(blockValidator: IBlockValidator) {
        validators.add(blockValidator)
    }

}
