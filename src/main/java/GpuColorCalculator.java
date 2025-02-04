import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.internal.kernel.KernelManager;

import java.awt.image.BufferedImage;

import workdistribution.core.ThreadManager;

public final class GpuColorCalculator {
    private static final int WARP_AREA = 32;
    private static final int WARP_HEIGHT = 4;
    private static final int WARP_WIDTH = 8;

    private static final CoolestKernel[] kernels = new CoolestKernel[ThreadManager.INSTANCE.getThreadCount()];
    private static final KernelManager km = KernelManager.instance();
    private static final Device gpu = km.bestDevice();

    public GpuColorCalculator() {
        for (int i = 0; i < kernels.length; i++) {
            kernels[i] = new CoolestKernel();
        }
    }

    // Aparapi срет непонятные ошибки, переписать на плюсах с использованием нативного opencl (JNA / JNI)
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
        kernel.initialize(yAreaAmount, xAreaAmount);
        kernel.areaHeight = symbolToPixelAreaRatio;
        kernel.areaWidth = symbolToPixelAreaRatio;
        kernel.invertedArea = 1f / (symbolToPixelAreaRatio * symbolToPixelAreaRatio);
        kernel.brightnessMultiplier = kernel.invertedArea / 255;

        // Нужно распределить всю работу на блоки независимо от xAreaAmount, yAreaAmount, symbolToPixelAreaRatio
        // Таким образом, чтобы globalWidth%localWidth == 0 && globalHeight%localHeight == 0 && localH*localW < 256
        // Сделать CoreGpuWorkDistributor? Нужно как-то понимать, сколько вообще можно выделить блоки на видюхе (учитывая n потоков)
        Range range = gpu.createRange2D(xAreaAmount, yAreaAmount, 1, 1);
        start = System.currentTimeMillis();

        boolean success = false;
        do {
            try {
                kernel.execute(range);
                success = true;
            } catch (Exception e) {
            }
        } while (!success);

        CustomColor[][] symbolColors = new CustomColor[yAreaAmount][xAreaAmount];
        for (int y = 0; y < yAreaAmount; y++) {
            for (int x = 0; x < xAreaAmount; x++) {
                symbolColors[y][x] = new CustomColor(
                        kernel.outRArray[y][x],
                        kernel.outGArray[y][x],
                        kernel.outBArray[y][x],
                        kernel.outBrightness[y][x]
                );
            }
        }
        end = System.currentTimeMillis();

        System.out.println("gpu process took " + (end - start));

//        kernel.colors = null;
//        kernel.outRArray = null;
//        kernel.outGArray = null;
//        kernel.outBArray = null;
//        kernel.outBrightness = null;
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

final class CoolestKernel extends Kernel {

    int[][] colors = null;
    float[][] outBrightness = null;
    int[][] outRArray = null;
    int[][] outGArray = null;
    int[][] outBArray = null;
    int areaHeight = 0;
    int areaWidth = 0;
    float invertedArea = 0;
    float brightnessMultiplier = 0;

    @Override
    public void run() {
        int xBlock = getGlobalId(0);
        int yBlock = getGlobalId(1);

        int yAbsoluteBlockPosition = yBlock * areaHeight;
        int xAbsoluteBlockPosition = xBlock * areaWidth;

        int r = 0;
        int g = 0;
        int b = 0;
        for (int i = 0; i < areaHeight; i++) {
            for (int j = 0; j < areaWidth; j++) {
                int color = colors[yAbsoluteBlockPosition + i][xAbsoluteBlockPosition + j];
                r += (color & 0x00ff0000) >> 16;
                g += (color & 0x0000ff00) >> 8;
                b += color & 0x000000ff;
            }
        }

        outRArray[yBlock][xBlock] = (int) (r * invertedArea);
        outGArray[yBlock][xBlock] = (int) (g * invertedArea);
        outBArray[yBlock][xBlock] = (int) (b * invertedArea);
        outBrightness[yBlock][xBlock] = (r * 0.2126f + g * 0.7152f + b * 0.0722f) * brightnessMultiplier;
    }

    public void setColors(int[][] colors) {
        this.colors = colors.clone();
    }

    public void initialize(int yAreaAmount, int xAreaAmount) {
        //  if (outBrightness == null) {
        outRArray = new int[yAreaAmount][xAreaAmount];
        outGArray = new int[yAreaAmount][xAreaAmount];
        outBArray = new int[yAreaAmount][xAreaAmount];
        outBrightness = new float[yAreaAmount][xAreaAmount];
        // }
    }
}