package brightness.calculator.cpu

import Color
import Size
import brightness.calculator.BrightnessCalculator
import kotlinx.coroutines.*
import measureTimeMillis
import workdistribution.row.RowInputData
import workdistribution.row.RowThreadWorkDistributor
import java.awt.image.BufferedImage
import java.util.concurrent.FutureTask

class RowCpuBrightnessCalculator(
    private val imageSize: Size,
    symbolToPixelAreaRatio: Int,
) : BrightnessCalculator(symbolToPixelAreaRatio) {

    private var bufferedImage: BufferedImage? = null

    private val threadCount = Runtime.getRuntime().availableProcessors()
    private val threadList = Array(threadCount) { Thread() }

    override fun calculateColor(image: BufferedImage): List<Array<Color>> {
        bufferedImage = image

        return measureTimeMillis("computing brighntess") {
            val threadDataArray = RowThreadWorkDistributor(
                symbolToPixelAreaRatio,
                Size(image.width, image.height)
            ).getThreadInputData2DArray()

            val colorList = getColors(threadDataArray)
            bufferedImage = null
            colorList
        }

    }

    private fun getColors(threadDataArray: Array<RowInputData?>): List<Array<Color>> {
        val symbolsPerXDimension = imageSize.width / symbolToPixelAreaRatio
        val brightnessArrayDeferred = ArrayList<Deferred<Array<Color>>>(symbolsPerXDimension)

        return runBlocking(Dispatchers.Default) {
            threadDataArray.forEach { threadData ->
                brightnessArrayDeferred.add(getDeferredBrightness(threadData, symbolsPerXDimension))
            }

            brightnessArrayDeferred.awaitAll()
        }
    }

    private fun CoroutineScope.getDeferredBrightness(
        threadInputData: RowInputData?,
        symbolsPerXDimension: Int
    ): Deferred<Array<Color>> =
        async {
            requireNotNull(threadInputData) { "amogus" }
            val colorArray = Array(symbolsPerXDimension) { Color(0, 0, 0, -1f) }

            for (x in 0 until symbolsPerXDimension) {
                val intColor2DArray = getIntColorsData(
                    xOffset = x * symbolToPixelAreaRatio,
                    yOffset = threadInputData.areaYOffset * symbolToPixelAreaRatio,
                    size = threadInputData.areaSize,
                )

                val color = getAverageColor(intColor2DArray)
                colorArray[x] = color
            }
            colorArray
        }


    private fun getIntColorsData(xOffset: Int, yOffset: Int, size: Size): Array<IntArray> {
        val colorArray = Array(size.height) { IntArray(size.width) }

        for (y in 0 until size.height) {
            bufferedImage!!.getRGB(xOffset, yOffset + y, size.width, 1, colorArray[y], 0, 0)
        }

        return colorArray
    }

    private fun getAverageColor(colorInt2DArray: Array<IntArray>): Color =
        colorInt2DArray.averageColor()


    private fun Array<IntArray>.averageColor(): Color {
        var red = 0
        var green = 0
        var blue = 0

        forEach { array ->
            array.forEach { color ->
                red += color ushr 16 and 0xFF
                green += color ushr 8 and 0xFF
                blue += color ushr 0 and 0xFF
            }

        }
        val luminance = (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255

        return Color(
            r = red / size,
            g = green / size,
            b = blue / size,
            brightness = luminance / size
        )
    }
}