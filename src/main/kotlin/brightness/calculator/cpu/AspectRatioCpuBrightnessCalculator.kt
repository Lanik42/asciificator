package brightness.calculator.cpu

import CustomColor
import CustomSize
import brightness.calculator.BrightnessCalculator
import measureTimeNanos
import workdistribution.core.AspectRatioCoreThreadWorkDistributor
import workdistribution.core.ThreadInputData
import workdistribution.core.ThreadManager
import java.awt.image.BufferedImage
import java.util.concurrent.Future
import kotlin.math.roundToInt
import kotlin.math.sqrt

class AspectRatioCpuBrightnessCalculator(
    private val imageSize: CustomSize,
    symbolToPixelAreaRatio: Int,
) : BrightnessCalculator(symbolToPixelAreaRatio) {

    private var bufferedImage: BufferedImage? = null

    private val scalingFactor =
        sqrt(symbolToPixelAreaRatio.toDouble() * symbolToPixelAreaRatio / (imageSize.width * imageSize.height))

    private val symbolsPerXDimension = (imageSize.width * scalingFactor).roundToInt()
    private val symbolsPerYDimension = (imageSize.height * scalingFactor).roundToInt()

    override fun calculateBrightness(
        image: BufferedImage,
        bench: CoreCpuBrightnessCalculator.Bench
    ): Array<Array<CustomColor>> {
        bench.frameCount++

        bufferedImage = image

        val threadDataArray = AspectRatioCoreThreadWorkDistributor(
            symbolsPerXDimension,
            symbolsPerYDimension,
            imageSize
        ).getThreadInputData2DArray()

        val brightnessList = getBrightness(threadDataArray, bench)
        bufferedImage = null
        return brightnessList
    }

    // 1st version = 800ms. 2nd - 650? (get pixels by area + remove flatMap to calculate averageBrightness) . 3rd - 450ms
    // TODO!!! Сейчас массив, который возвращает getBrighntessOuter, возвращает Array цветов для всей области из потока (1/12 картинки), либо придумать что делать здесь
    // TODO либо подогнать TextPainter
    private fun getBrightness(
        threadDataArray: Array<ThreadInputData?>,
        bench: CoreCpuBrightnessCalculator.Bench
    ): Array<Array<CustomColor>> {
        val futureArray = Array<Future<Array<Array<CustomColor>?>>?>(symbolsPerYDimension) { null }
        val colorArray = Array<Array<CustomColor>?>(symbolsPerYDimension) { null }

        threadDataArray.forEachIndexed { index, it ->
            ThreadManager.nexecutors.submit<Array<Array<CustomColor>?>> {
                val threadOffsetInSymbols = if (index == 0) {
                    0
                } else {
                    threadDataArray[index - 1]!!.threadHeightInSymbols * index
                }

                getBrightnessByThread(it, threadOffsetInSymbols, bench)
            }.also { futureArray[index] = it }
        }

        futureArray.forEachIndexed { offset, future ->
            future?.get()?.forEachIndexed { index, value ->
                colorArray[offset * threadDataArray[0]!!.threadHeightInSymbols + index] = value
            }
        }

        return colorArray.requireNoNulls()
    }

    private fun getBrightnessByThread(
        threadData: ThreadInputData?,
        threadOffsetInSymbols: Int,
        bench: CoreCpuBrightnessCalculator.Bench,
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
        bench: CoreCpuBrightnessCalculator.Bench,
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

        for (y in 0 until size.height) {
            bufferedImage!!.getRGB(0, yOffset + y, size.width, 1, colorArray[y], 0, 0)

            // Трум-трум лайфхаки ради 5% общего перфоманса
            // Считываем только каждую вторую горизонтальную строку, так считывание занимает 0.15мс вместо условных 0.3мс, на картинку вроде особо не влияет
//            if (y < size.height) {
//                colorArray[y + 1] = colorArray[y]
//            }
        }

        return colorArray
    }

    private fun Array<IntArray>.averageBrightnessBySize(threadData: ThreadInputData): Array<Array<CustomColor>?> {
        val threadHeightInSymbols =
            threadData.threadHeightInSymbols + threadData.lastRowExtraSymbols
        val colorArray = Array<Array<CustomColor>?>(threadHeightInSymbols) {
            Array(symbolsPerXDimension) { CustomColor(0, 0, 0, 0.0f) }
        }

        val symbolSize = threadData.symbolSizeInPixels
        repeat(threadData.threadHeightInSymbols) { y ->
            repeat(symbolsPerXDimension) { x ->
                colorArray[y]!![x] =
                    calculateBrightness(y * symbolSize.height, x * symbolSize.width, symbolSize)
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

    private fun Array<IntArray>.calculateBrightness(
        yOffset: Int,
        xOffset: Int,
        symbolSize: CustomSize
    ): CustomColor {
        var red = 0
        var green = 0
        var blue = 0

        for (blockY in yOffset until yOffset + symbolSize.height) {
            for (blockX in xOffset until xOffset + symbolSize.width) {
                try {
                    red += this[blockY][blockX] ushr 16 and 0xFF
                    green += this[blockY][blockX] ushr 8 and 0xFF
                    blue += this[blockY][blockX] ushr 0 and 0xFF
                } catch (e: Exception) {
                    println()
                }
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