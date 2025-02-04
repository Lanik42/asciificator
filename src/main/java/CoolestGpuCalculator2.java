import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.internal.kernel.KernelManager;

import java.awt.image.BufferedImage;

import workdistribution.core.ThreadManager;

public final class CoolestGpuCalculator2 {
    private static final int WARP_AREA = 32;
    private static final int WARP_HEIGHT = 4;
    private static final int WARP_WIDTH = 8;

    private static final CoolestKernel[] kernels = new CoolestKernel[ThreadManager.INSTANCE.getThreadCount()];
    private static final KernelManager km = KernelManager.instance();
    private static final Device gpu = km.bestDevice();

    public CoolestGpuCalculator2() {
        for (int i = 0; i < kernels.length; i++) {
            kernels[i] = new CoolestKernel();
        }
    }

    public CustomColor[][] run(BufferedImage bufferedImage, int symbolToPixelAreaRatio, int threadIndex) {
        long start = System.currentTimeMillis();

        int[][] colors = getPixelColors(bufferedImage);

        long end = System.currentTimeMillis();
        System.out.println("pixel color fetch took " + (end - start));

        int yAreaAmount = bufferedImage.getHeight() / symbolToPixelAreaRatio;
        int xAreaAmount = bufferedImage.getWidth() / symbolToPixelAreaRatio;
        //1920x1080 -> 480x270, 4x4

        CoolestKernel kernel = kernels[threadIndex];
        kernel.setColors(colors);
        kernel.initialize(bufferedImage.getHeight(), bufferedImage.getWidth());
        kernel.areaHeight = symbolToPixelAreaRatio;
        kernel.areaWidth = symbolToPixelAreaRatio;
        kernel.invertedArea = 1f / (symbolToPixelAreaRatio * symbolToPixelAreaRatio);

        Range range = gpu.createRange2D(bufferedImage.getHeight(), bufferedImage.getWidth(), 1, 1);
        start = System.currentTimeMillis();

        kernel.execute(range);

        int area = symbolToPixelAreaRatio * symbolToPixelAreaRatio;
        CustomColor[][] symbolColors = new CustomColor[yAreaAmount][xAreaAmount];
        for (int y = 0; y < yAreaAmount; y++) {
            for (int x = 0; x < xAreaAmount; x++) {
                int r = 0;
                int g = 0;
                int b = 0;
                float brightness = 0;

                for (int i = 0; i < symbolToPixelAreaRatio; i++) {
                    for (int j = 0; j < symbolToPixelAreaRatio; j++) {
                        r += kernel.outRArray[y * symbolToPixelAreaRatio + i][x * symbolToPixelAreaRatio + j];
                        g += kernel.outGArray[y * symbolToPixelAreaRatio + i][x * symbolToPixelAreaRatio + j];
                        b += kernel.outBArray[y * symbolToPixelAreaRatio + i][x * symbolToPixelAreaRatio + j];
                        brightness += kernel.outBrightness[y * symbolToPixelAreaRatio + i][x * symbolToPixelAreaRatio + j];
                    }
                }

                symbolColors[y][x] = new CustomColor(r / area, g / area, b / area, brightness / area);
            }
        }
        end = System.currentTimeMillis();

        System.out.println("gpu process took " + (end - start));

        // Возможная оптимизация: выпилить эти зануления, чтобы каждый раз не создавать объекты
        kernel.colors = null;
        kernel.outRArray = null;
        kernel.outGArray = null;
        kernel.outBArray = null;
        kernel.outBrightness = null;

        return symbolColors;
    }

    private int[][] getPixelColors(BufferedImage bufferedImage) {
        int[][] colors = new int[bufferedImage.getHeight()][bufferedImage.getWidth()];

        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            bufferedImage.getRGB(0, y, bufferedImage.getWidth(), 1, colors[y], 0, 0);
        }

        return colors;
    }
}

final class CoolestKernel2 extends Kernel {

    int[][] colors = null;
    float[][] outBrightness = null;
    int[][] outRArray = null;
    int[][] outGArray = null;
    int[][] outBArray = null;
    int areaHeight = 0;
    int areaWidth = 0;
    float invertedArea = 0;

    @Override
    public void run() {
        int xBlock = getGlobalId(0);
        int yBlock = getGlobalId(1);
        int xThread = getLocalId(0);
        int yThread = getLocalId(1);

        int yAbsolutePosition = yBlock;
        int xAbsolutePosition = xBlock;

        int color = colors[yAbsolutePosition][xAbsolutePosition];

        int outR = (color & 0x00ff0000) >> 16;
        int outG = (color & 0x0000ff00) >> 8;
        int outB = color & 0x000000ff;

        outRArray[yAbsolutePosition][xAbsolutePosition] = (outR);
        outGArray[yAbsolutePosition][xAbsolutePosition] = (outG);
        outBArray[yAbsolutePosition][xAbsolutePosition] = (outB);
        outBrightness[yAbsolutePosition][xAbsolutePosition] = (outR * 0.2126f + outG * 0.7152f + outB * 0.0722f) / 255;
    }

    public void setColors(int[][] colors) {
        this.colors = colors.clone();
    }

    public void initialize(int yAreaAmount, int xAreaAmount) {
        outRArray = new int[yAreaAmount][xAreaAmount];
        outGArray = new int[yAreaAmount][xAreaAmount];
        outBArray = new int[yAreaAmount][xAreaAmount];
        outBrightness = new float[yAreaAmount][xAreaAmount];
    }
}
