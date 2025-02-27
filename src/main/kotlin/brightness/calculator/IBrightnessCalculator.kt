package brightness.calculator

import brightness.CustomColor
import java.awt.image.BufferedImage

class CalculatorBench {

    var frameCount = 0
    var fetchRgbTime = 0L
    var brightnessCalcTime = 0L
    var overallPixelProcessTime = 0L
}

interface IBrightnessCalculator {

    fun calculateBrightness(image: BufferedImage, bench: CalculatorBench = CalculatorBench()):  Array<Array<CustomColor>>
}