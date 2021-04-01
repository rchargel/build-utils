package com.github.rchargel.build.api.models

import com.github.rchargel.build.test.BuildTester
import org.junit.AfterClass
import org.junit.Test

class ModelsTest {

    @Test
    fun testApi() = BuildTester(Api::class.java, setOf("schemata")).evaluate()

    @Test
    fun testComponent() = BuildTester(Component::class.java).evaluate()

    @Test
    fun testObjectSchema() = BuildTester(ObjectSchema::class.java, setOf("contentType")).evaluate()

    @Test
    fun testParameter() = BuildTester(Parameter::class.java).evaluate()

    @Test
    fun testPath() = BuildTester(Path::class.java).evaluate()

    companion object {
        @JvmStatic
        @AfterClass
        fun unregister() = ObjectSchemaRegistry.clear()
    }
}