package workdistribution.core

import Size

data class ThreadInputData(
    val threadAreaYOffset: Int,
    val threadSymbolsHeight: Int,
    val symbolAreaListByHeight: List<Area>,
)

data class Area(
    val x: Int,
    val y: Int,
    val size: Size
)