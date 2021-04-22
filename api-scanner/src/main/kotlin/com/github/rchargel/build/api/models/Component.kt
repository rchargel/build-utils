package com.github.rchargel.build.api.models

data class Component internal constructor(
        val name: String? = null,
        val description: String? = null,
        val paths: Set<Path> = emptySet()
) {

    fun merge(other: Component?) = Component(
            name = this.name ?: other?.name,
            description = this.description ?: other?.description,
            paths = this.paths + (other?.paths ?: emptySet())
    )

    companion object {
        @JvmStatic
        fun builder() = ComponentBuilder()
    }

    class ComponentBuilder internal constructor(
            private var name: String? = null,
            private var description: String? = null,
            private var paths: MutableSet<Path> = mutableSetOf()
    ) {
        fun name(name: String?) = apply { this.name = name }
        fun description(description: String?) = apply { this.description = description }
        fun addPath(path: Path) = apply { this.paths.add(path) }
        fun paths(paths: Collection<Path>) = apply { this.paths = paths.toMutableSet() }
        fun build() = Component(
                name = name,
                description = description,
                paths = paths.toSet()
        )
    }
}

fun Set<Component>.merge(other: Collection<Component>?): Set<Component> = if (other == null) {
    this
} else {
    val thisMap = this.map { it.name to it }.toMap()
    val otherMap = other.map { it.name to it }.toMap()
    val keys = thisMap.keys + otherMap.keys

    keys.mapNotNull { key ->
        val thisComp = thisMap[key]
        val otherComp = otherMap[key]

        when {
            thisComp == null -> otherComp
            otherComp == null -> thisComp
            else -> thisComp.merge(otherComp)
        }
    }.toSet()
}