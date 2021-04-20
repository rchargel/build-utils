package com.github.rchargel.build.common

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class CanDefaultFirstAndLastTest(
    private val list: Collection<Int>,
    private val defaultVal: Int,
    private val firstExpected: Int,
    private val lastExpected: Int,
    private val firstExpectedWithPred: Int,
    private val lastExpectedWithPred: Int,
) {

    @Test
    fun validateFirst() = assert(list.firstOr(defaultVal) == firstExpected) {
        "Expected first $firstExpected but was ${
            list.firstOr(defaultVal)
        }"
    }

    @Test
    fun validateFirstWithPredicate() = assert(list.firstOr(defaultVal) { i -> i % 3 == 0 } == firstExpectedWithPred) {
        "Expected first $firstExpectedWithPred but was ${
            list.firstOr(defaultVal) { i -> i % 3 == 0 }
        }"
    }

    @Test
    fun validateLast() = assert(list.lastOr(defaultVal) == lastExpected) {
        "Expected first $lastExpected but was ${
            list.lastOr(defaultVal)
        }"
    }


    @Test
    fun validateLastWithPredicate() = assert(list.lastOr(defaultVal) { i -> i % 3 == 0 } == lastExpectedWithPred) {
        "Expected first $lastExpectedWithPred but was ${
            list.lastOr(defaultVal) { i -> i % 3 == 0 }
        }"
    }

    companion object {
        @JvmStatic
        @Parameters(name = "List {0} with default {1} will have first = {2} and last = {3}")
        fun params() = listOf(
            arrayOf(emptyList<Int>(), 100, 100, 100, 100, 100),
            arrayOf(listOf(1), 100, 1, 1, 100, 100),
            arrayOf(listOf(1, 2, 3), 100, 1, 3, 3, 3),
            arrayOf(listOf(1, 2, 3, 4, 5, 6, 7, 8), 100, 1, 8, 3, 6)
        )
    }

}