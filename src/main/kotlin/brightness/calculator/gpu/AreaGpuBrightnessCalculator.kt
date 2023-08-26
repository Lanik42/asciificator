package brightness.calculator.gpu

import Color
import CustomSize
import GpuCalculator
import brightness.calculator.BrightnessCalculator
import kotlinx.coroutines.*
import measureTimeMillis
import workdistribution.area.AreaInputData
import workdistribution.area.AreaThreadWorkDistributor
import java.awt.image.BufferedImage

class AreaGpuBrightnessCalculator(
    symbolToPixelAreaRatio: Int,
) : BrightnessCalculator(symbolToPixelAreaRatio) {

    private var bufferedImage: BufferedImage? = null

    override fun calculateBrightness(image: BufferedImage): Array<Array<Color>> {
        bufferedImage = image

        val threadData2DArray = measureTimeMillis {
            AreaThreadWorkDistributor(
                symbolToPixelAreaRatio,
                CustomSize(image.width, image.height)
            ).getThreadInputData2DArray()
        }.first

        measureTimeMillis("brightness calculation") {
            GpuCalculator().run(threadData2DArray, image, symbolToPixelAreaRatio)
        }
//            val height = threadData2DArray.size
//            val width = threadData2DArray[0].size
//
//            val brightnessList = getBrightness(threadData2DArray)
//
//            val brightness2DArray = MutableList(height) { yIndex ->
//                FloatArray(width) { xIndex ->
//                    brightnessList[yIndex * width + xIndex]
//                }
//            }

        bufferedImage = null
        //    brightness2DArray
        return arrayOf() // TODO заглушка
    }

    // Improve: сразу создавать 2d массив, чтобы потом не перекидывать данные
    private fun getBrightness(inputThreadData2DArray: Array<Array<AreaInputData>>): List<Float> {
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

    private fun CoroutineScope.getDeferredBrightness(areaInputData: AreaInputData) = async {
        getMediumBrightness(
            getColorData(
                areaInputData.areaXOffset,
                areaInputData.areaYOffset,
                areaInputData.areaSize,
            )
        )
    }


    private fun getColorData(xOffset: Int, yOffset: Int, size: CustomSize): Array<IntArray> {
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