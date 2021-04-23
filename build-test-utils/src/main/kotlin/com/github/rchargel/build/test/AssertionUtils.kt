package com.github.rchargel.build.test

import java.lang.AssertionError

fun assertOrFail(assertion: () -> Boolean, message: () -> String) {
    assertOrFail(assertion.invoke(), message)
}

fun assertOrFail(assertion: Boolean, message: () -> String) {
    if (!assertion) throw AssertionError(message.invoke())
}
