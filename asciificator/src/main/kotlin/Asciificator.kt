import brightness.calculator.CalculatorBench
import brightness.calculator.cpu.BrightnessCalculator
import brightness.calculator.cpu.ParallelBrightnessCalculator
import brightness.converter.BrightnessConverter
import org.bytedeco.javacpp.opencv_core
import org.bytedeco.javacpp.opencv_videoio
import paint.cpu.CpuTextPainter
import video.getAsciiFrameSize
import video.toBufferedImage
import java.awt.Font
import java.awt.image.BufferedImage

enum class WorkType {
    BATCH,
    REALTIME
}

class Asciificator(private val inputArgs: InputArgs, private val workType: WorkType) {

    private val font = Font(Font.MONOSPACED, Font.PLAIN, inputArgs.fontSize)  // меньше 10 шрифта все шакалится

    fun previewImage(frameTimeStamp: Int): BufferedImage {
        val videoCapture = opencv_videoio.VideoCapture(inputArgs.path)
        val fps = videoCapture.get(opencv_videoio.CAP_PROP_FPS)
        val frameSize = getAsciiFrameSize(inputArgs, videoCapture)
        val cvType = if (inputArgs.colored) {
            opencv_core.CV_8UC3
        } else {
            opencv_core.CV_8UC1
        }

        videoCapture.set(opencv_videoio.CAP_PROP_POS_FRAMES, frameTimeStamp * fps)
        val frame = opencv_core.Mat(frameSize, cvType)
        videoCapture.read(frame)

        return processImage(frame.toBufferedImage())
    }

    fun processImage(bufferedImage: BufferedImage, bench: CalculatorBench = CalculatorBench()): BufferedImage {
        val imageSize = CustomSize(bufferedImage.width, bufferedImage.height)

        val brightnessCalculator = when (workType) {
            WorkType.REALTIME -> ParallelBrightnessCalculator(imageSize, inputArgs.symbolToPixelAreaRatio)
            WorkType.BATCH -> BrightnessCalculator(imageSize, inputArgs.symbolToPixelAreaRatio)
        }
        val color2DList = brightnessCalculator.calculateBrightness(bufferedImage, bench)

        val char2DArray = measureTimeNanos("bright converter") {
            BrightnessConverter(inputArgs.colored).convertToSymbols(color2DList)
        }.first

        // Если мы сможем принимать, работать и выплевывать opencv_core.Mat, то это сэкономит значительно времени на конвертациях (и возможно обработке тоже)
        val a = measureTimeNanos("paint") {
            CpuTextPainter(font, inputArgs.symbolToPixelAreaRatio)
                .drawImage(char2DArray, color2DList, inputArgs.colored, inputArgs.scale)
        }

        return a.first
    }
}