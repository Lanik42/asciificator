import brightness.calculator.cpu.CoreCpuBrightnessCalculator
import brightness.converter.BrightnessConverter
import paint.TextPainter
import java.awt.Font
import java.awt.image.BufferedImage


enum class WorkDistributionType {
    BY_AREA,
    BY_COLUMN
}

//data class Color(
//    val r: Int,
//    val g: Int,
//    val b: Int,
//    val brightness: Float
//)

class Asciificator {

    companion object Bench {

        var paintTime = 0L
        var frameCount = 0L
    }

    fun processImage(
        bufferedImage: BufferedImage,
        inputArgs: InputArgs,
        bench: CoreCpuBrightnessCalculator.Bench = CoreCpuBrightnessCalculator.Bench()
    ): BufferedImage {
        frameCount++
        val imageSize = CustomSize(bufferedImage.width, bufferedImage.height)

        val coreCpuBrightnessCalculator = CoreCpuBrightnessCalculator(imageSize, inputArgs.symbolToPixelAreaRatio)
        val color2DList = coreCpuBrightnessCalculator.calculateBrightness(bufferedImage, bench)

        val char2DArray = measureTimeNanos("bright converter") {
            BrightnessConverter(inputArgs.colored).convertToSymbols(color2DList)
        }.first

        val font = Font(Font.MONOSPACED, Font.PLAIN, inputArgs.fontSize)  // меньше 11 шрифта все шакалится

        val a = measureTimeNanos("paint") {
            TextPainter(font, inputArgs.symbolToPixelAreaRatio)
                .drawImage(char2DArray, color2DList, inputArgs.colored, inputArgs.scale)
        }
        paintTime += a.second

        return a.first
    }
}