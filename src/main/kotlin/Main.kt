import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfInt
import org.opencv.imgcodecs.Imgcodecs
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.io.IOException
import java.util.concurrent.Future
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
// 7. PRIORITY! Выпилить все List ради оптимизации

// КОРУТИНЫ ГОВНО!!!!! 40мс на иницаилизацию!!!!! Тред пул на 6 потоков инициализируется за 2мс!!!!! Выпилить корутины!!!

fun main(args: Array<String>) {
    initOpenCV()

    val inputArgs = args.parse()

    measureTimeMillis("overall") {
        runProcessing(inputArgs)
    }

    exitProcess(0)
}

private fun runProcessing(inputArgs: InputArgs) {

    val video = true
    if (video) {
        VideoProcessor.processVideo2(inputArgs)
    } else {
        val file = File(inputArgs.path)
        val bufferedImage = ImageIO.read(file)

        val asciiImage = Asciificator().processImage(bufferedImage, inputArgs.copy(outPath = inputArgs.outPath))
        measureTimeMillis("write") {
            writeImageCV(asciiImage, inputArgs.outPath)
        }
    }
}

// opencv x2 к скорости записи, имба не контрится, конвертация в mat занимает максимум 3мс времени
private fun writeImageCV(image: BufferedImage, path: String) {
    try {
        val qualityParams = MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 60)
        if (image.type == BufferedImage.TYPE_BYTE_GRAY) {
            Imgcodecs.imwrite("$path.jpg", image.toMat(CvType.CV_8UC1), qualityParams)
        } else {
            Imgcodecs.imwrite("$path.jpg", image.toMat(CvType.CV_8UC3), qualityParams)
        }
    } catch (ex: IOException) {
        ex.printStackTrace()
    }
}

fun BufferedImage.toMat(type: Int): Mat {
    val mat = Mat(height, width, type)
    val pixels = (raster.dataBuffer as DataBufferByte).data
    return mat.apply { put(0, 0, pixels) }
}

private fun <T> List<Future<T>>.awaitAllWithResult(): List<T> =
    map { it.get() }

fun <T> List<Future<T>>.awaitAll() {
    forEach { it.get() }
}

private fun initOpenCV() {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
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
