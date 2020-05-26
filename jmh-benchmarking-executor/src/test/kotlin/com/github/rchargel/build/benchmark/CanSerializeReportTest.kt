package com.github.rchargel.build.benchmark

import com.fake.test.SimpleBenchmark
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.rchargel.build.benchmark.results.BenchmarkResults
import org.junit.Assert.assertEquals
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.stream.Stream

class CanSerializeReportTest {

    companion object {
        @ClassRule
        @JvmField
        val temporaryFolder = TemporaryFolder()
    }

    @Test
    fun canSerializeReport() {
        val result = BenchmarkExecutor().executeBenchmarks(0.2, 1,
                listOf(SimpleBenchmark::class.java).stream() as Stream<Class<*>>)

        val file = temporaryFolder.newFile()
        val objMapper = ObjectMapper()

        file.writer(Charsets.UTF_8).use { objMapper.writer().writeValue(it, result) }

        val deserialized = file.reader(Charsets.UTF_8).use { objMapper.readValue(it, BenchmarkResults::class.java) }
        assertEquals(result, deserialized)
    }
}