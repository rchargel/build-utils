package com.fake.jaxrs.components

import java.nio.charset.StandardCharsets
import java.util.*
import javax.annotation.security.DenyAll
import javax.annotation.security.PermitAll
import javax.annotation.security.RolesAllowed
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.ext.Provider

@Provider
class AuthorizationFilter : ContainerRequestFilter {
    @Context
    private lateinit var resourceInfo: ResourceInfo

    override fun filter(ctx: ContainerRequestContext) {
        val method = resourceInfo.resourceMethod

        if (!method.isAnnotationPresent(PermitAll::class.java)) {
            if (method.isAnnotationPresent(DenyAll::class.java)) {
                ctx.abortWith(Response.status(Response.Status.FORBIDDEN)
                        .entity("Access Blocked for all users").build())
                return
            }

            val headers = ctx.headers
            val authHeaders = headers[AUTHORIZATION_PROPERTY]

            if (authHeaders?.isNotEmpty() != true) {
                ctx.abortWith(Response.status(Response.Status.FORBIDDEN)
                        .entity("Auth required").build())
                return
            }

            val encodedPassword = authHeaders[0].replaceFirst("$AUTHENTICATION_SCHEME ", "")
            val usernamePassword = String(java.util.Base64.getDecoder().decode(encodedPassword), StandardCharsets.UTF_8)
            val tokenizer = StringTokenizer(usernamePassword, ":")
            val user = tokenizer.nextToken()
            val pass = tokenizer.nextToken()

            if (method.isAnnotationPresent(RolesAllowed::class.java)) {
                val roles = method.getAnnotation(RolesAllowed::class.java).value.toSet()
                if (!roles.contains(getRole(user, pass))) {
                    ctx.abortWith(Response.status(Response.Status.FORBIDDEN)
                            .entity("You are not authorized").build())
                }
            }
        }
    }

    companion object {
        private const val AUTHORIZATION_PROPERTY = "Authorization"
        private const val AUTHENTICATION_SCHEME = "Basic"

        fun getRole(username: String, password: String) = "ADMIN"
    }
}