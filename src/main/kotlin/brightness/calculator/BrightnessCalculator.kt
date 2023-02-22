package brightness.calculator

import Color
import workdistribution.ThreadWorkDistributor
import java.awt.image.BufferedImage

abstract class BrightnessCalculator(
    protected val symbolToPixelAreaRatio: Int,
) {

    abstract fun calculateColor(image: BufferedImage): List<Array<Color>>
}