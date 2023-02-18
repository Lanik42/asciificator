package brightness

import threads.ThreadWorkDistributor
import java.awt.image.BufferedImage

abstract class BrightnessCalculator(
    protected val threadWorkDistributor: ThreadWorkDistributor,
    protected val symbolToPixelAreaRatio: Int,
) {

    abstract fun calculateBrightness(image: BufferedImage): Array<FloatArray>
}