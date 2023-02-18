import brightness.converter.BrightnessConverter
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess

const val ABSOLUTE_FILE_PATH = "-path"
const val COLORED = "-c"
const val SYMBOL_TO_PIXEL_AREA_RATIO = "-ratio"

fun main(args: Array<String>) {
    val inputArgs = InputArgs(path = "C:\\amogus3.png", symbolToPixelAreaRatio = 64) // args.parse()

    val file = File(inputArgs.path)
    val bufferedImage = ImageIO.read(file)

    Asciificator().processImage(bufferedImage, inputArgs.symbolToPixelAreaRatio)

    exitProcess(0)
}

private fun Array<String>.parse(): InputArgs {
    val nameToValueMap = getArgNameToValueMap()
    return nameToValueMap.parseMapToInputArgs()
}

private fun Array<String>.getArgNameToValueMap(): Map<String, String> {
    val argNameToValueMap = mutableMapOf<String, String>()

    for (i in 0..lastIndex step 2) {
        if (i + 1 >= size || get(i + 1).startsWith("-")) {
            error("Wrong input format! Each argument should be followed by value. Missing value for argument ${get(i)}.")
        }

        argNameToValueMap[get(i)] = get(i+1)
    }

    return argNameToValueMap
}

private fun Map<String, String>.parseMapToInputArgs(): InputArgs {
    val path = get(ABSOLUTE_FILE_PATH) ?: error("Path (-path) argument not specified!")
    val symbolToPixelAreaRatio = get(SYMBOL_TO_PIXEL_AREA_RATIO) ?: error("Ratio (-ratio) argument not specified!")

    return InputArgs(path = path, symbolToPixelAreaRatio = symbolToPixelAreaRatio.toInt())
}

private fun benchmarkDifferentArgs(bufferedImage: BufferedImage) {
    val differentArgsList = listOf(
        InputArgs(path = "C:\\amogus2.jpg", symbolToPixelAreaRatio = 2),
        InputArgs(path = "C:\\amogus2.jpg", symbolToPixelAreaRatio = 4),
        InputArgs(path = "C:\\amogus2.jpg", symbolToPixelAreaRatio = 8),
        InputArgs(path = "C:\\amogus2.jpg", symbolToPixelAreaRatio = 16),
        InputArgs(path = "C:\\amogus2.jpg", symbolToPixelAreaRatio = 32),
        InputArgs(path = "C:\\amogus2.jpg", symbolToPixelAreaRatio = 64),
    )

    val asciificator = Asciificator()
    differentArgsList.forEachIndexed { index, inputArgs ->
        println("\n************** test $index, ratio ${inputArgs.symbolToPixelAreaRatio} **************\n")

        println("-------------- BY_AREA --------------\n")
        asciificator.testProcessImage(bufferedImage, inputArgs.symbolToPixelAreaRatio, WorkDistributionType.BY_AREA)

        println("\n-------------- BY_COLUMN--------------\n")
        asciificator.testProcessImage(bufferedImage, inputArgs.symbolToPixelAreaRatio, WorkDistributionType.BY_COLUMN)
    }
}
