package no.nav.exceltopdf.fileconversion.exception

abstract class FileConversionException(
    override val message: String?,
    override val cause: Throwable?
) : RuntimeException(message, cause)

class ExcelConversionException(
    override val message: String?,
    override val cause: Throwable?
) : FileConversionException(message, cause)
