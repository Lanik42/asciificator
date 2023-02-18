package workdistribution

import Size

class RowThreadWorkDistributor(
    symbolToPixelAreaRatio: Int,
    imageSize: Size
) : ThreadWorkDistributor(symbolToPixelAreaRatio, imageSize) {

    private companion object {

        const val ZERO_OFFSET = 0
    }

    override fun getThreadInputData2DArray(): Array<Array<ThreadInputData?>> {
        val threadDataArray = Array<ThreadInputData?>(areasPerYDimension) { null }

        defineThreadWorkDistribution(threadDataArray)

        return Array(1) { threadDataArray }
    }

    private fun defineThreadWorkDistribution(threadDataArray: Array<ThreadInputData?>, ) {
        val lastYAreaIndex = areasPerYDimension - 1

        for (y in 0 until areasPerYDimension) {
            if (y == lastYAreaIndex) {
                handleLastYArea(threadDataArray, lastYAreaIndex)
                continue
            }

            val threadWorkAreaSize = Size(imageSize.width, symbolToPixelAreaRatio)
            threadDataArray[y] = ThreadInputData(
                threadWorkAreaSize = threadWorkAreaSize,
                threadWorkAreaXOffset = ZERO_OFFSET,
                threadWorkAreaYOffset = y
            )
        }
    }

    /**
     * Этот метод используется для обработки ситуации, когда высота картинки не ровно поделилась на заданный
     * symbolToPixelAreaRatio, и нет возможности поделить всю работу между потоками равномерно
     *
     * Нижние зоны по y берут на себя невошедшие пиксели снизу
     */

    private fun handleLastYArea(threadDataArray: Array<ThreadInputData?>, yOffset: Int) {
        val size = Size(
            imageSize.width,
            symbolToPixelAreaRatio + extraBottomPixels
        )

        val inputThreadData = ThreadInputData(
            threadWorkAreaSize = size,
            threadWorkAreaXOffset = ZERO_OFFSET,
            threadWorkAreaYOffset = yOffset
        )

        threadDataArray[yOffset] = inputThreadData
    }
}