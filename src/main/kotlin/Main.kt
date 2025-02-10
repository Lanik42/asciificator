import camera.CameraProcessor
import org.bytedeco.javacpp.Loader
import org.bytedeco.javacpp.opencv_java
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfInt
import org.opencv.imgcodecs.Imgcodecs
import video.VideoProcessor
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

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
// 6. PRIORITY! Выпилить все List ради оптимизации
// 7. PRIORITY!!!! Сделать подгон symbolToPixelArea под формат видео / изображения, пофиксит кривой размер выходного результата
// или по крайней мере сделать разные параметры под ширину и высоту. То есть если на входе картинка 4:3 - мы считаем яркость не квадратной области, а прямоугольника
// с соотношеним 4:3
// 8. PRIORITY!!!! Избавиться от разделения на потоки при подсчете яркости для видео, тк обработка видео и так запускает обработку
// n (кол-во потоков) кадров одновременно, нет смысла для каждого кадра еще разделять работу на 12 потоков, в теории ухудшает производительность


// fontSize = 11 выглядит неплохо в большинстве случаев, но нужно тестить
fun main(args: Array<String>) {
    initOpenCV()

    val inputArgs = args.parse()

    measureTimeMillis("overall") {
        runProcessing(inputArgs)
    }
}

private val VIDEO_EXTENSIONS = listOf("mp4", "avi", "webm")

private fun runProcessing(inputArgs: InputArgs) {

    val video = inputArgs.path.substringAfterLast(".") in VIDEO_EXTENSIONS
    val camera = inputArgs.path == "camera"
    when {
        camera -> CameraProcessor.start(inputArgs)

        video -> VideoProcessor.processVideo(inputArgs)

        else -> {
            val file = File(inputArgs.path)
            val bufferedImage = ImageIO.read(file)

            val asciiImage = Asciificator().processImage(bufferedImage, inputArgs)
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

//fun BufferedImage.toMatBytedeco(): ByteDecoMat {
//
//}

private fun initOpenCV() {
    Loader.load(opencv_java::class.java)
//    Loader.load(cudnn::class.java)
    // System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
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
