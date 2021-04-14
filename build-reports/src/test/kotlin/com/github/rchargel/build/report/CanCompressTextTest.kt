package com.github.rchargel.build.report

import com.github.rchargel.build.report.compressors.TextCompressor
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mozilla.javascript.EvaluatorException
import java.io.StringWriter
import java.nio.charset.StandardCharsets.UTF_8

@RunWith(Parameterized::class)
class CanCompressTextTest(
        private val type: Type,
        private val input: String,
        private val expected: String,
        private val expectedException: Class<out java.lang.Exception>?
) {

    enum class Type { CSS, JAVASCRIPT }

    private val tc = TextCompressor()

    private fun validateAction(action: () -> Unit) {
        try {
            action.invoke()
            if (expectedException != null)
                assert(false) { "Should have thrown exception of type $expectedException" }
        } catch (e: Exception) {
            assert(e.javaClass == expectedException) {
                "Should have thrown exception of type $expectedException, but was ${e.javaClass}"
            }
        }
    }

    @Test
    fun canCompressText() = validateAction {
        val actual = when (type) {
            Type.CSS -> tc.compressCSS(input).trim()
            else -> tc.compressJavaScript(input).trim()
        }
        assert(actual == expected) {
            "Expected $expected, but was $actual"
        }
    }

    @Test
    fun canCompressReader() = validateAction {
        val actual = when (type) {
            Type.CSS -> tc.compressCSS(input.reader()).trim()
            else -> tc.compressJavaScript(input.reader()).trim()
        }
        assert(actual == expected) {
            "Expected $expected, but was $actual"
        }
    }

    @Test
    fun canCompressInputStream() = validateAction {
        val actual = when (type) {
            Type.CSS -> tc.compressCSS(input.byteInputStream(UTF_8)).trim()
            else -> tc.compressJavaScript(input.byteInputStream(UTF_8)).trim()
        }
        assert(actual == expected) {
            "Expected $expected, but was $actual"
        }
    }

    @Test
    fun canCompressReaderTo() = validateAction {
        val writer = StringWriter()
        writer.use {
            when (type) {
                Type.CSS -> tc.compressCSSTo(input.reader(), it)
                else -> tc.compressJavaScriptTo(input.reader(), it)
            }
        }
        val actual = writer.toString()
        assert(actual == expected) {
            "Expected $expected, but was $actual"
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "When processed as {0} expecting {2}")
        fun params(): Collection<Array<Any?>> = listOf(
                arrayOf(Type.CSS, """body {
                    margin: 0px;
                    color: black;
                    background-color: white;
                }""".trimMargin(), "body{margin:0;color:black;background-color:white}", null),
                arrayOf(Type.JAVASCRIPT, """function(value) {
                    console.log('Value is ' + value);
                }""".trimMargin(), """function(a){console.log("Value is "+a)};""", null),
                arrayOf(Type.CSS, """body {
                    margin: 0px;
                    color: #000000;
                    background-color: #ffffff;
                }
                body * {
                    font-family: arial, helvetica, sans-serif;
                    font-size: 11pt;
                }""".trimMargin(), "body{margin:0;color:#000;background-color:#fff}body *{font-family:arial,helvetica,sans-serif;font-size:11pt}", null),
                arrayOf(Type.JAVASCRIPT, """function(value) {
                    let x = 0;
                    console.log('Value is ' + value);
                }
                function(first, second, third) {
                    return first + (second * third);
                }""".trimMargin(), """function(a){let x=0;console.log("Value is "+a)}function(c,b,a){return c+(b*a)};""", null),
                arrayOf(Type.JAVASCRIPT, """function(value { 
                    console.log('invalid function');
                }""".trimMargin(), "", EvaluatorException::class.java)
        )
    }
}