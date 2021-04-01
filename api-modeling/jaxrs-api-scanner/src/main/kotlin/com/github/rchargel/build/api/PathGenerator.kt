package com.github.rchargel.build.api

import com.github.rchargel.build.api.models.Method
import com.github.rchargel.build.api.models.Path
import io.swagger.v3.oas.models.Operation

class PathGenerator {

    fun createPath(path: String, operation: Operation, method: Method): List<Pair<String, Path>> {
        val path = Path.builder()
                .path(path)
                .name(operation.summary ?: operation.operationId)
                .description(operation.description)
                .method(method)
                .build()

        return operation.tags?.map { it to path } ?: listOf(null as String to path)
    }
}