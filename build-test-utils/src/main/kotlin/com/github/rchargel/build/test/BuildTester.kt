package com.github.rchargel.build.test

import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import pl.pojo.tester.api.ConstructorParameters
import pl.pojo.tester.internal.field.DefaultFieldValueChanger
import pl.pojo.tester.internal.instantiator.ObjectGenerator
import pl.pojo.tester.internal.utils.ThoroughFieldPermutator
import java.beans.Introspector
import java.lang.reflect.Method

class BuildTester(private val underTest: Class<*>, ignoredProperties: Set<String> = setOf("class")) {
    private val builder: Any
    private val allIgnored: Set<String> = ignoredProperties + setOf("class")
    private val properties = Introspector.getBeanInfo(underTest).propertyDescriptors.filter { it.readMethod != null }
            .filter { !allIgnored.contains(it.name) }
    private val objectGenerator = ObjectGenerator(
            DefaultFieldValueChanger.INSTANCE,
            ArrayListValuedHashMap<Class<*>, ConstructorParameters>(),
            ThoroughFieldPermutator()
    )

    init {
        val method: Method = underTest.getMethod("builder")
        builder = try {
            method.invoke(null)
        } catch (e: NullPointerException) {
            throw NoSuchMethodException("No static method 'builder()'")
        }
    }

    private fun findMethod(builder: Any, name: String, propType: Class<*>): Method {
        return builder.javaClass.methods.filter { it.name == name }
                .filter { it.parameterCount == 1 }
                .firstOrNull { it.parameterTypes[0].isAssignableFrom(propType) || propType.isAssignableFrom(it.parameterTypes[0]) }
                ?: throw NoSuchMethodException("No method $name with parameter $propType")
    }

    fun evaluate() {
        val values = properties.map { prop ->
            val value = objectGenerator.createNewInstance(prop.propertyType)
            val method: Method = findMethod(builder, prop.name, prop.propertyType)
            assert(builder.javaClass == method.returnType) {
                "Builder method returns type of ${method.returnType} but should be builder"
            }
            assert(builder === method.invoke(builder, value)) {
                "Builder returns new instance of builder"
            }

            prop.name to value
        }.toMap()

        val buildMethod: Method = try {
            builder.javaClass.getMethod("build")
        } catch (e: NoSuchMethodException) {
            throw NoSuchMethodException("Builder must have a 'build()' method")
        }
        val obj = buildMethod.invoke(builder)
        assert(obj.javaClass == underTest) {
            "Builder produced object of type ${obj.javaClass} but expected $underTest"
        }

        assert(obj != null) { "Resulting object was null" }

        properties.forEach { prop ->
            val actual = prop.readMethod.invoke(obj)

            assert(values[prop.name] == actual) {
                "${prop.name}: expected ${values[prop.name]} but was $actual"
            }
        }
    }

}