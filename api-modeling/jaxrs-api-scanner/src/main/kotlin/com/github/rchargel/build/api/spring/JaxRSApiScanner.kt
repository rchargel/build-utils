package com.github.rchargel.build.api.spring

import com.github.rchargel.build.api.spring.models.Api
import com.github.rchargel.build.api.spring.models.Component
import com.github.rchargel.build.api.spring.models.Method
import com.github.rchargel.build.api.spring.models.Path
import com.github.rchargel.build.common.ClasspathUtil
import io.swagger.v3.jaxrs2.Reader
import java.util.stream.Collectors

class JaxRSApiScanner : ApiScanner {
    private val schemaGenerator = ObjectSchemaGenerator()
    private val pathGenerator = PathGenerator()

    override fun isAvailable() = try {
        Class.forName("javax.ws.rs.GET")
        true
    } catch (e: ClassNotFoundException) {
        false
    }

    override fun scanApi(basePackage: String): Api? {
        val classes = ClasspathUtil.findClassesInPackage(basePackage)
                .collect(Collectors.toSet())

        val reader = Reader()
        val openAPI = reader.read(classes)

        openAPI.components.schemas.forEach { schema ->
            schemaGenerator.generateSchema(schema.key, schema.value)
        }

        val componentBuilders = (openAPI.tags?.map {
            it.name to Component.builder().name(it.name).description(it.description)
        } ?: listOf(null to Component.builder())).toMap().toMutableMap()

        val builder = Api.builder()
                .description(openAPI.info?.description)
                .license(openAPI.info?.license?.name)
                .licenseUrl(openAPI.info?.license?.url)
                .urls(openAPI.servers?.map { it.url } ?: emptyList())
                .version(openAPI.info?.version)

        println(openAPI)

        openAPI.paths?.flatMap {
            val pathList: MutableList<Pair<String, Path>> = mutableListOf()
            if (it.value.get != null)
                pathList.addAll(pathGenerator.createPath(it.key, it.value.get, Method.GET))
            if (it.value.put != null)
                pathList.addAll(pathGenerator.createPath(it.key, it.value.put, Method.PUT))
            if (it.value.post != null)
                pathList.addAll(pathGenerator.createPath(it.key, it.value.post, Method.POST))
            if (it.value.delete != null)
                pathList.addAll(pathGenerator.createPath(it.key, it.value.delete, Method.DELETE))
            if (it.value.head != null)
                pathList.addAll(pathGenerator.createPath(it.key, it.value.head, Method.HEAD))
            if (it.value.options != null)
                pathList.addAll(pathGenerator.createPath(it.key, it.value.options, Method.OPTIONS))
            pathList
        }?.forEach { p ->
            if (!componentBuilders.containsKey(p.first))
                componentBuilders[p.first] = Component.builder()
            componentBuilders[p.first]?.addPath(p.second)
        }
        componentBuilders.values.forEach { builder.addComponent(it.build()) }

        val api = builder.build()
        println(api)
        return api
    }

    companion object {
        private val ANNOTATIONS: Lazy<List<Class<out Annotation>>> = lazy {
            listOf(
                    Class.forName("javax.ws.rs.GET") as Class<out Annotation>,
                    Class.forName("javax.ws.rs.POST") as Class<out Annotation>,
                    Class.forName("javax.ws.rs.DELETE") as Class<out Annotation>,
                    Class.forName("javax.ws.rs.HEAD") as Class<out Annotation>,
                    Class.forName("javax.ws.rs.PUT") as Class<out Annotation>,
                    Class.forName("javax.ws.rs.Path") as Class<out Annotation>
            )
        }
    }
}