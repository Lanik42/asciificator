package brightness

import Size
import kotlinx.coroutines.*
import measureTimeMillis
import threads.AreaThreadWorkDistributor
import threads.ThreadWorkDistributor
import java.awt.image.BufferedImage

class CpuBrightnessCalculator(
    threadWorkDistributor: ThreadWorkDistributor,
    symbolToPixelAreaRatio: Int,
) : BrightnessCalculator(threadWorkDistributor, symbolToPixelAreaRatio) {

    private var bufferedImage: BufferedImage? = null

    override fun calculateBrightness(image: BufferedImage): Array<FloatArray> {
        bufferedImage = image

        val inputThreadData2DArray = threadWorkDistributor.getThreadData2DArray()
        var brightness2DArray: Array<FloatArray> = arrayOf()

        val width = inputThreadData2DArray.size
        val height = inputThreadData2DArray[0].size

        // ------------ SINGLE THREAD ----------
        measureTimeMillis("single thread") {
            val brightnessList = getDataSingleThread(inputThreadData2DArray)

            brightness2DArray = Array(width) { xIndex ->
                FloatArray(height) { yIndex ->
                    brightnessList[inputThreadData2DArray.size * yIndex + xIndex]
                }
            }
        }

        // ------------ MULTI THREAD ----------
        measureTimeMillis("multi thread") {
            val brightnessListMultiThread = getDataMultiThread(inputThreadData2DArray)

            val brightness2DArrayMultiThread = Array(width) { xIndex ->
                FloatArray(height) { yIndex ->
                    brightnessListMultiThread[inputThreadData2DArray.size * yIndex + xIndex]
                }
            }
        }


        return brightness2DArray
    }

    private fun getDataSingleThread(inputThreadData2DArray: Array<Array<ThreadData?>>): List<Float> {
        val brightnessList = mutableListOf<Float>()
        inputThreadData2DArray.forEach { threadDataArray ->
            threadDataArray.forEach { threadData ->
                requireNotNull(threadData) { "amogus" }
                brightnessList.add(
                    getMediumBrightness(
                        getColorData(
                            threadData.threadWorkAreaXOffset,
                            threadData.threadWorkAreaYOffset,
                            threadData.threadWorkAreaSize
                        )
                    )
                )
            }
        }
        return brightnessList
    }

    private fun getDataMultiThread(inputThreadData2DArray: Array<Array<ThreadData?>>): List<Float> {
        val brightnessListDeferred = mutableListOf<Deferred<Float>>()

        return runBlocking(Dispatchers.Default) {
            inputThreadData2DArray.forEach { threadDataArray ->
                threadDataArray.forEach { threadData ->
                    async {
                        requireNotNull(threadData) { "amogus" }
                        getMediumBrightness(
                            getColorData(
                                threadData.threadWorkAreaXOffset,
                                threadData.threadWorkAreaYOffset,
                                threadData.threadWorkAreaSize
                            )
                        )
                    }.let {
                        brightnessListDeferred.add(it)
                    }
                }
            }

            brightnessListDeferred.awaitAll()
        }
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