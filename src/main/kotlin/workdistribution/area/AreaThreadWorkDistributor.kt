package workdistribution.area

import Size
import measureTimeMillis

class AreaThreadWorkDistributor(
    private val symbolToPixelAreaRatio: Int,
    imageSize: Size
) {

    private val extraRightPixels = imageSize.width % symbolToPixelAreaRatio
    private val extraBottomPixels = imageSize.height % symbolToPixelAreaRatio

    private val symbolsPerXDimension = imageSize.width / symbolToPixelAreaRatio
    private val symbolsPerYDimension = imageSize.height / symbolToPixelAreaRatio

    fun getThreadInputData2DArray(): Array<Array<AreaInputData?>> {
        val threadData2DArray = measureTimeMillis("thread data get") {
            val td = Array(symbolsPerYDimension) {
                Array<AreaInputData?>(symbolsPerXDimension) { null }
            }

            defineThreadWorkDistribution(td)
            td
        }

        return threadData2DArray
    }

    private fun defineThreadWorkDistribution(threadData2DArray: Array<Array<AreaInputData?>>) {
        val lastXAreaIndex = symbolsPerXDimension - 1
        val lastYAreaIndex = symbolsPerYDimension - 1

        measureTimeMillis("cycle time") {
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
                    threadData2DArray[y][x] = AreaInputData(
                        areaSize = size,
                        areaXOffset = x,
                        areaYOffset = y
                    )
                }
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

    private fun handleLastYArea(inputThreadDataArray: Array<Array<AreaInputData?>>, xOffset: Int, yOffset: Int) {
        val size = Size(symbolToPixelAreaRatio, extraBottomPixels + symbolToPixelAreaRatio)

        val inputThreadData = AreaInputData(
            areaSize = size,
            areaXOffset = xOffset,
            areaYOffset = yOffset
        )

        inputThreadDataArray[yOffset][xOffset] = inputThreadData
    }

    private fun handleLastXArea(inputThreadDataArray: Array<Array<AreaInputData?>>, xOffset: Int, yOffset: Int) {
        val size = Size(extraRightPixels + symbolToPixelAreaRatio, symbolToPixelAreaRatio)

        val inputThreadData = AreaInputData(
            areaSize = size,
            areaXOffset = xOffset,
            areaYOffset = yOffset
        )

        inputThreadDataArray[yOffset][xOffset] = inputThreadData
    }

    private fun handleLastXYArea(inputThreadDataArray: Array<Array<AreaInputData?>>, xOffset: Int, yOffset: Int) {
        val size = Size(extraRightPixels + symbolToPixelAreaRatio, extraBottomPixels + symbolToPixelAreaRatio)

        val inputThreadData = AreaInputData(
            areaSize = size,
            areaXOffset = xOffset,
            areaYOffset = yOffset
        )

        inputThreadDataArray[yOffset][xOffset] = inputThreadData
    }
}