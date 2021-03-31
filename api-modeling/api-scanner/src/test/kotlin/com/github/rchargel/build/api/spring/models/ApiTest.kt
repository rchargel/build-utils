package com.github.rchargel.build.api.spring.models

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class ApiTest(private val a: Api, private val b: Api?, private val expected: Api) {

    @Test
    fun canMergeAPIs() = assert(expected == (a + b)) {
        "Expected $expected but found ${a + b}"
    }

    companion object {
        @Parameters
        @JvmStatic
        fun params() = arrayOf(
                arrayOf(Api(title = "First API"), Api(title = "Second API", description = "My Description"), Api(title = "First API", description = "My Description")),
                arrayOf(Api(title = "First API"), null, Api(title = "First API")),
                arrayOf(Api(), Api(title = "Second API", version = "1.0"), Api(title = "Second API", version = "1.0")),
                arrayOf(Api(), Api("Title", "MIT", "http://license", setOf("http://go"), "1.0", "info"), Api("Title", "MIT", "http://license", setOf("http://go"), "1.0", "info")),
                arrayOf(Api("Title", "MIT", "http://license", setOf("http://go"), "1.0", "info"), Api(urls = setOf("http://stop")), Api("Title", "MIT", "http://license", setOf("http://go", "http://stop"), "1.0", "info")),
                arrayOf(Api("Title", "MIT", "http://license", setOf("http://go"), "1.0", "info"), null, Api("Title", "MIT", "http://license", setOf("http://go"), "1.0", "info")),
                arrayOf(Api("Title", null, null, setOf("http://go"), "2.0", "info"), Api(null, "MIT", null, emptySet(), "1.0"), Api("Title", "MIT", null, setOf("http://go"), "2.0", "info"))
        )
    }
}