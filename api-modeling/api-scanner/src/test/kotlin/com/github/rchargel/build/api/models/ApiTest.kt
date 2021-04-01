package com.github.rchargel.build.api.models

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
                arrayOf(Api.builder().title("First API").build(), Api.builder().title("Second API").description("My Description").build(), Api.builder().title("First API").description("My Description").build()),
                arrayOf(Api.builder().title("First API").build(), null, Api.builder().title("First API").build()),
                arrayOf(Api.builder().build(), Api.builder().title("Second API").version("1.0").build(), Api.builder().title("Second API").version("1.0").build()),
                arrayOf(Api.builder().build(), Api("Title", "MIT", "http://license", setOf("http://go"), "1.0", "info"), Api("Title", "MIT", "http://license", setOf("http://go"), "1.0", "info")),
                arrayOf(Api.builder().title("Title").license("MIT").licenseUrl("http://license").urls(setOf("http://go")).version("1.0").description("info").build(), Api.builder().urls(setOf("http://stop")).build(), Api.builder().title("Title").license("MIT").licenseUrl("http://license").urls(setOf("http://go", "http://stop")).version("1.0").description("info").build()),
                arrayOf(Api.builder().title("Title").license("MIT").licenseUrl("http://license").urls(setOf("http://go")).version("1.0").description("info").build(), null, Api.builder().title("Title").license("MIT").licenseUrl("http://license").urls(setOf("http://go")).version("1.0").description("info").build()),
                arrayOf(Api.builder().title("Title").urls(setOf("http://go")).version("2.0").description("info").build(), Api.builder().license("MIT").version("1.0").build(), Api.builder().title("Title").license("MIT").urls(setOf("http://go")).version("2.0").description("info").build()),
                arrayOf(Api.builder().addComponent(Component.builder().name("Comp1").addPath(Path.builder().path("Path1").build()).build()).build(), Api.builder().addComponent(Component.builder().name("Comp1").addPath(Path.builder().path("Path2").build()).build()).build(),
                        Api.builder().addComponent(Component.builder().name("Comp1").addPath(Path.builder().path("Path1").build()).addPath(Path.builder().path("Path2").build()).build()).build()),
                arrayOf(Api.builder().addComponent(Component.builder().addPath(Path.builder().path("Path1").build()).build()).addComponent(Component.builder().name("Comp1").build()).build(),
                        Api.builder().addComponent(Component.builder().name("Comp2").addPath(Path.builder().path("Path2").build()).build()).addComponent(Component.builder().addPath(Path.builder().path("Path3").build()).build()).build(),
                        Api.builder().addComponent(Component.builder().addPath(Path.builder().path("Path1").build()).addPath(Path.builder().path("Path3").build()).build()).addComponent(Component.builder().name("Comp1").build()).addComponent(Component.builder().name("Comp2").addPath(Path.builder().path("Path2").build()).build()).build())
        )
    }
}