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
}