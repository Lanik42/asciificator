import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess

const val ABSOLUTE_FILE_PATH = "-path"
const val COLORED = "-colored"
const val SYMBOL_TO_PIXEL_AREA_RATIO = "-ratio"
const val OUT_FORMAT = "-format" // RESTRICT USER FROM THAT? Gif for grayscale, jpg for colored images (most optimized options)
const val OUT_PATH = "-output"
const val FONT_SIZE = "-fontSize"
const val SCALE_SYMBOLS_FIT = "-scale"

// TODO
// 1. Посмотреть, как можно справиться с отступами по x слева и по y снизу (TextPainter)
// 2. Вынести в инпуты способ определения высоты / ширины символа - либо = symbolToPixelAreaRatio, либо
//    g2d1.fontMetrics.ascent + LINE_HEIGHT_CORRECTION и g2d1.fontMetrics.charWidth('@') + SYMBOLS_SPACING_CORRECTION
//    Способ определения высоты / ширины можно назвать "Скалировать изображение, чтобы каждый символ был различим"
//    Возможно стоит разбить на две галочки скалирование по высоте / по ширине
// 3. Подумать, стоит ли давать юзеру выбирать формат
// 4. Подобрать наиболее удачные параметры для записи форматов (TextPainter)
//   * grayscale: gif + TYPE_BYTE_GRAY - smaller than jpg files, ~20% faster than jpg, low quality
//
//   * colored:
//   *   1. gif + TYPE_BYTE_INDEXED is faster, but not color accurate, low quality
//   *   2. jpg + TYPE_3BYTE_BGR 2x size gif, 20% slower speed, but color accurate
//
//   * check IndexColorModel and color sizes. Perhaps  12 / 16 bit per pixel color would be better for colored paint
// 5. GPU вычисления?
// 6. Аскификация видео + аудио

// КОРУТИНЫ ГОВНО!!!!! 40мс на иницаилизацию!!!!! Тред пул на 6 потоков инициализируется за 2мс!!!!! Выпилить корутины!!!

fun main(args: Array<String>) {
    val inputArgs = args.parse()

    val file = File(inputArgs.path)
    val bufferedImage = ImageIO.read(file)

    Asciificator().processImage(bufferedImage, inputArgs)

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
    val colored = get(COLORED) ?: error("Colored (-colored) argument not specified!")
    val outFormat = get(OUT_FORMAT) ?: error("Out format (-format) argument not specified!")
    val outPath = get(OUT_PATH) ?: error("Out path (-output) argument not specified!")
    val fontSize = get(FONT_SIZE) ?: error("Font size (-fontSize) argument not specified!")
    val scale = get(SCALE_SYMBOLS_FIT) ?: error("Scale (-scale) argument not specified!")


    return InputArgs(
        path = path,
        symbolToPixelAreaRatio = symbolToPixelAreaRatio.toInt(),
        fontSize = fontSize.toInt(),
        colored = colored.toBoolean(),
        outFormat = outFormat,
        outPath = outPath,
        scale = scale.toBoolean(),
    )
}

//private fun benchmarkDifferentArgs(bufferedImage: BufferedImage) {
//    val differentArgsList = listOf(
//        InputArgs(path = "C:\\amogus2.jpg", symbolToPixelAreaRatio = 2, colored = true, outFormat = "jpg", outPath = "C:\\1.jpg"),
//        InputArgs(path = "C:\\amogus2.jpg", symbolToPixelAreaRatio = 4, colored = true, outFormat = "jpg", outPath = "C:\\1.jpg"),
//        InputArgs(path = "C:\\amogus2.jpg", symbolToPixelAreaRatio = 8, colored = true, outFormat = "jpg", outPath = "C:\\1.jpg"),
//        InputArgs(path = "C:\\amogus2.jpg", symbolToPixelAreaRatio = 16, colored = true, outFormat = "jpg", outPath = "C:\\1.jpg"),
//        InputArgs(path = "C:\\amogus2.jpg", symbolToPixelAreaRatio = 32, colored = true, outFormat = "jpg", outPath = "C:\\1.jpg"),
//        InputArgs(path = "C:\\amogus2.jpg", symbolToPixelAreaRatio = 64, colored = true, outFormat = "jpg", outPath = "C:\\1.jpg"),
//    )
//
//    val asciificator = Asciificator()
//    differentArgsList.forEachIndexed { index, inputArgs ->
//        println("\n************** test $index, ratio ${inputArgs.symbolToPixelAreaRatio} **************\n")
//
//        println("-------------- BY_AREA --------------\n")
//        asciificator.testProcessImage(bufferedImage, inputArgs.symbolToPixelAreaRatio, WorkDistributionType.BY_AREA)
//
//        println("\n-------------- BY_COLUMN--------------\n")
//        asciificator.testProcessImage(bufferedImage, inputArgs.symbolToPixelAreaRatio, WorkDistributionType.BY_COLUMN)
//    }
//}
