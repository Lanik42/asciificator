package brightness.calculator.cpu

import CustomSize
import brightness.CustomColor
import brightness.calculator.CalculatorBench
import brightness.calculator.IBrightnessCalculator
import measureTimeNanos
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte

class BrightnessCalculator(
    private val imageSize: CustomSize,
    private val symbolToPixelAreaRatio: Int,
): IBrightnessCalculator {

    private companion object {

        const val BYTES_PER_PIXEL = 3
    }

    private var bufferedImage: BufferedImage? = null

    private val symbolsPerXDimension = imageSize.width / symbolToPixelAreaRatio
    private val symbolsPerYDimension = imageSize.height / symbolToPixelAreaRatio

    override fun calculateBrightness(image: BufferedImage, bench: CalculatorBench): Array<Array<CustomColor>> {
        bench.frameCount++

        bufferedImage = image

        val brightnessList = measureTimeNanos {
            getBrightness(bench)
        }

        bench.overallPixelProcessTime += brightnessList.second
        bufferedImage = null
        return brightnessList.first
    }

    private fun getBrightness(bench: CalculatorBench): Array<Array<CustomColor>> {
        val rgb2DArray = measureTimeNanos {
            getRgbData(size = imageSize)
        }
        bench.fetchRgbTime += rgb2DArray.second

        val brightness = measureTimeNanos("calculate brightness") {
            rgb2DArray.first.averageBrightnessBySize()
        }

        bench.brightnessCalcTime += brightness.second
        return brightness.first
    }

    private fun getRgbData(size: CustomSize): Array<ByteArray> {
        val rowSize = size.width * BYTES_PER_PIXEL
        val colorArray2 = Array(size.height) { ByteArray(rowSize) }

        val pixels = (bufferedImage!!.raster.dataBuffer as DataBufferByte).data

        colorArray2.forEachIndexed { index, ints ->
            System.arraycopy(pixels, rowSize * index, ints, 0, rowSize)
        }

        return colorArray2
    }

    private fun Array<ByteArray>.averageBrightnessBySize(): Array<Array<CustomColor>> {
        val colorArray = Array(symbolsPerYDimension) {
            Array(symbolsPerXDimension) { CustomColor(0, 0, 0, 0.0f) }
        }

        val xStep = symbolToPixelAreaRatio * BYTES_PER_PIXEL
        repeat(symbolsPerYDimension) { y ->
            repeat(symbolsPerXDimension) { x ->
                colorArray[y][x] = calculateBrightness(y * symbolToPixelAreaRatio, x * xStep)
            }
        }

        return colorArray
    }

    private fun Array<ByteArray>.calculateBrightness(yOffset: Int, xOffset: Int): CustomColor {
        var red = 0
        var green = 0
        var blue = 0

        val xStep = symbolToPixelAreaRatio * BYTES_PER_PIXEL
        for (blockY in yOffset until yOffset + symbolToPixelAreaRatio) {
            var blockX = xOffset
            while (blockX < xOffset + xStep) {
                red += this[blockY][blockX + 2].toInt() and 0xFF
                green += this[blockY][blockX + 1].toInt() and 0xFF
                blue += this[blockY][blockX].toInt() and 0xFF
                blockX += 3
            }
        }
        val luminance = (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255

        val area = symbolToPixelAreaRatio * symbolToPixelAreaRatio
        return CustomColor(
            red / area,
            green / area,
            blue / area,
            luminance / area
        )
    }
}