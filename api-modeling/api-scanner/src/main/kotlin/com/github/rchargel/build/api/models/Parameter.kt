package com.github.rchargel.build.api.models

enum class ParameterType {
    Header,
    Path,
    Query,
    RequestBody
}

data class Parameter internal constructor(
        val type: ParameterType,
        val name: String,
        val description: String?,
        val required: Boolean = false,
        val deprecated: Boolean = false,
        val defaultValue: String? = null,
        val body: ObjectSchema? = null
) {
    companion object {
        @JvmStatic
        fun builder() = ParameterBuilder()
    }

    class ParameterBuilder internal constructor(
            private var type: ParameterType? = null,
            private var name: String? = null,
            private var description: String? = null,
            private var required: Boolean = false,
            private var deprecated: Boolean = false,
            private var defaultValue: String? = null,
            private var body: ObjectSchema? = null
    ) {

        fun type(type: ParameterType) = apply { this.type = type }
        fun name(name: String) = apply { this.name = name }
        fun description(description: String?) = apply { this.description = description }
        fun required(required: Boolean) = apply { this.required = required }
        fun deprecated(deprecated: Boolean) = apply { this.deprecated = deprecated }
        fun defaultValue(defaultValue: String?) = apply { this.defaultValue = defaultValue }
        fun body(body: ObjectSchema?) = apply { this.body = body }
        fun body(objectSchemaName: String) = apply { this.body = ObjectSchemaRegistry.getObjectSchema(objectSchemaName) }
        fun build() = Parameter(
                type = this.type!!,
                name = this.name!!,
                description = this.description,
                required = this.required,
                deprecated = this.deprecated,
                defaultValue = this.defaultValue,
                body = this.body
        )
    }
}

