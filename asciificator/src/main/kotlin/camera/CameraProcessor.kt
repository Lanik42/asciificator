package camera

import Asciificator
import InputArgs
import com.github.sarxos.webcam.Webcam
import workdistribution.core.ThreadManager
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JLabel

class CameraProcessor(inputArgs: InputArgs) {

    private var videoCaptureInProgress = false

    private val asciificator = Asciificator(inputArgs, WorkType.REALTIME)
    private val frame: JFrame = JFrame()
    private val label: JLabel = JLabel()

    fun start(inputArgs: InputArgs) {
        videoCaptureInProgress = true

        frame.apply {
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            setLocationRelativeTo(null)
            size = Dimension(1280, 720)
            preferredSize = Dimension(1280, 720)
            contentPane?.add(label)
            isVisible = true
        }

        val webcam = Webcam.getDefault()
        webcam.open(true)

        ThreadManager.executors.submit {
            var firstFrame = true

            while (videoCaptureInProgress) {
                val start = System.currentTimeMillis()

                label.icon = StretchIcon(asciificator.processImage(webcam.image))

                if (firstFrame) {
                    frame.pack()
                    firstFrame = false
                }

                val end = System.currentTimeMillis()
                // 30 fps
                if ((end - start) < 33) {
                    Thread.sleep(33 - (end - start))
                }
            }
        }
    }
}