package no.nav.exceltopdf.fileconversion

interface FileToPdfConverter {

    /**
     * Accepts a ByteArray representatino of an Excel file and returns a {@code ByteArray} representation of
     * the resulting PDF file.
     *
     * @param source
     *        A ByteArray representation of the Excel file that should be converted
     * @return A ByteArray representation of the resulting PDF file
     */
    fun convertExcelToPdf(source: ByteArray): ByteArray
}
