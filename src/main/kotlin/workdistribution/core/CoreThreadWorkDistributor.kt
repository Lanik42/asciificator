package workdistribution.core

import CustomSize

class CoreThreadWorkDistributor(
    private val symbolToPixelAreaRatio: Int,
    imageSize: CustomSize
) {

    private val extraBottomSymbols = imageSize.height % symbolToPixelAreaRatio
    private val symbolsPerYDimension = imageSize.height / symbolToPixelAreaRatio

    fun getThreadInputData2DArray(): Array<ThreadInputData?> {
        val rowData = Array<ThreadInputData?>(ThreadManager.threadCount) { null }

        defineThreadWorkDistribution(rowData)

        return rowData
    }

    private fun defineThreadWorkDistribution(rowData: Array<ThreadInputData?>) {
        val lastYAreaIndex = ThreadManager.threadCount - 1
        val threadHeightInSymbols = symbolsPerYDimension / ThreadManager.threadCount

        for (y in 0..lastYAreaIndex) {
            if (y == lastYAreaIndex) {
                rowData[y] = ThreadInputData(
                    threadHeightInSymbols = threadHeightInSymbols,
                    symbolSizeInPixels = CustomSize(symbolToPixelAreaRatio, symbolToPixelAreaRatio),
                    lastRowExtraSymbols = symbolsPerYDimension - threadHeightInSymbols * (ThreadManager.threadCount - 1) - threadHeightInSymbols,
                )
                break
            }

            rowData[y] = ThreadInputData(
                threadHeightInSymbols = threadHeightInSymbols,
                symbolSizeInPixels = CustomSize(symbolToPixelAreaRatio, symbolToPixelAreaRatio),
            )
        }
    }

    /**
     * Этот метод используется для обработки ситуации, когда высота картинки не ровно поделилась на заданный
     * symbolToPixelAreaRatio, и нет возможности поделить всю работу между потоками равномерно
     *
     * Нижние зоны по y берут на себя невошедшие символы снизу
     */


    // TODO зарефачить это так, чтобы не последний тред съедал нижние пиксели, а выделялся еще один поток, который бы их обработал
    private fun handleLastYArea(threadDataArray: Array<ThreadInputData?>, yIndex: Int) {
        val threadHeightInSymbols = symbolsPerYDimension / ThreadManager.threadCount

        val inputThreadData = ThreadInputData(
            threadHeightInSymbols = threadHeightInSymbols,
            symbolSizeInPixels = CustomSize(symbolToPixelAreaRatio, symbolToPixelAreaRatio),
            lastRowExtraSymbols = extraBottomSymbols
        )
        threadDataArray[yIndex] = inputThreadData
    }


}