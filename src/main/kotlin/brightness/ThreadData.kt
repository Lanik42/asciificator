package brightness

import Size

data class ThreadData(
    val threadWorkAreaSize: Size,
    // val colorData: Array<IntArray>,
    val threadWorkAreaXOffset: Int,
    val threadWorkAreaYOffset: Int,
)