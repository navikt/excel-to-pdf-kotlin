package no.nav.exceltopdf.util

import org.apache.commons.io.IOUtils
import org.apache.pdfbox.pdmodel.font.PDFont

object PdfFontUtil {

    private const val FONTS_PATH = "fonts"
    private const val DEFAULT_FAMILY = "calibri"
    private const val DEFAULT_FONT = "calibri.ttf"

    fun getDefaultFontBytes(): ByteArray {
        return getResource("$DEFAULT_FAMILY/$DEFAULT_FONT")
            ?: throw IllegalStateException("Default font calibri finnes ikke")
    }

    private fun getResource(path: String): ByteArray? {
        return javaClass.classLoader.getResourceAsStream("$FONTS_PATH/$path")
            ?.let { IOUtils.toByteArray(it) }
    }

    fun PDFont.breddeIPunkter(data: String, fontsize: Short): Float = getStringWidth(data) / 1000 * fontsize
}
