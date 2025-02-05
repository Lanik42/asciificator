package camera

import Asciificator
import InputArgs
import com.github.sarxos.webcam.Webcam
import workdistribution.core.ThreadManager
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JLabel


object CameraEncoder {

    private var videoCaptureInProgress = false

    private val asciificator = Asciificator()

    fun start(inputArgs: InputArgs) {
        var first = true
        videoCaptureInProgress = true
        val frame = JFrame("Testing")
        val label = JLabel()

        ThreadManager.executors.submit {
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.setLocationRelativeTo(null)
            frame.size = Dimension(1280, 720)
            frame.preferredSize = Dimension(1280, 720)
            frame.contentPane.add(label)
            frame.isVisible = true
        }

        val webcam: Webcam = Webcam.getDefault()
        webcam.open(true)

        while (videoCaptureInProgress) {
            val start = System.currentTimeMillis()

            val image = asciificator.processImage(webcam.image, inputArgs)

            label.icon = StretchIcon(image)
            if (first) {
                frame.pack()
                first = false
            }

            val end = System.currentTimeMillis()
            // 30 fps
            if ((end - start) < 33) {
                Thread.sleep(33 - (end - start))
            }
        }
    }
}