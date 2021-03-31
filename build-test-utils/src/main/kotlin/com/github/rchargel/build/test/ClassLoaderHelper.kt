package com.github.rchargel.build.test

import java.net.URLClassLoader

object ClassLoaderHelper {
    @JvmStatic
    fun addClassToClassLoader(vararg types: Class<*>) {
        val locations = types.map { it.protectionDomain.codeSource.location }.toTypedArray()
        val classLoader = URLClassLoader.newInstance(locations, Thread.currentThread().contextClassLoader)
        Thread.currentThread().contextClassLoader = classLoader
    }
}