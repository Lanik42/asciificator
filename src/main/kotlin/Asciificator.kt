import brightness.CpuBrightnessCalculator
import threads.AreaThreadWorkDistributor
import threads.ColumnThreadWorkDistributor
import java.awt.image.BufferedImage

enum class WorkDistributionType {
    BY_AREA,
    BY_COLUMN
}

class Asciificator {

    // аскификатор сам определяет, каким brightnessCalculator пользоваться. На InputArgs можно оставить выбор cpu / gpu
    fun processImage(bufferedImage: BufferedImage, symbolToPixelAreaRatio: Int) {
        val imageSize = Size(bufferedImage.width, bufferedImage.height)

        val threadWorkDistributor = ColumnThreadWorkDistributor(symbolToPixelAreaRatio, imageSize) // default
        val brightnessCalculator = CpuBrightnessCalculator(
            threadWorkDistributor,
            symbolToPixelAreaRatio,
        )

        val brightnessArray = brightnessCalculator.calculateBrightness(bufferedImage)
    }

    fun testProcessImage(bufferedImage: BufferedImage, symbolToPixelAreaRatio: Int, workDistributionType: WorkDistributionType) {
        val imageSize = Size(bufferedImage.width, bufferedImage.height)

        val threadWorkDistributor = getThreadWorkDistributor(workDistributionType, symbolToPixelAreaRatio, imageSize)
        val brightnessCalculator = CpuBrightnessCalculator(
            threadWorkDistributor,
            symbolToPixelAreaRatio,
        )

        val brightnessArray = brightnessCalculator.calculateBrightness(bufferedImage)
    }

    private fun getThreadWorkDistributor(workDistributionType: WorkDistributionType, symbolToPixelAreaRatio: Int, imageSize: Size) =
        when(workDistributionType) {
            WorkDistributionType.BY_AREA -> AreaThreadWorkDistributor(symbolToPixelAreaRatio, imageSize)
            WorkDistributionType.BY_COLUMN -> ColumnThreadWorkDistributor(symbolToPixelAreaRatio, imageSize)
        }

}