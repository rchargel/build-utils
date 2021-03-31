package com.github.rchargel.build.api

import com.fake.jaxrs.components.TestController
import com.github.rchargel.build.api.spring.ApiScanner
import com.github.rchargel.build.api.spring.JaxRSApiScanner
import com.github.rchargel.build.test.ClassLoaderHelper
import org.junit.BeforeClass
import org.junit.Test

class ApiScannerWithJaxRSTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() = ClassLoaderHelper.addClassToClassLoader(
                JaxRSApiScanner::class.java,
                ApiScanner::class.java,
                TestController::class.java
        )
    }

    @Test
    fun shouldFindOneApiScanner() = assert(1 == ApiScanner.loadScanners().size) {
        "Should find 1 ApiScanner"
    }

    @Test
    fun shouldFindOneController() {
        ApiScanner.loadApi("com.fake.jaxrs")
    }
}