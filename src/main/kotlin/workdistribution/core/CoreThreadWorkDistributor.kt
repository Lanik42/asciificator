package workdistribution.core

import Size

class CoreThreadWorkDistributor(
    private val symbolToPixelAreaRatio: Int,
    imageSize: Size
) {

    private val extraBottomPixels = imageSize.height % symbolToPixelAreaRatio
    private val symbolsPerYDimension = imageSize.height / symbolToPixelAreaRatio

    fun getThreadInputData2DArray(): Array<ThreadInputData?> {
        val threadDataArray = Array<ThreadInputData?>(ThreadManager.threadCount) { null }

        defineThreadWorkDistribution(threadDataArray)

        return threadDataArray
    }

    private fun defineThreadWorkDistribution(threadDataArray: Array<ThreadInputData?>) {
        val lastYAreaIndex = ThreadManager.threadCount - 1
        val threadHeightInSymbols = symbolsPerYDimension / ThreadManager.threadCount

        for (y in 0..lastYAreaIndex) {
            if (y == lastYAreaIndex) {
                handleLastYArea(threadDataArray, lastYAreaIndex)
                continue
            }

            val symbolAreaListByHeight = mutableListOf<Area>()
            for (areaY in 0 until threadHeightInSymbols) {
                symbolAreaListByHeight.add(Area(0, areaY, Size(symbolToPixelAreaRatio, symbolToPixelAreaRatio)))
            }

            threadDataArray[y] = ThreadInputData(
                threadAreaYOffset = y,
                threadSymbolsHeight = threadHeightInSymbols,
                symbolAreaListByHeight = symbolAreaListByHeight,
            )
        }
    }

    /**
     * Этот метод используется для обработки ситуации, когда высота картинки не ровно поделилась на заданный
     * symbolToPixelAreaRatio, и нет возможности поделить всю работу между потоками равномерно
     *
     * Нижние зоны по y берут на себя невошедшие пиксели снизу
     */

    private fun handleLastYArea(threadDataArray: Array<ThreadInputData?>, yIndex: Int) {
        val threadHeightInSymbols = symbolsPerYDimension % (ThreadManager.threadCount - 1)

        val symbolAreaListByHeight = mutableListOf<Area>()
        for (areaY in 0 until threadHeightInSymbols - 1) {
            symbolAreaListByHeight.add(Area(0, areaY, Size(symbolToPixelAreaRatio, symbolToPixelAreaRatio)))
        }
        if (threadHeightInSymbols != 0) {
            symbolAreaListByHeight.add(Area(0, threadHeightInSymbols, Size(symbolToPixelAreaRatio, symbolToPixelAreaRatio + extraBottomPixels)))
        }

        val inputThreadData = ThreadInputData(
            threadAreaYOffset = yIndex,
            threadSymbolsHeight = threadHeightInSymbols,
            symbolAreaListByHeight = symbolAreaListByHeight,
        )
        threadDataArray[yIndex] = inputThreadData
    }
}