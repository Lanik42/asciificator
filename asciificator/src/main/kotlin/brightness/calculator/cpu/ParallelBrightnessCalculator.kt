package brightness.calculator.cpu

import CustomSize
import brightness.CustomColor
import brightness.calculator.CalculatorBench
import brightness.calculator.IBrightnessCalculator
import measureTimeNanos
import workdistribution.core.ThreadInputData
import workdistribution.core.ThreadManager
import workdistribution.core.ThreadWorkDistributor
import java.awt.image.BufferedImage
import java.util.concurrent.Future

class ParallelBrightnessCalculator(
    private val imageSize: CustomSize,
    private val symbolToPixelAreaRatio: Int,
): IBrightnessCalculator {

    private var bufferedImage: BufferedImage? = null

    private val symbolsPerXDimension = imageSize.width / symbolToPixelAreaRatio
    private val symbolsPerYDimension = imageSize.height / symbolToPixelAreaRatio

    override fun calculateBrightness(
        image: BufferedImage,
        bench: CalculatorBench
    ): Array<Array<CustomColor>> {
        bench.frameCount++

        bufferedImage = image

        val brightnessList = measureTimeNanos {
            val threadDataArray = ThreadWorkDistributor(
                symbolToPixelAreaRatio,
                imageSize
            ).getThreadInputData2DArray()

            getBrightness(threadDataArray, bench)
        }

        bench.overallPixelProcessTime += brightnessList.second
        bufferedImage = null
        return brightnessList.first
    }

    private fun getBrightness(
        threadDataArray: Array<ThreadInputData?>,
        bench: CalculatorBench
    ): Array<Array<CustomColor>> {
        val futureArray = Array<Future<Array<Array<CustomColor>?>>?>(ThreadManager.threadCount) { null }
        val colorArray = Array<Array<CustomColor>?>(symbolsPerYDimension) { null }
        val benchArray = Array(ThreadManager.threadCount) { CalculatorBench() }

        threadDataArray.forEachIndexed { index, it ->
            ThreadManager.nexecutors.submit<Array<Array<CustomColor>?>> {
                val threadOffsetInSymbols = if (index == 0) {
                    0
                } else {
                    threadDataArray[index - 1]!!.threadHeightInSymbols * index
                }

                getBrightnessByThread(it, threadOffsetInSymbols, benchArray[index])
            }.also { futureArray[index] = it }
        }

        futureArray.forEachIndexed { offset, future ->
            future?.get()?.forEachIndexed { index, value ->
                colorArray[offset * threadDataArray[0]!!.threadHeightInSymbols + index] = value
            }
        }
        bench.brightnessCalcTime += benchArray.maxBy { it.brightnessCalcTime }.brightnessCalcTime
        bench.fetchRgbTime += benchArray.maxBy { it.fetchRgbTime }.fetchRgbTime

        return colorArray.requireNoNulls()
    }

    private fun getBrightnessByThread(
        threadData: ThreadInputData?,
        threadOffsetInSymbols: Int,
        bench: CalculatorBench,
    ): Array<Array<CustomColor>?> {
        requireNotNull(threadData)

        val threadYPositionOffsetPixels = threadOffsetInSymbols * symbolToPixelAreaRatio
        val symbolSize = threadData.symbolSizeInPixels
        val threadPixelSize = CustomSize(
            height = symbolSize.height * (threadData.threadHeightInSymbols + threadData.lastRowExtraSymbols),
            width = bufferedImage!!.width
        )

        return getBrightness(threadYPositionOffsetPixels, threadPixelSize, threadData, bench)
    }

    private fun getBrightness(
        yOffset: Int,
        threadPixelSize: CustomSize,
        threadData: ThreadInputData,
        bench: CalculatorBench,
    ): Array<Array<CustomColor>?> {
        val rgb2DArray = measureTimeNanos {
            getRgbData(yOffset = yOffset, size = threadPixelSize)
        }
        bench.fetchRgbTime += rgb2DArray.second

        val brightness = measureTimeNanos("calculate brightness") {
            rgb2DArray.first.averageBrightnessBySize(threadData)
        }
        bench.brightnessCalcTime += brightness.second
        return brightness.first
    }

    private fun getRgbData(yOffset: Int, size: CustomSize): Array<IntArray> {
        val colorArray = Array(size.height) { IntArray(size.width) }

        for (y in 0 until size.height step 2) {
            bufferedImage!!.getRGB(0, yOffset + y, size.width, 1, colorArray[y], 0, 0)

            // Трум-трум лайфхаки ради 10% общего перфоманса
            // Считываем только каждую вторую горизонтальную строку
            if (y < size.height && y + 1 < colorArray.size) {
                colorArray[y + 1] = colorArray[y]
            }
        }

        return colorArray
    }

    private fun Array<IntArray>.averageBrightnessBySize(threadData: ThreadInputData): Array<Array<CustomColor>?> {
        val threadHeightInSymbols = threadData.threadHeightInSymbols + threadData.lastRowExtraSymbols
        val colorArray = Array<Array<CustomColor>?>(threadHeightInSymbols) {
            Array(symbolsPerXDimension) { CustomColor(0, 0, 0, 0.0f) }
        }

        val symbolSize = threadData.symbolSizeInPixels
        repeat(threadData.threadHeightInSymbols) { y ->
            repeat(symbolsPerXDimension) { x ->
                colorArray[y]!![x] = calculateBrightness(y * symbolSize.height, x * symbolSize.width, symbolSize)
            }
        }

        repeat(threadData.lastRowExtraSymbols) { y ->
            repeat(symbolsPerXDimension) { x ->
                colorArray[threadData.threadHeightInSymbols + y]!![x] = calculateBrightness(
                    yOffset = (threadData.threadHeightInSymbols + y) * (symbolSize.height),
                    xOffset = x * symbolSize.width,
                    symbolSize = symbolSize
                )
            }
        }

        return colorArray
    }

    private fun Array<IntArray>.calculateBrightness(yOffset: Int, xOffset: Int, symbolSize: CustomSize): CustomColor {
        var red = 0
        var green = 0
        var blue = 0

        for (blockY in yOffset until yOffset + symbolSize.height) {
            for (blockX in xOffset until xOffset + symbolSize.width) {
                red += this[blockY][blockX] ushr 16 and 0xFF
                green += this[blockY][blockX] ushr 8 and 0xFF
                blue += this[blockY][blockX] ushr 0 and 0xFF
            }
        }
        val luminance = (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255

        val area = symbolSize.height * symbolSize.width
        return CustomColor(
            red / area,
            green / area,
            blue / area,
            luminance / area
        )
    }
}