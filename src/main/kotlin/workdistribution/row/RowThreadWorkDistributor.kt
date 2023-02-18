package workdistribution.row

import Size

class RowThreadWorkDistributor(
    private val symbolToPixelAreaRatio: Int,
    imageSize: Size
) {

    private val extraBottomPixels = imageSize.height % symbolToPixelAreaRatio
    private val symbolsPerYDimension = imageSize.height / symbolToPixelAreaRatio

    fun getThreadInputData2DArray(): Array<RowInputData?> {
        val threadDataArray = Array<RowInputData?>(symbolsPerYDimension) { null }

        defineThreadWorkDistribution(threadDataArray)

        return threadDataArray
    }

    private fun defineThreadWorkDistribution(threadDataArray: Array<RowInputData?>) {
        val lastYAreaIndex = symbolsPerYDimension - 1

        for (y in 0 until symbolsPerYDimension) {
            if (y == lastYAreaIndex) {
                handleLastYArea(threadDataArray, lastYAreaIndex)
                continue
            }

            threadDataArray[y] = RowInputData(
                singleSymbolArea = Size(symbolToPixelAreaRatio, symbolToPixelAreaRatio),
                yOffset = y,
            )
        }
    }

    /**
     * Этот метод используется для обработки ситуации, когда высота картинки не ровно поделилась на заданный
     * symbolToPixelAreaRatio, и нет возможности поделить всю работу между потоками равномерно
     *
     * Нижние зоны по y берут на себя невошедшие пиксели снизу
     */

    private fun handleLastYArea(threadDataArray: Array<RowInputData?>, yOffset: Int) {
        val inputThreadData = RowInputData(
            singleSymbolArea = Size(symbolToPixelAreaRatio, symbolToPixelAreaRatio + extraBottomPixels),
            yOffset = yOffset,
        )

        threadDataArray[yOffset] = inputThreadData
    }
}