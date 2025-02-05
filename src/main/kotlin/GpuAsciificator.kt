import brightness.converter.BrightnessConverter
import paint.cpu.CpuTextPainter
import java.awt.Font
import java.awt.image.BufferedImage

class GpuAsciificator(private val gpuColorCalculator: CoolestGpuSingleKernelCalculator) {

    fun processImage(bufferedImage: BufferedImage, inputArgs: InputArgs): BufferedImage {
        val color2DList = gpuColorCalculator.run(bufferedImage, inputArgs.symbolToPixelAreaRatio)
        val char2DArray = BrightnessConverter(inputArgs.colored).convertToSymbols(color2DList)

        val font =
            Font(Font.MONOSPACED, Font.PLAIN, inputArgs.fontSize)  // меньше 11 шрифта все шакалится
        val output = CpuTextPainter(font, inputArgs.symbolToPixelAreaRatio)
            .drawImage(char2DArray, color2DList, inputArgs.colored, inputArgs.scale)

        return output
    }
}