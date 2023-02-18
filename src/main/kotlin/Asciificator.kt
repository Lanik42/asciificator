import brightness.calculator.AreaCpuBrightnessCalculator
import brightness.calculator.RowCpuBrightnessCalculator
import brightness.converter.BrightnessConverter
import java.awt.image.BufferedImage

enum class WorkDistributionType {
    BY_AREA,
    BY_COLUMN
}

class Asciificator {

    // На InputArgs можно оставить выбор cpu / gpu
    fun processImage(bufferedImage: BufferedImage, symbolToPixelAreaRatio: Int) {
        val imageSize = Size(bufferedImage.width, bufferedImage.height)

        val areaBrightnessCalculator = AreaCpuBrightnessCalculator(symbolToPixelAreaRatio)
        val rowBrightnessCalculator = RowCpuBrightnessCalculator(imageSize, symbolToPixelAreaRatio)

        val brightness2DList = areaBrightnessCalculator.calculateBrightness(bufferedImage)

        BrightnessConverter().convertToSymbols(brightness2DList)
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

        brightnessCalculatorRow.calculateBrightness(bufferedImage)
    }

//    private fun getThreadWorkDistributor(workDistributionType: WorkDistributionType, symbolToPixelAreaRatio: Int, imageSize: Size) =
//        when(workDistributionType) {
//            WorkDistributionType.BY_AREA -> AreaThreadWorkDistributor(symbolToPixelAreaRatio, imageSize)
//            WorkDistributionType.BY_COLUMN -> RowThreadWorkDistributor(symbolToPixelAreaRatio, imageSize)
//        }

}