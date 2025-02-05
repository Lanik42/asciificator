import brightness.calculator.cpu.CoreCpuBrightnessCalculator
import org.bytedeco.javacpp.opencv_core
import org.bytedeco.javacpp.opencv_videoio
import org.bytedeco.javacv.Java2DFrameConverter
import org.bytedeco.javacv.OpenCVFrameConverter
import org.opencv.core.CvType
import org.opencv.videoio.Videoio
import workdistribution.core.ThreadManager
import java.awt.image.BufferedImage
import java.io.File
import java.util.Collections
import java.util.concurrent.Future

object VideoProcessor {

    var FRAME_SIZE: opencv_core.Size = opencv_core.Size(-1, -1)
    var CV_TYPE: Int = -1

    // Подобрано эмпирически
    private var BLOCK_SIZE = 20

    private val asciificator = Asciificator()

    fun processVideo(inputArgs: InputArgs) {
        printDebug(inputArgs)

        opencv(inputArgs)

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

    private fun opencv(inputArgs: InputArgs) {
        val preVideoCapture = opencv_videoio.VideoCapture(inputArgs.path)
        val frameCount = preVideoCapture.get(Videoio.CAP_PROP_FRAME_COUNT)
        val fps = preVideoCapture.get(Videoio.CAP_PROP_FPS)
        FRAME_SIZE = getAsciiFrameSize(inputArgs, preVideoCapture)
        CV_TYPE = if (inputArgs.colored) {
            CvType.CV_8UC3
        } else {
            CvType.CV_8UC1
        }
        preVideoCapture.release()

        val videoCaptureArray = Collections.synchronizedList(
            Array(ThreadManager.threadCount) { opencv_videoio.VideoCapture(inputArgs.path) }.toList()
        )

        val mat2DArray = Collections.synchronizedList(
            Array(ThreadManager.threadCount) {
                Array(BLOCK_SIZE) {
                    opencv_core.Mat(FRAME_SIZE, CV_TYPE)
                }
            }.toList()
        )

        measureTimeMillis("opencv") {
            val videoWriter = getVideoWriter(inputArgs, fps, FRAME_SIZE, inputArgs.colored)
            // 0 - 11, 12 - 23, ...
            for (frameBlockOffset in 0 until frameCount.toInt() step ThreadManager.threadCount * BLOCK_SIZE) {
                val futureMats = mutableListOf<Future<Array<opencv_core.Mat>>>()
                println("Progress $frameBlockOffset / $frameCount")

                measureTimeMillis("time") {
                    repeat(ThreadManager.threadCount) { threadIndex ->
                        ThreadManager.executors.submit<Array<opencv_core.Mat>> {
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
                            getAsciiFrames(frames, inputArgs)
                        }.also {
                            futureMats.add(threadIndex, it)
                        }
                    }

                    futureMats.forEach {
                        it.get().forEach { mat ->
                            videoWriter.write(mat)
                        }
                    }
                }
            }

            videoWriter.release()
            videoCaptureArray.forEach {
                it.release()
            }
        }
    }

    private fun getFrames(
        frameOffset: Int,
        videoCapture: opencv_videoio.VideoCapture,
        frameArray: Array<opencv_core.Mat>
    ) {
        videoCapture.set(Videoio.CAP_PROP_POS_FRAMES, frameOffset.toDouble())
        frameArray.forEach(videoCapture::read)
    }

    private fun getAsciiFrames(
        frameArray: Array<opencv_core.Mat>,
        inputArgs: InputArgs,
    ): Array<opencv_core.Mat> {
        val bench = CoreCpuBrightnessCalculator.Bench()
        measureTimeMillis("${frameArray.size} frames process") {
            frameArray.forEachIndexed { index, frame ->
                try {
                    frameArray[index] =
                        asciificator.processImage(frame.toBufferedImage(), inputArgs, bench)
                            .toMatBytedeco(CV_TYPE)
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
        frameSize: opencv_core.Size,
        colored: Boolean
    ): opencv_videoio.VideoWriter =
        opencv_videoio.VideoWriter(
            getOutputVideoName(inputArgs),
            opencv_videoio.VideoWriter.fourcc(
                'H'.code.toByte(),
                '2'.code.toByte(),
                '6'.code.toByte(),
                '4'.code.toByte()
            ),
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

    private fun getAsciiFrameSize(
        inputArgs: InputArgs,
        videoCapture: opencv_videoio.VideoCapture
    ): opencv_core.Size {
        val frame = opencv_core.Mat()
        videoCapture.read(frame)
        val asciiImage = Asciificator().processImage(frame.toBufferedImage(), inputArgs)

        return opencv_core.Size(opencv_core.Point(asciiImage.width, asciiImage.height))
    }

    private fun opencv_core.Mat.toBufferedImage(): BufferedImage {
        return Java2DFrameConverter().convert(OpenCVFrameConverter.ToMat().convert(this))
    }

//    private fun Mat.toBufferedImage(): BufferedImage {
//        val outputMat = MatOfByte()
//        Imgcodecs.imencode(".jpg", this, outputMat)
//        return ImageIO.read(ByteArrayInputStream(outputMat.toArray()))
//    }

    private fun printDebug(inputArgs: InputArgs) {
        println(
            "ffmpeg -i \"${getOutputVideoName(inputArgs)}\" " +
                    "-i \"${inputArgs.path}\" " +
                    "-c:v copy -map 0:v:0 -map 1:a:0 " +
                    "\"${getOutputVideoName(inputArgs, " ff")}\""
        )
        println(
            "ffmpeg -i \"${getOutputVideoName(inputArgs, " ff")}\" " +
                    "-maxrate 12M " +
                    "-minrate 2M " +
                    "-bufsize 6M " +
                    "-vf \"crop=trunc(iw/2)*2:trunc(ih/2)*2\" " +
                    "\"${getOutputVideoName(inputArgs, " ff+bitrate")}\""
        )
    }
}