package brightness

import Size
import kotlinx.coroutines.*
import workdistribution.ThreadInputData
import workdistribution.ThreadWorkDistributor
import java.awt.image.BufferedImage

class CpuBrightnessCalculator(
    threadWorkDistributor: ThreadWorkDistributor,
    symbolToPixelAreaRatio: Int,
) : BrightnessCalculator(threadWorkDistributor, symbolToPixelAreaRatio) {

    private var bufferedImage: BufferedImage? = null

    override fun calculateBrightness(image: BufferedImage): Array<FloatArray> {
        bufferedImage = image

        val threadData2DArray = threadWorkDistributor.getThreadInputData2DArray()

        val width = threadData2DArray.size
        val height = threadData2DArray[0].size

        val brightnessList = getBrightness(threadData2DArray)

        val brightness2DArray = Array(width) { xIndex ->
            FloatArray(height) { yIndex ->
                brightnessList[threadData2DArray.size * yIndex + xIndex]
            }
        }

        bufferedImage = null
        return brightness2DArray
    }

    private fun getBrightness(inputThreadData2DArray: Array<Array<ThreadInputData?>>): List<Float> {
        val brightnessListDeferred = mutableListOf<Deferred<Float>>()

        return runBlocking(Dispatchers.Default) {
            inputThreadData2DArray.forEach { threadDataArray ->
                threadDataArray.forEach { threadData ->
                    brightnessListDeferred.add(getDeferredBrightness(threadData))
                }
            }

            brightnessListDeferred.awaitAll()
        }
    }

    private fun CoroutineScope.getDeferredBrightness(threadInputData: ThreadInputData?) = async {
        requireNotNull(threadInputData) { "amogus" }
        getMediumBrightness(
            getColorData(
                threadInputData.threadWorkAreaXOffset,
                threadInputData.threadWorkAreaYOffset,
                threadInputData.threadWorkAreaSize
            )
        )
    }


    private fun getColorData(xOffset: Int, yOffset: Int, size: Size): Array<IntArray> {
        val colorArray = Array(size.width) { IntArray(size.height) }

        for (x in 0 until size.width) {
            for (y in 0 until size.height) {
                colorArray[x][y] = bufferedImage!!.getRGB(
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