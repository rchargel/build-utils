package com.github.rchargel.build.api.models

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class ObjectSchemaTest {

    private lateinit var underTest: ObjectSchema

    private val expected = """{
  "name": "string", 
  "isType": false, 
  "total": 0, 
  "listOfNames": [
    "string"
  ], 
  "data": {
    "key1": "string", 
    "key2": [
      0
    ]
  }, 
  "dataSet": [
    {
      "dskey": [
        false
      ]
    }
  ]
}"""

    @Test
    fun testToJsonString() {
        assertEquals(expected, underTest.toJsonString())
    }

    @Test
    fun cannotRegisterSameSchema() {
        assertThrows(DuplicateObjectSchemaRegistrationException::class.java) {
            ObjectSchema.builder().name("TestSchema").build()
        }
    }

    @Test
    fun cannotRequestUnknownSchema() {
        assertThrows(UnknownObjectSchemaException::class.java) {
            ObjectSchemaRegistry.getObjectSchema("FakeSchema")
        }
    }

    @Test
    fun canListSchemata() {
        ObjectSchema.builder()
                .name("NewSchema")
                .build()
        assert(listOf("NewSchema", "TestSchema") == ObjectSchemaRegistry.listSchemata())
    }

    @Before
    fun setup() {
        underTest = ObjectSchema.builder()
                .name("TestSchema")
                .addField("name", DataType.StringType)
                .addField("isType", DataType.BooleanType)
                .addField("total", DataType.NumberType)
                .addField("listOfNames", DataType.createArrayType(DataType.StringType))
                .addField("data", DataType.createObjectType(
                        "key1" to DataType.StringType,
                        "key2" to DataType.createArrayType(DataType.NumberType)
                ))
                .addField("dataSet", DataType.createArrayType(DataType.createObjectType("dskey" to DataType.createArrayType(DataType.BooleanType))))
                .build()
    }

    @After
    fun tearDown() {
        ObjectSchemaRegistry.clear()
    }
}