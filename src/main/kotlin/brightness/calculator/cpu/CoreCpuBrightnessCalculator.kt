package brightness.calculator.cpu

import Color
import Size
import brightness.calculator.BrightnessCalculator
import measureTimeMillis
import workdistribution.core.Area
import workdistribution.core.CoreThreadWorkDistributor
import workdistribution.core.ThreadInputData
import workdistribution.core.ThreadManager
import java.awt.image.BufferedImage
import java.util.concurrent.Future

class CoreCpuBrightnessCalculator(
    private val imageSize: Size,
    symbolToPixelAreaRatio: Int,
) : BrightnessCalculator(symbolToPixelAreaRatio) {

    private var bufferedImage: BufferedImage? = null

    override fun calculateColor(image: BufferedImage): List<Array<Color>> {
        bufferedImage = image

        return measureTimeMillis("computing brighntess") {
            val threadDataArray = measureTimeMillis("work distribution") {
                CoreThreadWorkDistributor(
                    symbolToPixelAreaRatio,
                    Size(image.width, image.height)
                ).getThreadInputData2DArray()
            }

            val colorList = getColors(threadDataArray)
            bufferedImage = null
            colorList
        }

    }

    // 1st version = 800ms. 2nd - 650? (get pixels by area + remove flatMap to calculate averageBrightness) . 3rd - 450ms
    // TODO!!! Сейчас массив, который возвращает getBrighntessOuter, возвращает Array цветов для всей области из потока (1/12 картинки), либо придумать что делать здесь
    // TODO либо подогнать TextPainter
    private fun getColors(threadDataArray: Array<ThreadInputData?>): List<Array<Color>> {
        val symbolsPerXDimension = imageSize.width / symbolToPixelAreaRatio
        val brightnessArray = ArrayList<Future<Array<Color>>>(symbolsPerXDimension)

        measureTimeMillis("thread work post") {
            ThreadManager.executors.forEachIndexed { index, executor ->
                executor.submit<Array<Color>> {
                    getBrightnessOuter(threadDataArray[index], symbolsPerXDimension)
                }.also { brightnessArray.add(it) }
            }
        }

        return measureTimeMillis("thread await") {
            brightnessArray.map {
                it.get()
            }
        }
    }

    private fun getBrightnessOuter(threadInputData: ThreadInputData?, symbolsPerXDimension: Int): Array<Color> {
        requireNotNull(threadInputData)

        val symbolsPerYDimension = imageSize.height / symbolToPixelAreaRatio
        val colorArray = Array(threadInputData.threadSymbolsHeight * symbolsPerXDimension) { Color(0,0,0,0f) }

        threadInputData.symbolAreaListByHeight.forEach { area ->
            val threadYPositionOffset = threadInputData.threadAreaYOffset * threadInputData.threadSymbolsHeight
            val areaYOffset = threadYPositionOffset + area.y
            val threadYPositionOffsetPixels = threadYPositionOffset * symbolToPixelAreaRatio
            val areaYOffsetPixels = threadYPositionOffsetPixels + area.y * symbolToPixelAreaRatio

            getBrightness(symbolsPerXDimension, areaYOffsetPixels, area, colorArray)
        }

        return colorArray
    }

    private fun getBrightness(symbolsPerXDimension: Int, yOffset: Int, area: Area, outArray: Array<Color>) {
        for (x in 0 until symbolsPerXDimension) {
            val intColor2DArray = getIntColorsData(
                xOffset = x * symbolToPixelAreaRatio,
                yOffset = yOffset,
                size = area.size,
            )

            outArray[x] = intColor2DArray.averageColor()
        }
    }

    private fun getIntColorsData(xOffset: Int, yOffset: Int, size: Size): Array<IntArray> {
        val colorArray = Array(size.height) { IntArray(size.width) }

        for (y in 0 until size.height) {
            bufferedImage!!.getRGB(xOffset, yOffset + y, size.width, 1, colorArray[y], 0, 0)
        }

        return colorArray
    }

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

        val area = size * size
        return Color(
            r = red / area,
            g = green / area,
            b = blue / area,
            brightness = luminance / area
        )
    }
}