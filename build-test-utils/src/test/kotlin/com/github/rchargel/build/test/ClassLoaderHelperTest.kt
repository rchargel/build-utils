package com.github.rchargel.build.test

import org.junit.Test

class ClassLoaderHelperTest {

    @Test
    fun notMuchToTestHere() = ClassLoaderHelper.addClassToClassLoader(ClassLoaderHelperTest::class.java)

}