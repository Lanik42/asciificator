package threads

import Size
import brightness.ThreadData

abstract class ThreadWorkDistributor(
    protected val symbolToPixelAreaRatio: Int,
    protected val imageSize: Size,
) {

   protected val extraRightPixels = imageSize.width % symbolToPixelAreaRatio
   protected val extraBottomPixels = imageSize.height % symbolToPixelAreaRatio

    protected val areasPerXDimension = (imageSize.width / symbolToPixelAreaRatio).apply {
        if (extraRightPixels != 0) this + 1
    }
    protected val areasPerYDimension = (imageSize.height / symbolToPixelAreaRatio).apply {
        if (extraBottomPixels != 0) this + 1
    }

    abstract fun getThreadData2DArray(): Array<Array<ThreadData?>>
}