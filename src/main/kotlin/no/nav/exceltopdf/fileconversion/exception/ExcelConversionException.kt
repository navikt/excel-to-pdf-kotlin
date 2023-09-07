package no.nav.exceltopdf.fileconversion.exception

class ExcelConversionException(
    override val message: String?,
    override val cause: Throwable?,
) : RuntimeException(message, cause)
