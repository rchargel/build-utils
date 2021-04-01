package com.github.rchargel.build.api.models

data class ObjectSchema internal constructor(
        val name: String,
        val contentType: String = "application/json",
        val fields: Collection<Pair<String, DataType>> = emptyList()
) {
    companion object {
        @JvmStatic
        fun builder() = ObjectSchemaBuilder()
    }

    fun toJsonString() = "{${fields.joinToString { toJsonString(it.first, it.second) }}\n}"

    private fun toJsonString(fieldName: String, dataType: DataType, pad: Int = 2) =
            "\n${"".padStart(pad)}\"$fieldName\": ${dataType.toJsonStringValue(pad + 2)}"

    class ObjectSchemaBuilder internal constructor(
            private var name: String? = null,
            private var fields: MutableSet<Pair<String, DataType>> = mutableSetOf()
    ) {
        fun name(name: String) = apply { this.name = name }
        fun addField(name: String, field: DataType) = apply { this.fields.add(name to field) }
        fun fields(fields: Collection<Pair<String, DataType>>) = apply { this.fields = fields.toMutableSet() }
        fun build() = ObjectSchemaRegistry.registerObjectSchema(ObjectSchema(
                name = name ?: error("Missing name"),
                fields = fields.toSet()
        ))
    }

}

class DuplicateObjectSchemaRegistrationException(message: String) : Exception(message)
class UnknownObjectSchemaException(message: String) : Exception(message)

object ObjectSchemaRegistry {
    private val internalRegistry: MutableMap<String, ObjectSchema> = mutableMapOf()

    fun registerObjectSchema(objectSchema: ObjectSchema): ObjectSchema {
        if (internalRegistry.containsKey(objectSchema.name))
            throw DuplicateObjectSchemaRegistrationException("ObjectSchema named ${objectSchema.name} has already been registered")
        internalRegistry[objectSchema.name] = objectSchema
        return objectSchema
    }

    fun getObjectSchema(objectSchemaName: String): ObjectSchema = internalRegistry[objectSchemaName]
            ?: throw UnknownObjectSchemaException("ObjectSchema named $objectSchemaName is not registered")

    fun listSchemata(): List<String> = internalRegistry.keys.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER, { it }))

    fun isSchemaRegistered(objectSchemaName: String) = internalRegistry.containsKey(objectSchemaName)

    fun clear() {
        internalRegistry.clear()
    }
}