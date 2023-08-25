package paint

import Color
import measureTimeMillis
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.awt.Color as AwtColor


class TextPainter(
    private val font: Font,
    private val symbolToPixelAreaRatio: Int,
) {
    
    private companion object {

        const val SYMBOLS_SPACING_CORRECTION = -1
        const val LINE_HEIGHT_CORRECTION = -1
    }

    fun drawImage(char2DArray: Array<CharArray>, color2DList: List<Array<Color>>, colored: Boolean, scaleSymbolsFit: Boolean): BufferedImage {
        var graphics: Graphics2D = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).createGraphics().apply {
            this.font = font
        }

        val (charWidth, charHeight) = getCharSize(scaleSymbolsFit, graphics)
        val outputImage = getOutputImage(charWidth, charHeight, char2DArray.size - 1, char2DArray[0].size, colored)
        graphics.dispose()

        // graphics2d for each thread
        graphics = outputImage.createGraphics()
        graphics.setupRender(font, char2DArray[0].size * charWidth, char2DArray.size * charHeight)

        measureTimeMillis("draw") {
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
            graphics2D.fontMetrics.charWidth('@') + SYMBOLS_SPACING_CORRECTION to graphics2D.fontMetrics.ascent + LINE_HEIGHT_CORRECTION
        } else {
            symbolToPixelAreaRatio to symbolToPixelAreaRatio
        }

    private fun getOutputImage(charWidth: Int, lineHeight: Int, linesAmount: Int, charAmount: Int, colored: Boolean): BufferedImage {
        val outputImageWidth = charWidth * charAmount
        val outputImageHeight = lineHeight * linesAmount

        return if (colored) {
            BufferedImage(outputImageWidth, outputImageHeight, BufferedImage.TYPE_3BYTE_BGR)
        } else {
            BufferedImage(outputImageWidth, outputImageHeight, BufferedImage.TYPE_BYTE_GRAY)
        }
    }

    private fun Graphics2D.setupRender(font: Font, fillWidth: Int, fillHeight: Int) {
        setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)
        setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
        color = AwtColor.WHITE
        this.font = font
        fill(Rectangle(fillWidth, fillHeight))
    }

    private fun drawColored(
        graphics2D: Graphics2D,
        char2DArray: Array<CharArray>,
        color2DList: List<Array<Color>>,
        charHeight: Int,
        charWidth: Int
    ) {
        // А что если не менять цвет для каждого символа, а сначала рисовать все символы одного цвета, потом другого и тд?
        char2DArray.forEachIndexed { yIndex, charArray ->
            charArray.forEachIndexed { xIndex, char ->
                val color = color2DList[yIndex][xIndex]
                graphics2D.color = AwtColor(color.r, color.g, color.b)

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

        // Работало бы классно, если бы все символы monospace шрифта были бы равны charWidth (а они должны быть ему равны)
        // Но в реальности что-то идет не так и символы не имеют charWidth ширину
//        char2DArray.forEachIndexed { yIndex, charArray ->
//            graphics2D.drawChars(charArray, 0, charArray.size, 0, lineHeight * yIndex)
//        }
    }
}