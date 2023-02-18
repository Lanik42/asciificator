import brightness.CpuBrightnessCalculator
import workdistribution.AreaThreadWorkDistributor
import workdistribution.RowThreadWorkDistributor
import java.awt.image.BufferedImage

enum class WorkDistributionType {
    BY_AREA,
    BY_COLUMN
}

class Asciificator {

    // На InputArgs можно оставить выбор cpu / gpu
    fun processImage(bufferedImage: BufferedImage, symbolToPixelAreaRatio: Int) {
        val imageSize = Size(bufferedImage.width, bufferedImage.height)

        val threadWorkDistributor = RowThreadWorkDistributor(symbolToPixelAreaRatio, imageSize)
        val brightnessCalculator = CpuBrightnessCalculator(threadWorkDistributor, symbolToPixelAreaRatio,)

        val brightness2DArray = brightnessCalculator.calculateBrightness(bufferedImage)
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
            WorkDistributionType.BY_COLUMN -> RowThreadWorkDistributor(symbolToPixelAreaRatio, imageSize)
        }

}