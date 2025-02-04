import brightness.calculator.cpu.CoreCpuBrightnessCalculator
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.VideoWriter
import org.opencv.videoio.Videoio
import workdistribution.core.ThreadManager
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.util.Collections
import java.util.concurrent.Future
import javax.imageio.ImageIO

object VideoProcessor {

    // Подобрано эмпирически
    private var BLOCK_SIZE = 20

    // Нужны ли аскификаторы по количеству потоков? - сейчас кол-во потоков обрабатывает реализация в GpuColorCalculator
    private val asciificator = Asciificator()

    fun processVideo2(inputArgs: InputArgs) {
        opencvV1(inputArgs)

        Runtime.getRuntime().exec(
            "ffmpeg -i \"${getOutputVideoName(inputArgs)}\" " +
                    "-i \"${inputArgs.path}\" " +
                    "-c:v copy -map 0:v:0 -map 1:a:0 " +
                    "\"${getOutputVideoName(inputArgs, " ff")}\""
        )

        println("avg paint time: ${Asciificator.paintTime.toDouble() / 1000000 / Asciificator.frameCount}ms")
        Asciificator.paintTime = 0
        Asciificator.frameCount = 0

        do {
            println("waiting")
            Thread.sleep(500)
        } while (File(getOutputVideoName(inputArgs, " ff")).totalSpace < 100000)
        // PRIORITY! научиться ждать окончания работы ффмпег

//        FFmpegLogCallback.set()
//        val frameGrabber = FFmpegFrameGrabber(getOutputVideoName(inputArgs, " ff"))
//        frameGrabber.start()
//        // Вытащить из записанного файла битрейт, и если он выше 12к- прогнать
//        if (frameGrabber.videoBitrate > 12000000 || frameGrabber.videoBitrate < 2000000) {
//            Runtime.getRuntime().exec(
//                "ffmpeg -i \"${getOutputVideoName(inputArgs, " ff")}\" " +
//                        "-maxrate 12M " +
//                        "-minrate 2M " +
//                        "-bufsize 6M " +
//                        "-vf \"crop=trunc(iw/2)*2:trunc(ih/2)*2\" " +
//                        "\"${getOutputVideoName(inputArgs, " ff+bitrate")}\""
//            )
//
//            do {
//                println("waiting")
//                Thread.sleep(500)
//            } while (File(getOutputVideoName(inputArgs, " ff+bitrate")).totalSpace < 100000)
//        }
//
//        frameGrabber.stop()
//        frameGrabber.release()
        //  File(getOutputVideoName(inputArgs)).delete()
    }

    private fun opencvV1(inputArgs: InputArgs) {
        println(
            "ffmpeg -i \"${getOutputVideoName(inputArgs, " ff")}\" " +
                    "-maxrate 12M " +
                    "-minrate 2M " +
                    "-bufsize 6M " +
                    "-vf \"crop=trunc(iw/2)*2:trunc(ih/2)*2\" " +
                    "\"${getOutputVideoName(inputArgs, " ff+bitrate")}\""
        )
        val preVideoCapture = VideoCapture(inputArgs.path)
        val frameCount = preVideoCapture.get(Videoio.CAP_PROP_FRAME_COUNT)
        val fps = preVideoCapture.get(Videoio.CAP_PROP_FPS)
        val depth = preVideoCapture.get(Videoio.CAP_PROP_XI_IMAGE_DATA_BIT_DEPTH)
        val frameSize = getAsciiFrameSize(inputArgs, preVideoCapture)
        val cvType = if (inputArgs.colored) {
            CvType.CV_8UC3
        } else {
            CvType.CV_8UC1
        }
        preVideoCapture.release()

        val videoCaptureArray = Collections.synchronizedList(
            Array(ThreadManager.threadCount) { VideoCapture(inputArgs.path) }.toList()
        )

        val mat2DArray = Collections.synchronizedList(
            Array(ThreadManager.threadCount) {
                Array(BLOCK_SIZE) {
                    Mat(frameSize, cvType)
                }
            }.toList()
        )

        measureTimeMillis("opencv") {
            val videoWriter = getVideoWriter(inputArgs, fps, frameSize, inputArgs.colored)
            // 0 - 11, 12 - 23, ...
            for (frameBlockOffset in 0 until frameCount.toInt() step ThreadManager.threadCount * BLOCK_SIZE) {
                val futureMats = mutableListOf<Future<Array<Mat>>>()
                println("Progress $frameBlockOffset / $frameCount")

                measureTimeMillis("time") {
                    repeat(ThreadManager.threadCount) { threadIndex ->
                        ThreadManager.executors.submit<Array<Mat>> {
                            val frameOffset = frameBlockOffset + threadIndex * BLOCK_SIZE
                            if (frameOffset + BLOCK_SIZE >= frameCount) {
                                synchronized(mat2DArray) {
                                    mat2DArray[threadIndex] = mat2DArray[threadIndex]
                                        .dropLast(frameOffset + BLOCK_SIZE % frameCount.toInt())
                                        .toTypedArray()
                                }
                            }

                            val videoCapture = videoCaptureArray[threadIndex]
                            val frames = mat2DArray[threadIndex]
                            getFrames(frameOffset, videoCapture, frames)
                            getAsciiFrames(frames, inputArgs, cvType)
                        }.also {
                            futureMats.add(threadIndex, it)
                        }
                    }

                    futureMats.forEach {
                        it.get().forEach(videoWriter::write)
                    }
                }
            }

            videoWriter.release()
            videoCaptureArray.forEach {
                it.release()
            }
        }
    }

    private fun getFrames(frameOffset: Int, videoCapture: VideoCapture, frameArray: Array<Mat>) {
        videoCapture.set(Videoio.CAP_PROP_POS_FRAMES, frameOffset.toDouble())
        frameArray.forEach(videoCapture::read)
    }

    private fun getAsciiFrames(
        frameArray: Array<Mat>,
        inputArgs: InputArgs,
        cvType: Int
    ): Array<Mat> {
        val bench = CoreCpuBrightnessCalculator.Bench()
        measureTimeMillis("${frameArray.size} frames process") {
            frameArray.forEachIndexed { index, frame ->
                try {
                    frameArray[index] =
                        asciificator.processImage(frame.toBufferedImage(), inputArgs, bench)
                            .toMat(cvType)
                } catch (e: Throwable) {
                    println(e.message)
                    if (e.message?.contains("unknown exception") == true) {
                        ""
                    }
                    // Починить багос, который возникает на последних кадрах, когда мы чуть переезжаем за
                    // общее число кадров в видео
                    // ПОХОЖЕ НЕ ТОЛЬКО НА ПОСЛЕДНИХ КАДРАХ, ВСЕ ОЧЕНЬ ПЛОХО, ЛОМАЕТСЯ ЖЕСТКО, ИГНОРИМ THROWABLE
                }
            }
        }
//        println(
//            "frame count: ${bench.frameCount}\n" +
//                    "avg fetch rgb time: ${bench.fetchRgbTime.toDouble() / bench.frameCount / 1000000}ms\n" +
//                    "avg brightness calc time: ${bench.brightnessCalcTime.toDouble() / bench.frameCount / 1000000}ms"
//        )

        return frameArray
    }

    private fun getVideoWriter(
        inputArgs: InputArgs,
        fps: Double,
        frameSize: Size,
        colored: Boolean
    ): VideoWriter =
        VideoWriter(
            getOutputVideoName(inputArgs),
            VideoWriter.fourcc('H', '2', '6', '4'),
            fps,
            frameSize,
            colored,
        )

    private fun getOutputVideoName(inputArgs: InputArgs, additionalString: String = ""): String {
        val outputDir = inputArgs.outPath.substringBeforeLast("\\")
        val fileName = inputArgs.path.substringBeforeLast(".").substringAfterLast("\\")
        val info = "(ratio ${inputArgs.symbolToPixelAreaRatio} + font ${inputArgs.fontSize})"

        return "$outputDir\\${fileName}_output$info$additionalString.mp4"
    }

    private fun getAsciiFrameSize(inputArgs: InputArgs, videoCapture: VideoCapture): Size {
        val frame = Mat()
        videoCapture.read(frame)
        val asciiImage = Asciificator().processImage(frame.toBufferedImage(), inputArgs)

        return Size(Point(asciiImage.width.toDouble(), asciiImage.height.toDouble()))
    }

    private fun Mat.toBufferedImage(): BufferedImage {
        val outputMat = MatOfByte()
        Imgcodecs.imencode(".jpg", this, outputMat)
        return ImageIO.read(ByteArrayInputStream(outputMat.toArray()))
    }
}