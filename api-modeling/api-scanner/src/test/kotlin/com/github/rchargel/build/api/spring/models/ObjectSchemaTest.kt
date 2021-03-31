package com.github.rchargel.build.api.spring.models

import org.junit.After
import org.junit.Assert.assertEquals
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