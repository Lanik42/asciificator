package workdistribution.core

import CustomSize

data class ThreadInputData(
    val threadHeightInSymbols: Int,
    val symbolSizeInPixels: CustomSize,
    val lastRowExtraSymbols: Int = 0,
)

data class Area(
    val y: Int,
    val size: CustomSize
)