package com.github.rchargel.build.common

import com.github.rchargel.build.common.ExceptionWrapper.ignoreError
import com.github.rchargel.build.common.ExceptionWrapper.wrap
import org.junit.Assert.assertThrows
import org.junit.Test

class CanWrapExceptionKotlinTest {

    @Test
    fun canWrapRunnable() {
        wrap(IllegalArgumentException::class.java) {
            assert(true) { "shouldn't fail" }
        }

        assertThrows(IllegalArgumentException::class.java) {
            wrap(IllegalArgumentException::class.java) {
                assert(false) { "Should be thrown" }
            }
        }
    }

    @Test
    fun canWrapSupplier() {
        assert("value" == wrap(IllegalArgumentException::class.java) {
            assert(true) { "shouldn't fail" }
            "value"
        }) { "missing value" }

        assertThrows(IllegalArgumentException::class.java) {
            wrap(IllegalArgumentException::class.java) {
                assert(false) { "Should be thrown" }
                "value"
            }
        }
    }

    @Test
    fun canIgnore() {
        assert("value" == ignoreError {
            assert(true) { "Will always pass" }
            "value"
        }) { "missing value" }

        assert(ignoreError {
            assert(false) { "Will still pass" }
            "value"
        } == null) { "Should be null" }
    }

}