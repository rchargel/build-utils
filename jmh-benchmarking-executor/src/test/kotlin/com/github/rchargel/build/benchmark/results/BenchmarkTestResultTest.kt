package com.github.rchargel.build.benchmark.results

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream

class BenchmarkTestResultTest {

    lateinit var result: BenchmarkTestResult

    @Before
    fun setup() {
        result = BenchmarkTestResult.builder("com.fake.package.MyTest.internalMethod")
                .mode("Average")
                .scoreUnits("ms")
                .numberOfTestThreads(4)
                .numberOfTestRepititions(10)
                .numberOfWarmupIterations(5)
                .numberOfMeasurementIterations(10)
                .warmupTimeInMilliseconds(1000)
                .measurementTimeInMilliseconds(1000)
                .medianMeasurement(10.1)
                .meanErrorAt999(0.5)
                .addParam("first", "1")
                .addParam("second", "2")
                .addRawMeasurement(listOf(0.5, 10.1, 100.2))
                .build()
                .merge(BenchmarkTestResult.builder("com.fake.package.MyTest.internalMethod")
                        .mode("Average")
                        .scoreUnits("ms")
                        .numberOfTestThreads(4)
                        .numberOfTestRepititions(10)
                        .numberOfWarmupIterations(5)
                        .numberOfMeasurementIterations(10)
                        .medianMeasurement(10.1)
                        .meanErrorAt999(0.5)
                        .addRawMeasurement(listOf(0.4, 9.0))
                        .build())
    }

    @Test
    fun validateBuilder() {
        assertEquals("com.fake.package", result.packageName)
        assertEquals("MyTest", result.className)
        assertEquals("internalMethod[ first=1, second=2 ]", result.methodName)
        assertEquals(20, result.numberOfTestRepetitions)
        assertEquals(0.4, result.distributionStatistics.minimum, 0.0)
        assertEquals(100.2, result.distributionStatistics.maximum, 0.0)
        assertEquals(24.04, result.distributionStatistics.mean, 0.0001)
    }

    @Test
    fun validateJSONSerialize() {
        ByteArrayOutputStream().use {
            val mapper = ObjectMapper()
            mapper.writeValue(it, result)
            val newValue = mapper.readValue(it.toByteArray(), BenchmarkTestResult::class.java)
            assertEquals(result, newValue)
        }
    }
}