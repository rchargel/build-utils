package com.github.rchargel.build.common

import com.fake.classes.classes.InformalHello
import com.fake.classes.classes.MyBaseClass
import com.fake.classes.classes.MyInterface
import com.github.rchargel.build.test.ClassLoaderHelper
import org.junit.After
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.util.stream.Collectors

@RunWith(Parameterized::class)
class CanFindSubTypesTest(private val type: Class<*>, private val subtypes: Int) {

    @Test
    fun findSubTypes() = assert(ClasspathUtil.findSubTypes(type).count().toInt() == subtypes) {
        "Expected $subtypes but found ${
            ClasspathUtil.findSubTypes(type).map { it.toString() }.collect(Collectors.joining())
        }"
    }

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            ClassLoaderHelper.addClassToClassLoader(
                MyInterface::class.java,
                MyBaseClass::class.java,
                InformalHello::class.java
            )
        }

        @AfterClass
        @JvmStatic
        fun tearDown() = ClassLoaderHelper.resetClassLoader()

        @JvmStatic
        @Parameters(name = "Starting with class {0}, I will find {1} sub types")
        fun params(): Array<Array<Any>> = arrayOf(
            arrayOf(MyInterface::class.java, 2),
            arrayOf(MyBaseClass::class.java, 1),
            arrayOf(InformalHello::class.java, 0)
        )
    }
}