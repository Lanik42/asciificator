package workdistribution

import Size
import workdistribution.area.AreaInputData

abstract class ThreadWorkDistributor(
    protected val symbolToPixelAreaRatio: Int,
    imageSize: Size,
) {

   protected val extraRightPixels = imageSize.width % symbolToPixelAreaRatio
   protected val extraBottomPixels = imageSize.height % symbolToPixelAreaRatio

    protected val symbolsPerXDimension = imageSize.width / symbolToPixelAreaRatio
    protected val symbolsPerYDimension = imageSize.height / symbolToPixelAreaRatio

    abstract fun getThreadInputData2DArray(): Array<Array<AreaInputData?>>
}