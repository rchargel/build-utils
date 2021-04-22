package com.github.rchargel.build.api

import com.github.rchargel.build.api.models.Api
import com.github.rchargel.build.common.ClasspathUtil
import com.github.rchargel.build.common.ExceptionWrapper.ignoreError
import java.util.stream.Collectors

interface ApiScanner {

    fun scanApi(basePackage: String): Api?

    fun isAvailable(): Boolean

    companion object {
        @JvmStatic
        fun loadScanners(): List<ApiScanner> = ignoreError {
            ClasspathUtil.findSubTypes(ApiScanner::class.java).collect(Collectors.toList())
                .mapNotNull { ignoreError { it.getConstructor().newInstance() } }
                .filter { it.isAvailable() }
        } ?: emptyList()

        @JvmStatic
        fun loadApi(basePackage: String): Api? = ignoreError {
            loadScanners().mapNotNull { it.scanApi(basePackage) }.reduceRight { a, b -> a + b }
        }
    }
}