package no.nav.exceltopdf.filkonvertering.exception

abstract class FilKonverteringException(
    override val message: String?,
    override val cause: Throwable?
) : RuntimeException(message, cause)

class ExcelKonverteringException(
    override val message: String?,
    override val cause: Throwable?
) : FilKonverteringException(message, cause)
