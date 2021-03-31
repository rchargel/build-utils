package com.github.rchargel.build.api.spring

import com.github.rchargel.build.api.spring.models.ArrayType
import com.github.rchargel.build.api.spring.models.DataType
import com.github.rchargel.build.api.spring.models.ObjectSchema
import io.swagger.v3.oas.models.media.*

class ObjectSchemaGenerator {
    fun generateSchema(key: String, schema: Schema<*>): ObjectSchema {
        val builder = ObjectSchema.builder()
                .name(key)

        schema.properties.entries.map { toField(it.key, it.value) }.forEach { builder.addField(it.first, it.second) }
        return builder.build()
    }

    private fun toField(key: String, schema: Schema<*>): Pair<String, DataType> = schema.name to toDataType(schema)

    private fun toDataType(schema: Schema<*>) = when (schema) {
        is StringSchema -> DataType.StringType
        is IntegerSchema -> DataType.NumberType
        is NumberSchema -> DataType.NumberType
        is BooleanSchema -> DataType.BooleanType
        is ArraySchema -> toArrayDataType(schema)
        is BinarySchema -> DataType.createArrayType(DataType.NumberType)
        is ByteArraySchema -> DataType.createArrayType(DataType.NumberType)
        is PasswordSchema -> DataType.StringType
        is DateSchema -> DataType.StringType
        is DateTimeSchema -> DataType.StringType
        is io.swagger.v3.oas.models.media.ObjectSchema -> DataType.createObjectType(
                schema.properties.map { toField(it.key, it.value) }.toMap()
        )
        else -> DataType.createObjectType(emptyMap())
    }

    private fun toArrayDataType(schema: ArraySchema): ArrayType {
        return DataType.createArrayType(toDataType(schema.items))
    }
}