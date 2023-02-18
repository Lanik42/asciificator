package brightness.calculator

import workdistribution.ThreadWorkDistributor
import java.awt.image.BufferedImage

abstract class BrightnessCalculator(
    protected val symbolToPixelAreaRatio: Int,
) {

    abstract fun calculateBrightness(image: BufferedImage): List<FloatArray>
}