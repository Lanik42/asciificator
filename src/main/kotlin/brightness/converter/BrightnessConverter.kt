package brightness.converter

import brightness.CustomColor


class BrightnessConverter(
    private val colored: Boolean
) {

    private companion object {

        // @%#*+=-:.
        // $@B%8&WM#*oahkbdpqwmZO0QLYXzcvuft/\|()1+~i!lI;:,"^`'.
        // $@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\|()1{}[]?-+~<>i!lI;:,"^`'.
        // "\$@B%8&WM#*oahkbdpqwmzcvunxrjft-+~i!lI;:,^`'."

        // $@B%8&WM#*oahkbdpqwmzcvunxrjft-+~i!lI;:,^.
        // @#%8&dao:*.
        // @#%8&*c.
        // #@oi!;"'`
        // #@%8&*oi!"`.

        // Не использовать @, тк этот символ имеет отличающуюся ширину от остальных, из-за этого невозможно сделать все строки одной
        // ширины =(
        const val SYMBOLS_BY_BRIGHTNESS = "B%8&WM#*oahkbdpqcnxrjft-~i!lI;:,^. "
        const val SYMBOLS_BY_BRIGHTNESS_COLORED = "@"
    }

    private val brightnessScale = SYMBOLS_BY_BRIGHTNESS.takeIf { colored } ?: SYMBOLS_BY_BRIGHTNESS

    private val brightnessLevelsAmount = brightnessScale.length
    private val brightnessStep = 1 / brightnessLevelsAmount.toFloat()
    private val brightnessLevels = brightnessScale.mapIndexed { index, _ ->
        index * brightnessStep
    }

    fun convertToSymbols(brightness2DList: Array<Array<CustomColor>>): Array<CharArray> {
        val symbols2DArray = Array(brightness2DList.size) { CharArray(brightness2DList[0].size) }

        brightness2DList.forEachIndexed { x, brightnessArray ->
            brightnessArray.forEachIndexed { y, colorInfo ->
                symbols2DArray[x][y] = convert(colorInfo.brightness)
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

            if (brightnessLevels[i] <= brightness && brightnessLevels[i + 1] >= brightness) {
                index = i
                break
            }
        }

        return brightnessScale[index]
    }
}