package io.definenulls.bitcoincore.core

import io.definenulls.bitcoincore.models.TransactionDataSortType
import io.definenulls.bitcoincore.utils.Bip69Sorter
import io.definenulls.bitcoincore.utils.ShuffleSorter
import io.definenulls.bitcoincore.utils.StraightSorter

class TransactionDataSorterFactory : ITransactionDataSorterFactory {
    override fun sorter(type: TransactionDataSortType): ITransactionDataSorter {
        return when (type) {
            TransactionDataSortType.None -> StraightSorter()
            TransactionDataSortType.Shuffle -> ShuffleSorter()
            TransactionDataSortType.Bip69 -> Bip69Sorter()
        }
    }
}
