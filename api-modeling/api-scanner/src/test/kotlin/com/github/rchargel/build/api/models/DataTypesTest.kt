package com.github.rchargel.build.api.models

import org.junit.Test

class DataTypesTest {
    @Test
    fun testDataTypesEquals() {
        assert(DataType.StringType == DataType.StringType)
        assert(DataType.StringType === DataType.StringType)
        assert(!DataType.StringType.equals(DataType.NumberType))
        assert(DataType.StringType != null)
        assert(!DataType.StringType.equals(emptyList<String>()))
        assert(DataType.createObjectType("field" to DataType.StringType) == DataType.createObjectType("field" to DataType.StringType))
        assert(DataType.createObjectType("field" to DataType.StringType) !== DataType.createObjectType("field" to DataType.StringType))
        assert(DataType.createObjectType("field" to DataType.StringType) != DataType.createObjectType("field" to DataType.NumberType))
    }
}