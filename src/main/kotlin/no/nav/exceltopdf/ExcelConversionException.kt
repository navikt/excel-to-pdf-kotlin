package no.nav.exceltopdf

class ExcelConversionException(
    override val message: String?,
    override val cause: Throwable?,
) : RuntimeException(message, cause)
