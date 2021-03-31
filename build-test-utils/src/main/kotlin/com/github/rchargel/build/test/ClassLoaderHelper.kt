package com.github.rchargel.build.test

import java.net.URLClassLoader

class ClassLoaderHelper {
    companion object {
        @JvmStatic
        fun  addClassToClassLoader(vararg types: Class<*>) {
            val locations = types.mapNotNull { it?.protectionDomain?.codeSource?.location }.toTypedArray()
            val classLoader = URLClassLoader.newInstance(locations, Thread.currentThread().contextClassLoader);
            Thread.currentThread().contextClassLoader = classLoader;
        }
    }
}