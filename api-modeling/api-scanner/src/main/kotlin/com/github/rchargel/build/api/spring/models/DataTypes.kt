package com.github.rchargel.build.api.spring.models

interface DataType {
    override fun toString(): String

    fun toJsonStringValue(pad: Int): String

    companion object {
        @JvmField
        val StringType = StringType()

        @JvmField
        val NumberType = NumberType()

        @JvmField
        val BooleanType = BooleanType()

        @JvmStatic
        fun createArrayType(componentType: DataType) = ArrayType(componentType)

        @JvmStatic
        fun createObjectType(vararg fields: Pair<String, DataType>) = ObjectType(fields.toMap())

        @JvmStatic
        fun createObjectType(fields: Map<String, DataType>) = ObjectType(fields.toMap())
    }
}

abstract class AbstractDataType internal constructor() : DataType {
    override fun hashCode(): Int = toString().hashCode()
    override fun equals(other: Any?) = when {
        other == null -> false
        other !is DataType -> false
        other.javaClass != this.javaClass -> false
        else -> other.toString() == this.toString()
    }
}

class StringType internal constructor() : AbstractDataType() {
    override fun toString() = "StringType"

    override fun toJsonStringValue(pad: Int) = "\"string\""
}

class NumberType internal constructor() : AbstractDataType() {
    override fun toString() = "NumberType"

    override fun toJsonStringValue(pad: Int) = "0"
}

class BooleanType internal constructor() : AbstractDataType() {
    override fun toString() = "BooleanType"
    override fun toJsonStringValue(pad: Int) = "false"
}

class ArrayType internal constructor(val componentType: DataType) : AbstractDataType() {
    override fun toString() = "ArrayType[$componentType]"
    override fun toJsonStringValue(pad: Int) =
            "[\n${"".padStart(pad)}${componentType.toJsonStringValue(pad + 2)}\n${"".padStart(pad - 2)}]"
}

class ObjectType internal constructor(val fields: Map<String, DataType>) : AbstractDataType() {
    override fun toString() = "ObjectType[${fields.entries.joinToString(", ")}]"
    override fun toJsonStringValue(pad: Int) =
            "{${fields.entries.joinToString { "\n${"".padStart(pad)}\"${it.key}\": ${it.value.toJsonStringValue(pad + 2)}" }}\n${"".padStart(pad - 2)}}"
//            "{\n${"".padStart(pad)}${fields.joinToString("\n${"".padStart(pad)}") { "\"${it.first}\": ${it.second.toJsonStringValue(pad + 2)}" }}\n${"".padStart((pad - 2))}}"
}