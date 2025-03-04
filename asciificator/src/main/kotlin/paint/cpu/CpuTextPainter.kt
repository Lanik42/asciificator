package paint.cpu

import brightness.CustomColor
import measureTimeNanos
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.awt.Color as AwtColor

class CpuTextPainter(
    private val font: Font,
    private val symbolToPixelAreaRatio: Int,
) {

    fun drawImage(
        char2DArray: Array<CharArray>,
        color2DList: Array<Array<CustomColor>>,
        colored: Boolean,
        scaleSymbolsFit: Boolean
    ): BufferedImage {
        var graphics: Graphics2D = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).createGraphics()
        graphics.setupRender(font)

        val (charWidth, charHeight) = getCharSize(scaleSymbolsFit, graphics)
        val outputImage = getOutputImage(charWidth, charHeight, char2DArray.size - 1, char2DArray[0].size, colored)
        graphics.dispose()

        graphics = outputImage.createGraphics()
        graphics.setupRender(font)
        graphics.fill(Rectangle(char2DArray[0].size * charWidth, char2DArray.size * charHeight))

        measureTimeNanos("draw") {
            if (colored) {
                drawColored(graphics, char2DArray, color2DList, charHeight, charWidth)
            } else {
                draw(graphics, char2DArray, charHeight, charWidth)
            }
        }

        graphics.dispose()

        return outputImage
    }

    private fun getCharSize(scaleSymbolsFit: Boolean, graphics2D: Graphics2D): Pair<Int, Int> =
        if (scaleSymbolsFit) {
            graphics2D.fontMetrics.charWidth('B') to graphics2D.fontMetrics.ascent - graphics2D.fontMetrics.descent
        } else {
            symbolToPixelAreaRatio to symbolToPixelAreaRatio
        }

    private fun getOutputImage(
        charWidth: Int,
        charHeight: Int,
        linesAmount: Int,
        charAmount: Int,
        colored: Boolean
    ): BufferedImage {
        val outputImageWidth = charWidth * charAmount
        val outputImageHeight = charHeight * linesAmount

        return if (colored) {
            BufferedImage(outputImageWidth, outputImageHeight, BufferedImage.TYPE_3BYTE_BGR)
        } else {
            BufferedImage(outputImageWidth, outputImageHeight, BufferedImage.TYPE_BYTE_GRAY)
        }
    }

    private fun Graphics2D.setupRender(font: Font) {
        setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF)
        setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED)
        setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
        setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE)

        color = AwtColor.WHITE
        this.font = font
    }

    private fun drawColored(
        graphics2D: Graphics2D,
        char2DArray: Array<CharArray>,
        color2DList: Array<Array<CustomColor>>,
        charHeight: Int,
        charWidth: Int
    ) {
        char2DArray.forEachIndexed { yIndex, charArray ->
            charArray.forEachIndexed { xIndex, char ->
                val color = color2DList[yIndex][xIndex]
                graphics2D.color = AwtColor(color.r, color.g, color.b)

                // переписать на либу, которая умеет работать на GPU для отрисовки?
                graphics2D.drawString(char.toString(), xIndex * charWidth, charHeight * yIndex)
            }
        }
    }

    private fun draw(graphics2D: Graphics2D, char2DArray: Array<CharArray>, charHeight: Int, charWidth: Int) {
        graphics2D.color = AwtColor.BLACK

        char2DArray.forEachIndexed { yIndex, charArray ->
            charArray.forEachIndexed { xIndex, char ->
                graphics2D.drawString(char.toString(), xIndex * charWidth, charHeight * yIndex)
            }
        }
    }
}