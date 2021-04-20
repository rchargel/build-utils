package com.github.rchargel.build.benchmark

import com.github.rchargel.build.benchmark.results.BenchmarkTestResult
import org.junit.Test

class CanMergeBenchmarkTest {
    val benchmarkResultMap1 = mapOf(
        "one.1.first" to BenchmarkTestResult.builder("one.1.first")
            .packageName("one")
            .className("1")
            .methodName("first")
            .rawMeasurements(listOf(listOf(1.0, 1.0, 1.0)))
            .build(),
        "three.3.third" to BenchmarkTestResult.builder("three.3.third")
            .packageName("three")
            .className("3")
            .methodName("third")
            .rawMeasurements(listOf(listOf(3.0, 3.0), listOf(3.0, 3.0)))
            .build()
    )

    val benchmarkResultMap2 = mapOf(
        "two.2.second" to BenchmarkTestResult.builder("two.2.second")
            .packageName("two")
            .className("2")
            .methodName("second")
            .rawMeasurements(listOf(listOf(2.0), listOf(2.0), listOf(2.0)))
            .build(),
        "three.3.third" to BenchmarkTestResult.builder("three.3.third")
            .packageName("third")
            .className("three")
            .methodName("3")
            .rawMeasurements(listOf(listOf(4.0, 4.0), listOf(4.0, 4.0)))
            .build()
    )

    @Test
    fun testMerger() {
        val mergeResult = BenchmarkExecutor.merge(benchmarkResultMap1, benchmarkResultMap2)

        assert(3 == mergeResult.size) { "Should have 3 entries but was ${mergeResult.size}" }

        var test = mergeResult["one.1.first"]
        assert(test != null) { "missing first entry" }
        assert("one" == test?.packageName) { "expected 'one' but was '${test?.packageName}'" }
        assert("1" == test?.className) { "expected '1' but was '${test?.className}'" }
        assert("first" == test?.methodName) { "expected 'first' but was '${test?.methodName}'" }
        assert(listOf(listOf(1.0, 1.0, 1.0)) == test?.rawMeasurements) { "unexpected raw values" }
        assert("{min=0.0, out-min=0.0, 1qtr=0.0, media=0.0, 3qtr=0.0, out-max=0.0, max=0.0}" == test?.shortString()) { "invalid short string" }

        test = mergeResult["two.2.second"]
        assert(test != null) { "missing second entry" }
        assert("two" == test?.packageName) { "expected 'two' but was '${test?.packageName}'" }
        assert("2" == test?.className) { "expected '2' but was '${test?.className}'" }
        assert("second" == test?.methodName) { "expected 'second' but was '${test?.methodName}'" }
        assert(listOf(listOf(2.0), listOf(2.0), listOf(2.0)) == test?.rawMeasurements) { "unexpected raw values" }
        assert("{min=0.0, out-min=0.0, 1qtr=0.0, media=0.0, 3qtr=0.0, out-max=0.0, max=0.0}" == test?.shortString()) { "invalid short string" }

        test = mergeResult["three.3.third"]
        assert(test != null) { "missing third entry" }
        assert("three" == test?.packageName) { "expected 'three' but was '${test?.packageName}'" }
        assert("3" == test?.className) { "expected '3' but was '${test?.className}'" }
        assert("third" == test?.methodName) { "expected 'third' but was '${test?.methodName}'" }
        assert(listOf(listOf(3.0, 3.0), listOf(3.0, 3.0), listOf(4.0, 4.0), listOf(4.0, 4.0)) == test?.rawMeasurements) { "unexpected raw values" }
        assert("{min=3.0, out-min=1.5, 1qtr=3.0, media=3.5, 3qtr=4.0, out-max=5.5, max=4.0}" == test?.shortString()) { "invalid short string" }

        assert(mergeResult["four.4.fourth"] == null) { "shouldn't find anything here" }
    }
}