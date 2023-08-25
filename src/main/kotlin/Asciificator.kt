import brightness.calculator.cpu.AreaCpuBrightnessCalculator
import brightness.calculator.cpu.CoreCpuBrightnessCalculator
import brightness.calculator.cpu.RowCpuBrightnessCalculator
import brightness.calculator.gpu.AreaGpuBrightnessCalculator
import brightness.calculator.gpu.SimpleGpuBrightnessCalculator
import brightness.converter.BrightnessConverter
import paint.TextPainter
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

enum class WorkDistributionType {
    BY_AREA,
    BY_COLUMN
}

data class Color(
    val r: Int,
    val g: Int,
    val b: Int,
    val brightness: Float
)

class Asciificator {

    // На InputArgs можно оставить выбор cpu / gpu
    fun processImage(bufferedImage: BufferedImage, inputArgs: InputArgs) {
        val imageSize = Size(bufferedImage.width, bufferedImage.height)

        val areaBrightnessCalculator = AreaCpuBrightnessCalculator(inputArgs.symbolToPixelAreaRatio)
        val rowBrightnessCalculator = RowCpuBrightnessCalculator(imageSize, inputArgs.symbolToPixelAreaRatio)
        val coreCpuBrightnessCalculator = CoreCpuBrightnessCalculator(imageSize, inputArgs.symbolToPixelAreaRatio)
        val areaGpuBrightnessCalculator = AreaGpuBrightnessCalculator(inputArgs.symbolToPixelAreaRatio)
        val simpleGpuBrightnessCalculator = SimpleGpuBrightnessCalculator(inputArgs.symbolToPixelAreaRatio)

        val color2DList = coreCpuBrightnessCalculator.calculateColor(bufferedImage)
        val char2DArray = BrightnessConverter(inputArgs.colored).convertToSymbols(color2DList)

        val font = Font(Font.MONOSPACED, Font.PLAIN, inputArgs.fontSize)  // меньше 11 шрифта все шакалится
        val outputImage = TextPainter(font, inputArgs.symbolToPixelAreaRatio)
            .drawImage(char2DArray, color2DList, inputArgs.colored, inputArgs.scale)

        measureTimeMillis("write") {
            writeImage(outputImage, inputArgs.outFormat, inputArgs.outPath)
        }
    }

    private fun writeImage(image: BufferedImage, format: String, path: String) {
        try {
            if (image.type == BufferedImage.TYPE_BYTE_GRAY) {
                ImageIO.write(image, "gif", File("$path.gif"))
            } else {
                ImageIO.write(image, "jpg", File("$path.jpg"))
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    fun testProcessImage(bufferedImage: BufferedImage, symbolToPixelAreaRatio: Int, workDistributionType: WorkDistributionType) {
        val imageSize = Size(bufferedImage.width, bufferedImage.height)

        val brightnessCalculatorRow = RowCpuBrightnessCalculator(
            imageSize,
            symbolToPixelAreaRatio,
        )
        val brightnessCalculatorArea = AreaCpuBrightnessCalculator(
            symbolToPixelAreaRatio
        )

        brightnessCalculatorRow.calculateColor(bufferedImage)
    }

//    private fun getThreadWorkDistributor(workDistributionType: WorkDistributionType, symbolToPixelAreaRatio: Int, imageSize: Size) =
//        when(workDistributionType) {
//            WorkDistributionType.BY_AREA -> AreaThreadWorkDistributor(symbolToPixelAreaRatio, imageSize)
//            WorkDistributionType.BY_COLUMN -> RowThreadWorkDistributor(symbolToPixelAreaRatio, imageSize)
//        }

}