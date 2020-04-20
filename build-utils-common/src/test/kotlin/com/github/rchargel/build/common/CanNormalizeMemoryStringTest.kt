package com.github.rchargel.build.common

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class CanNormalizeMemoryStringTest(private val number: Long?, private val expected: String?) {

    @Test
    fun validateFormat() = Assert.assertEquals(expected, StringUtils.normalizeMemoryString(number))

    companion object {
        @JvmStatic
        @Parameters(name = "{0} Should be formatted to {1}")
        fun params() = listOf(
                arrayOf(100L, "100.0 B"),
                arrayOf(1024L, "1024.0 B"),
                arrayOf(2048L, "2048.0 B"),
                arrayOf(2050L, "2.0 KB"),
                arrayOf(10000L, "9.8 KB"),
                arrayOf(1024000L, "1000.0 KB"),
                arrayOf(10240000L, "9.8 MB"),
                arrayOf(10240000000L, "9.5 GB"),
                arrayOf(10240000000000L, "9.3 TB"),
                arrayOf(10240000000000000L, "9313.2 TB"),
                arrayOf(null as Long?, null as String?)
        )
    }
}