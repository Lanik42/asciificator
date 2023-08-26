import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.VideoWriter
import org.opencv.videoio.Videoio
import workdistribution.core.ThreadManager
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.util.concurrent.Future
import javax.imageio.ImageIO

object VideoProcessor {

    // Взято почти с потолка, с таким значением быстро идет прогон
    private const val BLOCK_SIZE = 10

    private var awaitTime = 0L
    private var writeTime = 0L
    private var runAmount = 0L

    fun processVideo2(inputArgs: InputArgs) {
        val videoCapture = VideoCapture(inputArgs.path)
        val frameCount = videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT)
        val fps = videoCapture.get(Videoio.CAP_PROP_FPS)
        val outputDir = inputArgs.outPath.substringBeforeLast("\\")
        videoCapture.release()

        val size = getFrameSize(inputArgs, outputDir)
        val videoWriter = getVideoWriter(outputDir, fps, size)
        val videoCaptureArray = Array(ThreadManager.threadCount) { VideoCapture(inputArgs.path) }

        val mat2DArray = Array<Array<Mat?>>(ThreadManager.threadCount) {
            Array(BLOCK_SIZE) {
                Mat(size, CvType.CV_8UC1)
            }
        }

        // 0 - 11, 12 - 23, ...
        for (frameBlockOffset in 0 until frameCount.toInt() step ThreadManager.threadCount * BLOCK_SIZE) {
            val futureMats = mutableListOf<Future<List<Mat?>>>()

            repeat(ThreadManager.threadCount) { threadIndex ->
                ThreadManager.executors.submit<List<Mat?>> {
                    getFrames(
                        frameBlockOffset + threadIndex * BLOCK_SIZE,
                        videoCaptureArray[threadIndex],
                        mat2DArray[threadIndex]
                    )
                    getAsciiFrames(mat2DArray[threadIndex], inputArgs)
                }.also { futureMats.add(it) }
            }

            futureMats.forEach {
                val (list, time) = measureTimeMillis("await time") {
                    it.get()
                }
                awaitTime += time
                writeTime += measureTimeMillis("await time") {
                    list?.forEach(videoWriter::write)
                }.second
            }
            runAmount++
        }
        videoWriter.release()

        videoCaptureArray.forEach { it.release() }
        println("Await time: $awaitTime, write time: $writeTime\nAverage await time: ${awaitTime / runAmount}, average write time: ${writeTime / runAmount}")
    }

    private fun getFrames(frameOffset: Int, videoCapture: VideoCapture, frame2DArray: Array<Mat?>) {
        videoCapture.set(Videoio.CAP_PROP_POS_FRAMES, frameOffset.toDouble())
        measureTimeMillis("read image time") {
            frame2DArray.forEach {
                try {
                    videoCapture.read(it)
                } catch (e: Exception) {
                    println()
                }
            }
        }
    }

    private fun getAsciiFrames(frame2DArray: Array<Mat?>, inputArgs: InputArgs): List<Mat?> {
        frame2DArray.forEachIndexed { index, frame ->
            val bufferedImage = frame?.toBufferedImage()

            if (bufferedImage == null) {
                frame2DArray[index] = null
            } else {
                frame2DArray[index] = Asciificator().processImage(bufferedImage, inputArgs.copy(colored = false))
                    .toMat(CvType.CV_8UC1)
            }
        }

        return measureTimeMillis("filter not null") { frame2DArray.filterNotNull() }.first
    }

    private fun getVideoWriter(outputDir: String, fps: Double, frameSize: Size): VideoWriter =
        VideoWriter(
            "$outputDir\\output.mp4",
            VideoWriter.fourcc('H', '2', '6', '4'),
            fps,
            frameSize,
            false
        )

    private fun getFrameSize(inputArgs: InputArgs, outputDir: String): Size {
        val imageFile = File(outputDir, "frame0.jpg")
        val bufferedImage = ImageIO.read(imageFile)
        val asciiImage = Asciificator().processImage(bufferedImage, inputArgs.copy(colored = false))

        return Size(Point(asciiImage.width.toDouble(), asciiImage.height.toDouble()))
    }

    private fun Mat.toBufferedImage(): BufferedImage? =
        try {
            val outputMat = MatOfByte()
            Imgcodecs.imencode(".jpg", this, outputMat)
            ImageIO.read(ByteArrayInputStream(outputMat.toArray()))
        } catch (e: Exception) {
            null
        }
}