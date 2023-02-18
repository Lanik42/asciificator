package workdistribution.area

import Size
import workdistribution.ThreadWorkDistributor

class AreaThreadWorkDistributor(
    symbolToPixelAreaRatio: Int,
    imageSize: Size
): ThreadWorkDistributor(symbolToPixelAreaRatio, imageSize) {

    override fun getThreadInputData2DArray(): Array<Array<AreaThreadInputData?>> {
        val threadData2DArray = Array(symbolsPerYDimension) {
            Array<AreaThreadInputData?>(symbolsPerXDimension) { null }
        }

        defineThreadWorkDistribution(threadData2DArray)

        return threadData2DArray
    }

    private fun defineThreadWorkDistribution(threadData2DArray: Array<Array<AreaThreadInputData?>>, ) {
        val lastXAreaIndex = symbolsPerXDimension - 1
        val lastYAreaIndex = symbolsPerYDimension - 1

        for (y in 0 until symbolsPerYDimension) {
            for (x in 0 until symbolsPerXDimension) {
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
                threadData2DArray[y][x] = AreaThreadInputData(
                    threadWorkAreaSize = size,
                    threadWorkAreaXStart = x,
                    threadWorkAreaYStart = y
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

    private fun handleLastYArea(inputThreadDataArray: Array<Array<AreaThreadInputData?>>, xOffset: Int, yOffset: Int) {
        val size = Size(symbolToPixelAreaRatio, extraBottomPixels + symbolToPixelAreaRatio)

        val inputThreadData = AreaThreadInputData(
            threadWorkAreaSize = size,
            threadWorkAreaXStart = xOffset,
            threadWorkAreaYStart = yOffset
        )

        inputThreadDataArray[yOffset][xOffset] = inputThreadData
    }

    private fun handleLastXArea(inputThreadDataArray: Array<Array<AreaThreadInputData?>>, xOffset: Int, yOffset: Int) {
        val size = Size(extraRightPixels + symbolToPixelAreaRatio, symbolToPixelAreaRatio)

        val inputThreadData = AreaThreadInputData(
            threadWorkAreaSize = size,
            threadWorkAreaXStart = xOffset,
            threadWorkAreaYStart = yOffset
        )

        inputThreadDataArray[yOffset][xOffset] = inputThreadData
    }

    private fun handleLastXYArea(inputThreadDataArray: Array<Array<AreaThreadInputData?>>, xOffset: Int, yOffset: Int) {
        val size = Size(extraRightPixels + symbolToPixelAreaRatio, extraBottomPixels + symbolToPixelAreaRatio)

        val inputThreadData = AreaThreadInputData(
            threadWorkAreaSize = size,
            threadWorkAreaXStart = xOffset,
            threadWorkAreaYStart = yOffset
        )

        inputThreadDataArray[yOffset][xOffset] = inputThreadData
    }
}