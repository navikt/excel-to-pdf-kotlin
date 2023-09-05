package no.nav.exceltopdf.filkonvertering

interface FilTilPdfConverter {

    /**
     * Returnerer et åpent PDDocument som er lagret til destination.
     * Dokumentet bør lukkes.
     */
    fun konverterTilPdf(source: ByteArray): ByteArray
}
