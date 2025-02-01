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

    // На InputArgs можно оставить выбор cpu / gpu
    fun processImage(bufferedImage: BufferedImage, inputArgs: InputArgs): BufferedImage {
        val imageSize = CustomSize(bufferedImage.width, bufferedImage.height)

        val coreCpuBrightnessCalculator = CoreCpuBrightnessCalculator(imageSize, inputArgs.symbolToPixelAreaRatio)
        val color2DList = coreCpuBrightnessCalculator.calculateBrightness(bufferedImage)

        val char2DArray = BrightnessConverter(inputArgs.colored).convertToSymbols(color2DList)

        val font = Font(Font.MONOSPACED, Font.PLAIN, inputArgs.fontSize)  // меньше 11 шрифта все шакалится

        return TextPainter(font, inputArgs.symbolToPixelAreaRatio)
            .drawImage(char2DArray, color2DList, inputArgs.colored, inputArgs.scale)
    }
}