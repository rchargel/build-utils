package com.github.rchargel.build.api

import com.github.rchargel.build.api.models.Api
import com.github.rchargel.build.common.ClasspathUtil
import com.github.rchargel.build.common.ExceptionWrapper
import com.github.rchargel.build.common.ExceptionWrapper.ignoreException
import org.reflections.ReflectionsException
import java.util.stream.Collectors

interface ApiScanner {

    fun scanApi(basePackage: String): Api?

    fun isAvailable(): Boolean

    companion object {
        @JvmStatic
        fun loadScanners(): List<ApiScanner> = ignoreException {
            ClasspathUtil.findSubTypes(ApiScanner::class.java).collect(Collectors.toList())
                    .mapNotNull { it.getConstructor() }.mapNotNull { it.newInstance() }
                    .filter { it.isAvailable() }
        } ?: emptyList()

        @JvmStatic
        fun loadApi(basePackage: String): Api? = ignoreException {
            loadScanners().mapNotNull { it.scanApi(basePackage) }.reduceRight { a, b -> a + b }
        }
    }
}