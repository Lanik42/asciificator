import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.internal.kernel.KernelManager;

import java.awt.image.BufferedImage;


public final class SimpleGpuCalculator {
    private static final int WARP_AREA = 32;
    private static final int WARP_HEIGHT = 4;
    private static final int WARP_WIDTH = 8;

    public void run(BufferedImage bufferedImage, int symbolToPixelAreaRatio) {

        long start = System.currentTimeMillis();

        int[][] colors = getPixelColors(bufferedImage); // multithread this

        long end = System.currentTimeMillis();
        System.out.println("pixel color fetch took " + (end - start));

        //1920x1080 -> 480x270, 4x4

        start = System.currentTimeMillis();

        MyKernel2 kernel = new MyKernel2();
        kernel.setColors(colors);
        kernel.initialize(bufferedImage.getHeight() / 96, bufferedImage.getWidth() / 96);
        kernel.singleAreaSize = symbolToPixelAreaRatio * symbolToPixelAreaRatio;

        KernelManager km = KernelManager.instance();
        Device gpu = km.bestDevice();
        Range range = gpu.createRange2D(bufferedImage.getHeight() / 96, bufferedImage.getWidth() / 96, WARP_WIDTH, WARP_HEIGHT);

        kernel.execute(range);

        end = System.currentTimeMillis();
        System.out.println("gpu process took " + (end - start));
    }

//    public void run(AreaInputData[][] threadData2DArray, BufferedImage bufferedImage, int symbolToPixelAreaRatio) {
//
//
//        int[][] colors = getPixelColors(bufferedImage); // multithread this
//        int yAreaAmount = bufferedImage.getHeight() / threadData2DArray[0][0].getAreaSize().getHeight();
//        int xAreaAmount = bufferedImage.getWidth() / threadData2DArray[0][0].getAreaSize().getWidth();
//        //1920x1080 -> 480x270, 4x4
//
//        int warpsXDimension = xAreaAmount / WARP_WIDTH;
//        int warpsYDimension = yAreaAmount / WARP_HEIGHT;
//
//
//        long start = System.currentTimeMillis();
//
//        MyKernel kernel = new MyKernel();
//        kernel.setColors(new int[yAreaAmount][xAreaAmount]);
//        kernel.initialize(yAreaAmount, xAreaAmount);
//        kernel.singleAreaSize = symbolToPixelAreaRatio * symbolToPixelAreaRatio;
//
//        KernelManager km = KernelManager.instance();
//        Device gpu = km.bestDevice();
//        Range range = gpu.createRange2D(xAreaAmount, yAreaAmount, WARP_WIDTH, WARP_HEIGHT);
//
//        kernel.execute(range);
//
//        long end = System.currentTimeMillis();
//        System.out.println(end - start);
//    }

    private int[][] getPixelColors(BufferedImage bufferedImage) {
        int[][] colors = new int[bufferedImage.getHeight()][bufferedImage.getWidth()];

        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            bufferedImage.getRGB(0, y, bufferedImage.getWidth(), 1, colors[y], 0, 0);

        }

        return colors;
    }

}

final class MyKernel2 extends Kernel {

    int[][] colors = null;
    float[][] outBrightness = null;
    int[][] outRArray = null;
    int[][] outGArray = null;
    int[][] outBArray = null;
    int singleAreaSize = 0;

    @Override
    public void run() {
        int xThread = getGlobalId(0);
        int yThread = getGlobalId(1);

        int color = colors[yThread][xThread];

        int outR = color << 16 & 0xFF;
        int outG = color << 8 & 0xFF;
        int outB = color & 0xFF;

        outRArray[yThread][xThread] = outR;
        outGArray[yThread][xThread] = outG;
        outBArray[yThread][xThread] = outB;
        outBrightness[yThread][xThread] = (outR * 0.2126f + outG * 0.7152f + outB * 0.0722f) / 255;
    }

//    @Override
//    public void run() {
//        int xThread = getGlobalId(0);
//        int yThread = getGlobalId(1);

//        int outR = 0;
//        int outG = 0;
//        int outB = 0;

//        for (int y = 0; y < size; y++) {
//            for (int x = 0; x < size; x++) {
//                int color = colors[y + yThread][x + xThread];
//
//                outR += color << 16 & 0xFF;
//                outG += color << 8 & 0xFF;
//                outB += color & 0xFF;
//            }
//        }
//        int area = singleAreaSize * singleAreaSize;
//        outRArray[yThread][xThread] = outR / area;
//        outGArray[yThread][xThread] = outG / area;
//        outBArray[yThread][xThread] = outB / area;
//        outBrightness[yThread][xThread] = (outR * 0.2126f + outG * 0.7152f + outB * 0.0722f) / (area * 255);
//    }

    public void setColors(int[][] colors) {
        this.colors = colors;
    }

    public void initialize(int yAreaAmount, int xAreaAmount) {
        outRArray = new int[yAreaAmount][xAreaAmount];
        outGArray = new int[yAreaAmount][xAreaAmount];
        outBArray = new int[yAreaAmount][xAreaAmount];
        outBrightness = new float[yAreaAmount][xAreaAmount];
    }
}