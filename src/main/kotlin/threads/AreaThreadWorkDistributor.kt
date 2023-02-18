package threads

import Size
import brightness.ThreadData

class AreaThreadWorkDistributor(
    symbolToPixelAreaRatio: Int,
    imageSize: Size
): ThreadWorkDistributor(symbolToPixelAreaRatio, imageSize) {

    override fun getThreadData2DArray(): Array<Array<ThreadData?>> {
        val threadData2DArray = Array(areasPerXDimension) {
            Array<ThreadData?>(areasPerYDimension) { null }
        }

        defineThreadWorkDistribution(threadData2DArray, areasPerXDimension, areasPerYDimension)

        return threadData2DArray
    }

    private fun defineThreadWorkDistribution(
        threadData2DArray: Array<Array<ThreadData?>>,
        areasPerXDimension: Int,
        areasPerYDimension: Int
    ) {
        val lastXAreaIndex = areasPerXDimension - 1
        val lastYAreaIndex = areasPerYDimension - 1

        for (x in 0 until areasPerXDimension) {
            for (y in 0 until areasPerYDimension) {
                if (y == lastYAreaIndex && x == lastXAreaIndex) {
                    handleLastXYArea(threadData2DArray, lastXAreaIndex, lastYAreaIndex)
                    break
                }

                if (y == lastYAreaIndex) {
                    handleLastYArea(threadData2DArray, x, lastYAreaIndex)
                    continue
                }

                if (x == lastXAreaIndex) {
                    handleLastXArea(threadData2DArray, lastXAreaIndex, y)
                    continue
                }

                val size = Size(symbolToPixelAreaRatio, symbolToPixelAreaRatio)
                threadData2DArray[x][y] = ThreadData(
                    threadWorkAreaSize = size,
                    threadWorkAreaXOffset = x,
                    threadWorkAreaYOffset = y
                )
            }
        }
    }

    /**
     * Эти методы используются для обработки ситуации, когда размер картинки не ровно поделился на заданный
     * symbolToPixelAreaRatio, и нет возможности поделить всю работу между потоками равномерно
     *
     * Чтобы просчитать всю картинку целиком:
     * Самые правые зоны по x берут на себя невошедшие пиксели справа
     * Самые нижние зоны по y берут на себя невошедшие пиксели снизу
     */

    private fun handleLastYArea(inputThreadDataArray: Array<Array<ThreadData?>>, xOffset: Int, yOffset: Int) {
        val size = Size(symbolToPixelAreaRatio, extraBottomPixels + symbolToPixelAreaRatio)

        val inputThreadData = ThreadData(
            threadWorkAreaSize = size,
            threadWorkAreaXOffset = xOffset,
            threadWorkAreaYOffset = yOffset
        )

        inputThreadDataArray[xOffset][yOffset] = inputThreadData
    }

    private fun handleLastXArea(inputThreadDataArray: Array<Array<ThreadData?>>, xOffset: Int, yOffset: Int) {
        val size = Size(extraRightPixels + symbolToPixelAreaRatio, symbolToPixelAreaRatio)

        val inputThreadData = ThreadData(
            threadWorkAreaSize = size,
            threadWorkAreaXOffset = xOffset,
            threadWorkAreaYOffset = yOffset
        )

        inputThreadDataArray[xOffset][yOffset] = inputThreadData
    }

    private fun handleLastXYArea(inputThreadDataArray: Array<Array<ThreadData?>>, xOffset: Int, yOffset: Int) {
        val size = Size(extraRightPixels + symbolToPixelAreaRatio, extraBottomPixels + symbolToPixelAreaRatio)

        val inputThreadData = ThreadData(
            threadWorkAreaSize = size,
            threadWorkAreaXOffset = xOffset,
            threadWorkAreaYOffset = yOffset
        )

        inputThreadDataArray[xOffset][yOffset] = inputThreadData
    }
}