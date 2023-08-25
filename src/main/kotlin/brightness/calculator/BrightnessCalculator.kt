package brightness.calculator

import Color
import java.awt.image.BufferedImage

abstract class BrightnessCalculator(
    protected val symbolToPixelAreaRatio: Int,
) {

    abstract fun calculateColor(image: BufferedImage): List<Array<Color>>
}