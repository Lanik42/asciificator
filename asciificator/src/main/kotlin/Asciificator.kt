import brightness.calculator.CalculatorBench
import brightness.calculator.cpu.BrightnessCalculator
import brightness.calculator.cpu.ParallelBrightnessCalculator
import brightness.converter.BrightnessConverter
import paint.cpu.CpuTextPainter
import java.awt.Font
import java.awt.image.BufferedImage

enum class WorkType {
    BATCH,
    REALTIME
}

class Asciificator(private val inputArgs: InputArgs, private val workType: WorkType) {

    companion object Bench {

        var paintTime = 0L
        var frameCount = 0L
    }

    private val font = Font(Font.MONOSPACED, Font.PLAIN, inputArgs.fontSize)  // меньше 10 шрифта все шакалится

    fun processImage(bufferedImage: BufferedImage, bench: CalculatorBench = CalculatorBench()): BufferedImage {
        frameCount++
        val imageSize = CustomSize(bufferedImage.width, bufferedImage.height)

        val brightnessCalculator = when (workType) {
            WorkType.REALTIME -> ParallelBrightnessCalculator(imageSize, inputArgs.symbolToPixelAreaRatio)
            WorkType.BATCH    -> BrightnessCalculator(imageSize, inputArgs.symbolToPixelAreaRatio)
        }
        val color2DList = brightnessCalculator.calculateBrightness(bufferedImage, bench)

        val char2DArray = measureTimeNanos("bright converter") {
            BrightnessConverter(inputArgs.colored).convertToSymbols(color2DList)
        }.first

        val a = measureTimeNanos("paint") {
            CpuTextPainter(font, inputArgs.symbolToPixelAreaRatio)
                .drawImage(char2DArray, color2DList, inputArgs.colored, inputArgs.scale)
        }
        paintTime += a.second

        return a.first
    }
}