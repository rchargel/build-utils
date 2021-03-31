package com.github.rchargel.build.api.spring.models

data class Api(
        val title: String? = null,
        val license: String? = null,
        val licenseUrl: String? = null,
        val urls: Set<String> = emptySet(),
        val version: String? = null,
        val description: String? = null,
        val components: Set<Component> = emptySet(),
        val schemata: Map<String, ObjectSchema> = emptyMap()
) {
    operator fun plus(other: Api?) = this.merge(other)

    @Suppress("MemberVisibilityCanBePrivate")
    fun merge(other: Api?): Api {
        return Api(
                title = this.title ?: other?.title,
                license = this.license ?: other?.license,
                licenseUrl = this.licenseUrl ?: other?.licenseUrl,
                urls = this.urls + (other?.urls ?: emptyList()),
                version = this.version ?: other?.version,
                description = this.description ?: other?.description,
                schemata = this.schemata + (other?.schemata ?: emptyMap())
        )
    }

    companion object {
        @JvmStatic
        fun builder() = ApiBuilder()
    }

    class ApiBuilder internal constructor(
            private var title: String? = null,
            private var license: String? = null,
            private var licenseUrl: String? = null,
            private var urls: MutableSet<String> = mutableSetOf(),
            private var version: String? = null,
            private var description: String? = null,
            private var components: MutableSet<Component> = mutableSetOf()
    ) {
        fun title(title: String?) = apply { this.title = title }
        fun license(license: String?) = apply { this.license = license }
        fun licenseUrl(licenseUrl: String?) = apply { this.licenseUrl = licenseUrl }
        fun urls(urls: Collection<String>) = apply { this.urls.addAll(urls) }
        fun version(version: String?) = apply { this.version = version }
        fun description(description: String?) = apply { this.description = description }
        fun addComponent(component: Component) = apply { this.components.add(component) }

        fun build() = Api(
                title = title,
                license = license,
                licenseUrl = licenseUrl,
                urls = urls.toSet(),
                version = version,
                description = description,
                components = components.toSet(),
                schemata = ObjectSchemaRegistry.listSchemata().map { it to ObjectSchemaRegistry.getObjectSchema(it) }.toMap()
        )
    }
}
