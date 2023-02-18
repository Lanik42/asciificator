package workdistribution

import Size

data class ThreadInputData(
    val threadWorkAreaSize: Size,
    // val colorData: Array<IntArray>,
    val threadWorkAreaXOffset: Int,
    val threadWorkAreaYOffset: Int,
)