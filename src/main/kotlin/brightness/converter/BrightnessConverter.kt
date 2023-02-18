package brightness.converter

import java.io.File

class BrightnessConverter {

    private companion object {

        const val SYMBOLS_BY_BRIGHTNESS = "\$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\"^`'."
    }

    private val brightnessLevelsAmount = SYMBOLS_BY_BRIGHTNESS.length
    private val brightnessStep = 1 / brightnessLevelsAmount.toFloat()
    private val brightnessLevels = SYMBOLS_BY_BRIGHTNESS.mapIndexed { index, _ ->
        index * brightnessStep
    }

    fun convertToSymbols(brightness2DList: List<FloatArray>): Array<CharArray> {
        val symbols2DArray = Array(brightness2DList.size) { CharArray(brightness2DList[0].size) }

        brightness2DList.forEachIndexed { x, brightnessArray ->
            brightnessArray.forEachIndexed { y, brightness ->
                symbols2DArray[x][y] = convert(brightness)
            }
        }

        val file = File("C:\\amogus2.txt")
        file.createNewFile()
        file.writer().use { os ->
            symbols2DArray.forEach {
                os.write(it)
                os.write("\n")
            }
        }

        return symbols2DArray
    }

    // Яркость должна быть в диапазоне 0..1
    private fun convert(brightness: Float): Char {
        var index = -1

        for (i in 0 until brightnessLevelsAmount) {
            if (i == brightnessLevelsAmount - 1) {
                index = i
                break
            }

            if (brightnessLevels[i] <= brightness && brightnessLevels[i+1] >= brightness) {
                index = i
                break
            }
        }

        return SYMBOLS_BY_BRIGHTNESS[index]
    }
}