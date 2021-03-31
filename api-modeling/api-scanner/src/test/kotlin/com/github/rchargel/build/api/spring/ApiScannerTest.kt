package com.github.rchargel.build.api.spring

import com.github.rchargel.build.test.ClassLoaderHelper
import org.junit.BeforeClass
import org.junit.Test

class ApiScannerTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() = ClassLoaderHelper.addClassToClassLoader(ApiScanner::class.java)
    }

    @Test
    fun shouldNotFindScanners() = assert(ApiScanner.loadScanners().isEmpty()) {
        "Should not find any scanners"
    }
}