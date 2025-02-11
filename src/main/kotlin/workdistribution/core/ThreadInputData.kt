package workdistribution.core

import CustomSize

data class ThreadInputData(
    val threadHeightInSymbols: Int,
    val symbolSizeInPixels: CustomSize,
    val lastRowExtraSymbols: Int = 0,
)