package com.github.rchargel.build.api.spring.models

enum class Method {
    GET,
    POST,
    PUT,
    DELETE,
    HEAD,
    OPTIONS
}

data class Path(
        val path: String,
        val method: Method,
        val securitySchema: String? = null,
        val parameters: List<Parameter> = emptyList(),
        val name: String? = null,
        val description: String? = null
) {

    companion object {
        @JvmStatic
        fun builder() = PathBuilder()
    }

    class PathBuilder(
            private var path: String? = null,
            private var method: Method = Method.GET,
            private var name: String? = null,
            private var securitySchema: String? = null,
            private var description: String? = null,
            private var parameters: MutableList<Parameter> = mutableListOf()
    ) {
        fun path(path: String) = apply { this.path = path }
        fun name(name: String?) = apply { this.name = name }
        fun description(description: String?) = apply { this.description = description }
        fun method(method: Method) = apply { this.method = method }
        fun securitySchema(securitySchema: String?) = apply { this.securitySchema = securitySchema }
        fun addParameter(parameter: Parameter) = apply { this.parameters.add(parameter) }
        fun parameters(parameters: Collection<Parameter>) = apply { this.parameters = parameters.toMutableList() }

        fun build() = Path(
                name = name,
                path = path ?: error("Missing path"),
                description = description,
                method = method,
                securitySchema = securitySchema,
                parameters = parameters.toList()
        )
    }
}