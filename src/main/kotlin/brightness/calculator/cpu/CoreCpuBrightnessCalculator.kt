package brightness.calculator.cpu

import Color
import CustomSize
import brightness.calculator.BrightnessCalculator
import workdistribution.core.CoreThreadWorkDistributor
import workdistribution.core.ThreadInputData
import workdistribution.core.ThreadManager
import java.awt.image.BufferedImage
import java.util.concurrent.Future

class CoreCpuBrightnessCalculator(
    imageSize: CustomSize,
    symbolToPixelAreaRatio: Int,
) : BrightnessCalculator(symbolToPixelAreaRatio) {

    private var bufferedImage: BufferedImage? = null

    private val symbolsPerXDimension = imageSize.width / symbolToPixelAreaRatio
    private val symbolsPerYDimension = imageSize.height / symbolToPixelAreaRatio

    override fun calculateBrightness(image: BufferedImage): Array<Array<Color>> {
        bufferedImage = image

        val threadDataArray = CoreThreadWorkDistributor(
            symbolToPixelAreaRatio,
            CustomSize(image.width, image.height)
        ).getThreadInputData2DArray()

        val brightnessList = getBrightness(threadDataArray)
        bufferedImage = null
        return brightnessList
    }

//    ThreadManager.executors.forEachIndexed { index, executor ->
//        executor.submit<Array<Array<Color>?>> {
//            val threadOffsetInSymbols = if (index == 0) {
//                0
//            } else {
//                threadDataArray[index - 1]!!.threadHeightInSymbols * index
//            }
//
//            getBrightnessByThread(threadDataArray[index], threadOffsetInSymbols)
//        }.also { futureArray[index] = it }

    // 1st version = 800ms. 2nd - 650? (get pixels by area + remove flatMap to calculate averageBrightness) . 3rd - 450ms
    // TODO!!! Сейчас массив, который возвращает getBrighntessOuter, возвращает Array цветов для всей области из потока (1/12 картинки), либо придумать что делать здесь
    // TODO либо подогнать TextPainter
    private fun getBrightness(threadDataArray: Array<ThreadInputData?>): Array<Array<Color>> {
        val futureArray = Array<Future<Array<Array<Color>?>>?>(symbolsPerYDimension) { null }

        threadDataArray.forEachIndexed { index, it ->
            ThreadManager.nexecutors.submit<Array<Array<Color>?>> {
                val threadOffsetInSymbols = if (index == 0) {
                    0
                } else {
                    threadDataArray[index - 1]!!.threadHeightInSymbols * index
                }

                getBrightnessByThread(it, threadOffsetInSymbols)
            }.also { futureArray[index] = it }
        }

        val colorArray = Array<Array<Color>?>(symbolsPerYDimension) { null }
        futureArray.forEachIndexed { offset, future ->
            future?.get()?.forEachIndexed { index, value ->
                colorArray[offset * threadDataArray[0]!!.threadHeightInSymbols + index] = value
            }
        }


        return colorArray.requireNoNulls()
    }

    private fun getBrightnessByThread(threadData: ThreadInputData?, threadOffsetInSymbols: Int): Array<Array<Color>?> {
        requireNotNull(threadData)

        val threadYPositionOffsetPixels = threadOffsetInSymbols * symbolToPixelAreaRatio
        val symbolSize = threadData.symbolSizeInPixels
        val threadPixelSize = CustomSize(
            height = symbolSize.height * (threadData.threadHeightInSymbols + threadData.lastRowExtraSymbols),
            width = bufferedImage!!.width
        )

        return getBrightness(threadYPositionOffsetPixels, threadPixelSize, threadData)
    }

    private fun getBrightness(yOffset: Int, threadPixelSize: CustomSize, threadData: ThreadInputData): Array<Array<Color>?> {
        val rgb2DArray = getRgbData(yOffset = yOffset, size = threadPixelSize)

        return rgb2DArray.averageBrightnessBySize(threadData)
    }

    private fun getRgbData(yOffset: Int, size: CustomSize): Array<IntArray> {
        val colorArray = Array(size.height) { IntArray(size.width) }

        repeat(size.height) { y ->
            bufferedImage!!.getRGB(0, yOffset + y, size.width, 1, colorArray[y], 0, 0)
        }

        return colorArray
    }

    private fun Array<IntArray>.averageBrightnessBySize(threadData: ThreadInputData): Array<Array<Color>?> {
        val threadHeightInSymbols = threadData.threadHeightInSymbols + threadData.lastRowExtraSymbols
        val colorArray = Array<Array<Color>?>(threadHeightInSymbols) {
            Array(symbolsPerXDimension) { Color(0, 0, 0, 0.0f) }
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

    private fun Array<IntArray>.calculateBrightness(yOffset: Int, xOffset: Int, symbolSize: CustomSize): Color {
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
        return Color(
            r = red / area,
            g = green / area,
            b = blue / area,
            brightness = luminance / area
        )
    }
}