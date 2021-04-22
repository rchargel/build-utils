package com.github.rchargel.build.api

import com.github.rchargel.build.api.models.Api
import com.github.rchargel.build.test.ClassLoaderHelper
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

class FakeApiScannerTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() = ClassLoaderHelper.addClassToClassLoader(ApiScanner::class.java, FakeApiScannerTest::class.java)

        @AfterClass
        @JvmStatic
        fun tearDown() = ClassLoaderHelper.resetClassLoader()
    }

    @Test
    fun shouldFindScanners() = assert(ApiScanner.loadScanners().size == 1) {
        "Should find Fake Scanner"
    }

    @Test
    fun shouldNotFindAPI() = assert(null == ApiScanner.loadApi("org.test")) {
        "Should not find any information"
    }

    @Test
    fun shouldFindApi() = assert(Api.builder().title("Fake").build() == ApiScanner.loadApi("com.github.rchargel")) {
        "Didn't find api"
    }

    class FakeApiScanner : ApiScanner {
        override fun scanApi(basePackage: String) = if (basePackage.startsWith("com.github.rchargel")) Api.builder().title("Fake").build()
        else null

        override fun isAvailable() = true
    }
}