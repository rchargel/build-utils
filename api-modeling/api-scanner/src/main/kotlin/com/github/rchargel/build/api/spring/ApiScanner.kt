package com.github.rchargel.build.api.spring

import com.github.rchargel.build.api.spring.models.Api
import com.github.rchargel.build.common.ClasspathUtil
import org.reflections.ReflectionsException
import java.util.stream.Collectors

interface ApiScanner {

    fun scanApi(basePackage: String): Api?

    fun isAvailable(): Boolean

    companion object {
        @JvmStatic
        fun loadScanners(): List<ApiScanner> = try {
            ClasspathUtil.findSubTypes(ApiScanner::class.java).collect(Collectors.toList())
                    .mapNotNull { it.getConstructor() }.mapNotNull { it.newInstance() }
                    .filter { it.isAvailable() }
        } catch (e: ReflectionsException) {
            e.printStackTrace()
            emptyList()
        }

        @JvmStatic
        fun loadApi(basePackage: String): Api? = loadScanners().mapNotNull { it.scanApi(basePackage) }
                .reduceRight { a, b -> a + b }
    }
}