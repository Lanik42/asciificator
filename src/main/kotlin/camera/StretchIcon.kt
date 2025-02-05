package camera

import java.awt.Component
import java.awt.Container
import java.awt.Graphics
import java.awt.Image
import java.net.URL
import javax.swing.ImageIcon

/**
 * @(#)StretchIcon.java	1.0 03/27/12
 */
//package darrylbu.icon;

/**
 * An <CODE>Icon</CODE> that scales its image to fill the component area,
 * excluding any border or insets, optionally maintaining the image's aspect
 * ratio by padding and centering the scaled image horizontally or vertically.
 * <P>
 * The class is a drop-in replacement for <CODE>ImageIcon</CODE>, except that
 * the no-argument constructor is not supported.
</P> * <P>
 * As the size of the Icon is determined by the size of the component in which
 * it is displayed, <CODE>StretchIcon</CODE> must only be used in conjunction
 * with a component and layout that does not depend on the size of the
 * component's Icon.
 *
 * @version 1.0 03/27/12
 * @author Darryl
</P> */
class StretchIcon : ImageIcon {
    /**
     * Determines whether the aspect ratio of the image is maintained.
     * Set to `false` to allow th image to distort to fill the component.
     */
    private var proportionate: Boolean = true

    /**
     * Creates a <CODE>StretchIcon</CODE> from an array of bytes.
     *
     * @param  imageData an array of pixels in an image format supported by
     * the AWT Toolkit, such as GIF, JPEG, or (as of 1.3) PNG
     *
     * @see ImageIcon.ImageIcon
     */
    constructor(imageData: ByteArray?) : super(imageData)

    /**
     * Creates a <CODE>StretchIcon</CODE> from an array of bytes with the specified behavior.
     *
     * @param  imageData an array of pixels in an image format supported by
     * the AWT Toolkit, such as GIF, JPEG, or (as of 1.3) PNG
     * @param proportionate `true` to retain the image's aspect ratio,
     * `false` to allow distortion of the image to fill the
     * component.
     *
     * @see ImageIcon.ImageIcon
     */
    constructor(imageData: ByteArray?, proportionate: Boolean) : super(imageData) {
        this.proportionate = proportionate
    }

    /**
     * Creates a <CODE>StretchIcon</CODE> from an array of bytes.
     *
     * @param  imageData an array of pixels in an image format supported by
     * the AWT Toolkit, such as GIF, JPEG, or (as of 1.3) PNG
     * @param  description a brief textual description of the image
     *
     * @see ImageIcon.ImageIcon
     */
    constructor(imageData: ByteArray?, description: String?) : super(imageData, description)

    /**
     * Creates a <CODE>StretchIcon</CODE> from an array of bytes with the specified behavior.
     *
     * @see ImageIcon.ImageIcon
     * @param  imageData an array of pixels in an image format supported by
     * the AWT Toolkit, such as GIF, JPEG, or (as of 1.3) PNG
     * @param  description a brief textual description of the image
     * @param proportionate `true` to retain the image's aspect ratio,
     * `false` to allow distortion of the image to fill the
     * component.
     *
     * @see ImageIcon.ImageIcon
     */
    constructor(imageData: ByteArray?, description: String?, proportionate: Boolean) : super(
        imageData,
        description
    ) {
        this.proportionate = proportionate
    }

    /**
     * Creates a <CODE>StretchIcon</CODE> from the image.
     *
     * @param image the image
     *
     * @see ImageIcon.ImageIcon
     */
    constructor(image: Image?) : super(image)

    /**
     * Creates a <CODE>StretchIcon</CODE> from the image with the specified behavior.
     *
     * @param image the image
     * @param proportionate `true` to retain the image's aspect ratio,
     * `false` to allow distortion of the image to fill the
     * component.
     *
     * @see ImageIcon.ImageIcon
     */
    constructor(image: Image?, proportionate: Boolean) : super(image) {
        this.proportionate = proportionate
    }

    /**
     * Creates a <CODE>StretchIcon</CODE> from the image.
     *
     * @param image the image
     * @param  description a brief textual description of the image
     *
     * @see ImageIcon.ImageIcon
     */
    constructor(image: Image?, description: String?) : super(image, description)

    /**
     * Creates a <CODE>StretchIcon</CODE> from the image with the specified behavior.
     *
     * @param image the image
     * @param  description a brief textual description of the image
     * @param proportionate `true` to retain the image's aspect ratio,
     * `false` to allow distortion of the image to fill the
     * component.
     *
     * @see ImageIcon.ImageIcon
     */
    constructor(image: Image?, description: String?, proportionate: Boolean) : super(
        image,
        description
    ) {
        this.proportionate = proportionate
    }

    /**
     * Creates a <CODE>StretchIcon</CODE> from the specified file.
     *
     * @param filename a String specifying a filename or path
     *
     * @see ImageIcon.ImageIcon
     */
    constructor(filename: String?) : super(filename)

    /**
     * Creates a <CODE>StretchIcon</CODE> from the specified file with the specified behavior.
     *
     * @param filename a String specifying a filename or path
     * @param proportionate `true` to retain the image's aspect ratio,
     * `false` to allow distortion of the image to fill the
     * component.
     *
     * @see ImageIcon.ImageIcon
     */
    constructor(filename: String?, proportionate: Boolean) : super(filename) {
        this.proportionate = proportionate
    }

    /**
     * Creates a <CODE>StretchIcon</CODE> from the specified file.
     *
     * @param filename a String specifying a filename or path
     * @param  description a brief textual description of the image
     *
     * @see ImageIcon.ImageIcon
     */
    constructor(filename: String?, description: String?) : super(filename, description)

    /**
     * Creates a <CODE>StretchIcon</CODE> from the specified file with the specified behavior.
     *
     * @param filename a String specifying a filename or path
     * @param  description a brief textual description of the image
     * @param proportionate `true` to retain the image's aspect ratio,
     * `false` to allow distortion of the image to fill the
     * component.
     *
     * @see ImageIcon.ImageIcon
     */
    constructor(filename: String?, description: String?, proportionate: Boolean) : super(
        filename,
        description
    ) {
        this.proportionate = proportionate
    }

    /**
     * Creates a <CODE>StretchIcon</CODE> from the specified URL.
     *
     * @param location the URL for the image
     *
     * @see ImageIcon.ImageIcon
     */
    constructor(location: URL?) : super(location)

    /**
     * Creates a <CODE>StretchIcon</CODE> from the specified URL with the specified behavior.
     *
     * @param location the URL for the image
     * @param proportionate `true` to retain the image's aspect ratio,
     * `false` to allow distortion of the image to fill the
     * component.
     *
     * @see ImageIcon.ImageIcon
     */
    constructor(location: URL?, proportionate: Boolean) : super(location) {
        this.proportionate = proportionate
    }

    /**
     * Creates a <CODE>StretchIcon</CODE> from the specified URL.
     *
     * @param location the URL for the image
     * @param  description a brief textual description of the image
     *
     * @see ImageIcon.ImageIcon
     */
    constructor(location: URL?, description: String?) : super(location, description)

    /**
     * Creates a <CODE>StretchIcon</CODE> from the specified URL with the specified behavior.
     *
     * @param location the URL for the image
     * @param  description a brief textual description of the image
     * @param proportionate `true` to retain the image's aspect ratio,
     * `false` to allow distortion of the image to fill the
     * component.
     *
     * @see ImageIcon.ImageIcon
     */
    constructor(location: URL?, description: String?, proportionate: Boolean) : super(
        location,
        description
    ) {
        this.proportionate = proportionate
    }

    /**
     * Paints the icon.  The image is reduced or magnified to fit the component to which
     * it is painted.
     * <P>
     * If the proportion has not been specified, or has been specified as `true`,
     * the aspect ratio of the image will be preserved by padding and centering the image
     * horizontally or vertically.  Otherwise the image may be distorted to fill the
     * component it is painted to.
    </P> * <P>
     * If this icon has no image observer,this method uses the `c` component
     * as the observer.
     *
     * @param c the component to which the Icon is painted.  This is used as the
     * observer if this icon has no image observer
     * @param g the graphics context
     * @param x not used.
     * @param y not used.
     *
     * @see ImageIcon.paintIcon
    </P> */
    @Synchronized
    override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
        var x = x
        var y = y
        val image = image ?: return
        val insets = (c as Container).insets
        x = insets.left
        y = insets.top

        var w = c.getWidth() - x - insets.right
        var h = c.getHeight() - y - insets.bottom

        if (proportionate) {
            var iw = image.getWidth(c)
            var ih = image.getHeight(c)

            if (iw * h < ih * w) {
                iw = (h * iw) / ih
                x += (w - iw) / 2
                w = iw
            } else {
                ih = (w * ih) / iw
                y += (h - ih) / 2
                h = ih
            }
        }

        val io = imageObserver
        g.drawImage(image, x, y, w, h, io ?: c)
    }

    /**
     * Overridden to return 0.  The size of this Icon is determined by
     * the size of the component.
     *
     * @return 0
     */
    override fun getIconWidth(): Int {
        return 0
    }

    /**
     * Overridden to return 0.  The size of this Icon is determined by
     * the size of the component.
     *
     * @return 0
     */
    override fun getIconHeight(): Int {
        return 0
    }
}