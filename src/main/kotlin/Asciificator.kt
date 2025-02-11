import brightness.calculator.cpu.CoreCpuBrightnessCalculator
import brightness.converter.BrightnessConverter
import paint.cpu.CpuTextPainter
import java.awt.Font
import java.awt.image.BufferedImage

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

        val font = Font(Font.MONOSPACED, Font.PLAIN, inputArgs.fontSize)  // меньше 10 шрифта все шакалится

        val a = measureTimeNanos("paint") {
            CpuTextPainter(font, inputArgs.symbolToPixelAreaRatio)
                .drawImage(char2DArray, color2DList, inputArgs.colored, inputArgs.scale)
        }
        paintTime += a.second

        return a.first
    }
}