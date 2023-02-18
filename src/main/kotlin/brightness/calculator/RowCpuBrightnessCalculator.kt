package brightness.calculator

import Size
import kotlinx.coroutines.*
import measureTimeMillis
import workdistribution.row.RowInputData
import workdistribution.row.RowThreadWorkDistributor
import java.awt.image.BufferedImage
import java.util.ArrayList

class RowCpuBrightnessCalculator(
    private val imageSize: Size,
    symbolToPixelAreaRatio: Int,
): BrightnessCalculator(symbolToPixelAreaRatio) {

    private var bufferedImage: BufferedImage? = null

    override fun calculateBrightness(image: BufferedImage): List<FloatArray> {
        bufferedImage = image

        return measureTimeMillis {
            val threadDataArray = RowThreadWorkDistributor(
                symbolToPixelAreaRatio,
                Size(image.width, image.height)
            ).getThreadInputData2DArray()

            val brightnessList = getBrightness(threadDataArray)

            bufferedImage = null
            brightnessList
        }
    }

    // Improve: сразу создавать 2d массив, чтобы потом не перекидывать данные
    private fun getBrightness(threadDataArray: Array<RowInputData?>): List<FloatArray> {
        val symbolsPerXDimension = imageSize.width / symbolToPixelAreaRatio
        val brightnessArrayDeferred = ArrayList<Deferred<FloatArray>>(symbolsPerXDimension)

        return runBlocking(Dispatchers.Default) {
            threadDataArray.forEach { threadData ->
                brightnessArrayDeferred.add(getDeferredBrightness(threadData, symbolsPerXDimension))
            }

           brightnessArrayDeferred.awaitAll()
        }
    }

    private fun CoroutineScope.getDeferredBrightness(threadInputData: RowInputData?, symbolsPerXDimension: Int) = async {
        requireNotNull(threadInputData) { "amogus" }
        val brightnessArray = FloatArray(symbolsPerXDimension) { -1f }

        for (x in 0 until symbolsPerXDimension) {
            val brightness = getMediumBrightness(
                getColorData(
                    xOffset = x,
                    yOffset = threadInputData.yOffset,
                    size = threadInputData.singleSymbolArea,
                )
            )
            brightnessArray[x] = brightness
        }
        brightnessArray
    }


    private fun getColorData(xOffset: Int, yOffset: Int, size: Size): Array<IntArray> {
        val colorArray = Array(size.height) { IntArray(size.width) }

        for (y in 0 until size.height) {
            for (x in 0 until size.width) {
                colorArray[y][x] = bufferedImage!!.getRGB(
                    xOffset * symbolToPixelAreaRatio + x,
                    yOffset * symbolToPixelAreaRatio + y
                )
            }
        }

        return colorArray
    }

    private fun getMediumBrightness(colorData: Array<IntArray>): Float =
        colorData.flatMap {
            it.asIterable()
        }.getBrightness()

    private fun List<Int>.getBrightness(): Float {
        var luminance = 0f
        forEach { color ->
            val red = color ushr 16 and 0xFF
            val green = color ushr 8 and 0xFF
            val blue = color ushr 0 and 0xFF

            luminance += (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255
        }
        return luminance / size
    }
}