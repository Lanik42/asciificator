package workdistribution.area

import Size

data class AreaThreadInputData(
    val threadWorkAreaSize: Size,
    // val colorData: Array<IntArray>,
    val threadWorkAreaXStart: Int,
    val threadWorkAreaYStart: Int,
)