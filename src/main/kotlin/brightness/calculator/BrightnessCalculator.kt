package brightness.calculator

import brightness.CustomColor
import brightness.calculator.cpu.CoreCpuBrightnessCalculator
import java.awt.image.BufferedImage

abstract class BrightnessCalculator(
    protected val symbolToPixelAreaRatio: Int,
) {

    abstract fun calculateBrightness(
        image: BufferedImage,
        bench: CoreCpuBrightnessCalculator.Bench
    ): Array<Array<CustomColor>>
}