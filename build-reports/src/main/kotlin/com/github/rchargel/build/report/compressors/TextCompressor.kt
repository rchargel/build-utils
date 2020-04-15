package com.github.rchargel.build.report.compressors

import com.yahoo.platform.yui.compressor.CssCompressor
import com.yahoo.platform.yui.compressor.JavaScriptCompressor
import org.mozilla.javascript.ErrorReporter
import org.mozilla.javascript.EvaluatorException
import java.io.*

class TextCompressor(
        warningMessageConsumer: (str: String) -> Unit = { str -> println(str) },
        errorMessageConsumer: (str: String) -> Unit = { str -> println(str) }
) {
    private val errorReporter = CompressionErrorReporter(warningMessageConsumer, errorMessageConsumer)

    fun compressJavaScriptTo(inputStream: InputStream, outputStream: OutputStream) =
            compressJavaScriptTo(InputStreamReader(inputStream), OutputStreamWriter(outputStream))

    fun compressJavaScriptTo(reader: Reader, writer: Writer) =
            JavaScriptCompressor(reader, errorReporter).compress(writer, -1, true, false, false, false)

    fun compressJavaScript(input: String) =
            compressJavaScript(StringReader(input))

    fun compressJavaScript(inputStream: InputStream) =
            compressJavaScript(InputStreamReader(inputStream))

    fun compressJavaScript(reader: Reader): String {
        val writer = StringWriter()
        compressJavaScriptTo(reader, writer)
        return writer.toString()
    }

    fun compressCSSTo(inputStream: InputStream, outputStream: OutputStream) =
            compressCSSTo(InputStreamReader(inputStream), OutputStreamWriter(outputStream))

    fun compressCSSTo(reader: Reader, writer: Writer) =
            CssCompressor(reader).compress(writer, -1)

    fun compressCSS(input: String) = compressCSS(StringReader(input))

    fun compressCSS(inputStream: InputStream) = compressCSS(InputStreamReader(inputStream))

    fun compressCSS(reader: Reader): String {
        val writer = StringWriter()
        compressCSSTo(reader, writer)
        return writer.toString()
    }
}

private class CompressionErrorReporter(private val warnConsumer: (str: String) -> Unit, private val errorConsumer: (str: String) -> Unit) : ErrorReporter {
    override fun error(message: String?, sourceName: String?, line: Int, lineSource: String?, lineOffset: Int) {
        errorConsumer.invoke(toMessage(message.orEmpty(), sourceName.orEmpty(), line, lineSource.orEmpty(), lineOffset))
    }

    override fun warning(message: String?, sourceName: String?, line: Int, lineSource: String?, lineOffset: Int) {
        warnConsumer.invoke(toMessage(message.orEmpty(), sourceName.orEmpty(), line, lineSource.orEmpty(), lineOffset))
    }

    override fun runtimeError(message: String?, sourceName: String?, line: Int, lineSource: String?, lineOffset: Int): EvaluatorException {
        val message = toMessage(message.orEmpty(), sourceName.orEmpty(), line, lineSource.orEmpty(), lineOffset)
        errorConsumer.invoke(message)
        return EvaluatorException(message)
    }

    private fun toMessage(message: String, sourceName: String, line: Int, lineSource: String, lineOffset: Int) = if (line < 0) {
        "${sourceName}: ${message}"
    } else {
        "${sourceName}[${line}:${lineOffset}]: ${message}"
    }
}

