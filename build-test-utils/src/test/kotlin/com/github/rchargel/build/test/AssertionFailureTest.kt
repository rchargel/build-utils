package com.github.rchargel.build.test

import org.junit.Assert
import org.junit.Test

class AssertionFailureTest {

    @Test
    fun validateAssertions() {
        assertOrFail({ true }, { "Won't happen" })
        Assert.assertThrows(AssertionError::class.java) { assertOrFail({ false }, { "Will happen" }) }
    }
}