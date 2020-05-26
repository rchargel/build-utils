package com.github.rchargel.build.common

import kotlin.math.max

class RuntimeUtils {
    companion object {
        @JvmStatic
        fun getOptimizedThreads() = max(1, Runtime.getRuntime().availableProcessors() - 2)
    }
}