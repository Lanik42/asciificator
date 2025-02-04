import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.internal.kernel.KernelManager;

import java.awt.image.BufferedImage;

public final class CoolestGpuSingleKernelCalculator {
    private static final int WARP_AREA = 32;
    private static final int WARP_HEIGHT = 4;
    private static final int WARP_WIDTH = 8;

    private static final CoolestKernel kernel = new CoolestKernel();
    private static final KernelManager km = KernelManager.instance();
    private static final Device gpu = km.bestDevice();

    public CustomColor[][] run(BufferedImage bufferedImage, int symbolToPixelAreaRatio) {
        long start = System.currentTimeMillis();

        int[][] colors = getPixelColors(bufferedImage);

        long end = System.currentTimeMillis();
        System.out.println("pixel color fetch took " + (end - start));

        int yAreaAmount = bufferedImage.getHeight() / symbolToPixelAreaRatio;
        int xAreaAmount = bufferedImage.getWidth() / symbolToPixelAreaRatio;
        //1920x1080 -> 480x270, 4x4

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

        kernel.execute(range);

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