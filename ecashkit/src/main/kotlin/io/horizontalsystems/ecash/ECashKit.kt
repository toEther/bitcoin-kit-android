package io.definenulls.ecash

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import io.definenulls.bitcoincash.blocks.BitcoinCashBlockValidatorHelper
import io.definenulls.bitcoincash.blocks.validators.AsertValidator
import io.definenulls.bitcoincash.blocks.validators.DAAValidator
import io.definenulls.bitcoincash.blocks.validators.EDAValidator
import io.definenulls.bitcoincash.blocks.validators.ForkValidator
import io.definenulls.bitcoincore.AbstractKit
import io.definenulls.bitcoincore.BitcoinCore
import io.definenulls.bitcoincore.BitcoinCore.SyncMode
import io.definenulls.bitcoincore.BitcoinCoreBuilder
import io.definenulls.bitcoincore.blocks.BlockMedianTimeHelper
import io.definenulls.bitcoincore.blocks.validators.BlockValidatorChain
import io.definenulls.bitcoincore.blocks.validators.BlockValidatorSet
import io.definenulls.bitcoincore.blocks.validators.LegacyDifficultyAdjustmentValidator
import io.definenulls.bitcoincore.blocks.validators.ProofOfWorkValidator
import io.definenulls.bitcoincore.core.IInitialSyncApi
import io.definenulls.bitcoincore.extensions.toReversedByteArray
import io.definenulls.bitcoincore.managers.Bip44RestoreKeyConverter
import io.definenulls.bitcoincore.managers.KeyHashRestoreKeyConverter
import io.definenulls.bitcoincore.network.Network
import io.definenulls.bitcoincore.storage.CoreDatabase
import io.definenulls.bitcoincore.storage.Storage
import io.definenulls.bitcoincore.utils.Base58AddressConverter
import io.definenulls.bitcoincore.utils.CashAddressConverter
import io.definenulls.bitcoincore.utils.PaymentAddressParser
import io.definenulls.hdwalletkit.HDExtendedKey
import io.definenulls.hdwalletkit.HDWallet.Purpose
import io.definenulls.hdwalletkit.Mnemonic

class ECashKit : AbstractKit {
    enum class NetworkType {
        MainNet, TestNet

    }

    interface Listener : BitcoinCore.Listener

    override var bitcoinCore: BitcoinCore
    override var network: Network

    var listener: Listener? = null
        set(value) {
            field = value
            bitcoinCore.listener = value
        }

    constructor(
        context: Context,
        words: List<String>,
        passphrase: String,
        walletId: String,
        networkType: NetworkType = NetworkType.MainNet,
        peerSize: Int = 10,
        syncMode: SyncMode = SyncMode.Api(),
        confirmationsThreshold: Int = 1
    ) : this(context, Mnemonic().toSeed(words, passphrase), walletId, networkType, peerSize, syncMode, confirmationsThreshold)

    constructor(
        context: Context,
        seed: ByteArray,
        walletId: String,
        networkType: NetworkType = NetworkType.MainNet,
        peerSize: Int = 10,
        syncMode: SyncMode = SyncMode.Api(),
        confirmationsThreshold: Int = 1
    ) : this(context, HDExtendedKey(seed, Purpose.BIP44), walletId, networkType, peerSize, syncMode, confirmationsThreshold)

    /**
     * @constructor Creates and initializes the BitcoinKit
     * @param context The Android context
     * @param extendedKey HDExtendedKey that contains HDKey and version
     * @param walletId an arbitrary ID of type String.
     * @param networkType The network type. The default is MainNet.
     * @param peerSize The # of peer-nodes required. The default is 10 peers.
     * @param syncMode How the kit syncs with the blockchain. The default is SyncMode.Api().
     * @param confirmationsThreshold How many confirmations required to be considered confirmed. The default is 1 confirmation.
     */
    constructor(
        context: Context,
        extendedKey: HDExtendedKey,
        walletId: String,
        networkType: NetworkType,
        peerSize: Int = 10,
        syncMode: SyncMode = SyncMode.Api(),
        confirmationsThreshold: Int = 1
    ) {
        val database = CoreDatabase.getInstance(context, getDatabaseName(networkType, walletId, syncMode))
        val storage = Storage(database)
        val initialSyncApi: IInitialSyncApi

        network = when (networkType) {
            NetworkType.MainNet -> {
                initialSyncApi = ChronikApi()
                MainNetECash()
            }
            NetworkType.TestNet -> {
                TODO()
            }
        }

        val paymentAddressParser = PaymentAddressParser("bitcoincash", removeScheme = false)

        val blockValidatorSet = BlockValidatorSet()
        blockValidatorSet.addBlockValidator(ProofOfWorkValidator())

        val blockValidatorChain = BlockValidatorChain()
        if (networkType == NetworkType.MainNet) {
            val blockHelper = BitcoinCashBlockValidatorHelper(storage)

            val daaValidator = DAAValidator(targetSpacing, blockHelper)
            val asertValidator = AsertValidator()

            blockValidatorChain.add(ForkValidator(bchnChainForkHeight, bchaChainForkBlockHash, asertValidator))
            blockValidatorChain.add(asertValidator)

            blockValidatorChain.add(ForkValidator(svForkHeight, abcForkBlockHash, daaValidator))
            blockValidatorChain.add(daaValidator)

            blockValidatorChain.add(LegacyDifficultyAdjustmentValidator(blockHelper, heightInterval, targetTimespan, maxTargetBits))
            blockValidatorChain.add(EDAValidator(maxTargetBits, blockHelper, BlockMedianTimeHelper(storage)))
        }

        blockValidatorSet.addBlockValidator(blockValidatorChain)

        bitcoinCore = BitcoinCoreBuilder()
            .setContext(context)
            .setExtendedKey(extendedKey)
            .setPurpose(Purpose.BIP44)
            .setNetwork(network)
            .setPaymentAddressParser(paymentAddressParser)
            .setPeerSize(peerSize)
            .setSyncMode(syncMode)
            .setConfirmationThreshold(confirmationsThreshold)
            .setStorage(storage)
            .setInitialSyncApi(initialSyncApi)
            .setBlockValidator(blockValidatorSet)
            .build()

        //  extending bitcoinCore

        val bech32 = CashAddressConverter(network.addressSegwitHrp)
        bitcoinCore.prependAddressConverter(bech32)

        bitcoinCore.addRestoreKeyConverter(ECashRestoreKeyConverter())
    }

    companion object {
        const val maxTargetBits: Long = 0x1d00ffff              // Maximum difficulty
        const val targetSpacing = 10 * 60                       // 10 minutes per block.
        const val targetTimespan: Long = 14 * 24 * 60 * 60      // 2 weeks per difficulty cycle, on average.
        var heightInterval = targetTimespan / targetSpacing     // 2016 blocks

        const val svForkHeight = 556767                         // 2018 November 14
        const val bchnChainForkHeight = 661648                  // 2020 November 15, 14:13 GMT

        val abcForkBlockHash = "0000000000000000004626ff6e3b936941d341c5932ece4357eeccac44e6d56c".toReversedByteArray()
        val bchaChainForkBlockHash = "000000000000000004284c9d8b2c8ff731efeaec6be50729bdc9bd07f910757d".toReversedByteArray()

        private fun getDatabaseName(networkType: NetworkType, walletId: String, syncMode: SyncMode): String =
            "ECash-${networkType.name}-$walletId-${syncMode.javaClass.simpleName}"

        fun clear(context: Context, networkType: NetworkType, walletId: String) {
            for (syncMode in listOf(SyncMode.Api(), SyncMode.Full(), SyncMode.NewWallet())) {
                try {
                    SQLiteDatabase.deleteDatabase(context.getDatabasePath(getDatabaseName(networkType, walletId, syncMode)))
                } catch (ex: Exception) {
                    continue
                }
            }
        }
    }
}
