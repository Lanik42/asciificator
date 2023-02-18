package threads

import Size
import brightness.ThreadData

class ColumnThreadWorkDistributor(
    symbolToPixelAreaRatio: Int,
    imageSize: Size
) : ThreadWorkDistributor(symbolToPixelAreaRatio, imageSize) {

    private companion object {

        const val ZERO_OFFSET = 0
    }

    override fun getThreadData2DArray(): Array<Array<ThreadData?>> {
        val threadDataArray = Array<ThreadData?>(areasPerYDimension) { null }

        defineThreadWorkDistribution(threadDataArray)

        return Array(1) { threadDataArray }
    }

    private fun defineThreadWorkDistribution(threadDataArray: Array<ThreadData?>, ) {
        val lastYAreaIndex = areasPerYDimension - 1

        for (y in 0 until areasPerYDimension) {
            if (y == lastYAreaIndex) {
                handleLastYArea(threadDataArray, lastYAreaIndex)
                continue
            }

            val threadWorkAreaSize = Size(imageSize.width, symbolToPixelAreaRatio)
            threadDataArray[y] = ThreadData(
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

    private fun handleLastYArea(threadDataArray: Array<ThreadData?>, yOffset: Int) {
        val size = Size(
            imageSize.width,
            symbolToPixelAreaRatio + extraBottomPixels
        )

        val inputThreadData = ThreadData(
            threadWorkAreaSize = size,
            threadWorkAreaXOffset = ZERO_OFFSET,
            threadWorkAreaYOffset = yOffset
        )

        threadDataArray[yOffset] = inputThreadData
    }
}