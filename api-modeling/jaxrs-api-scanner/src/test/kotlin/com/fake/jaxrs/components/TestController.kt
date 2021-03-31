package com.fake.jaxrs.components

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import java.time.LocalDateTime
import java.util.*
import javax.annotation.security.RolesAllowed
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/api/1.0/test")
@Tags(Tag(name = "Main", description = "Main paths"))
@Produces(MediaType.APPLICATION_JSON)
class TestController {

    @GET
    fun listKeys(@QueryParam("filter") filter: String?): List<Data> = emptyList()

    @GET
    @Path("{key}")
    fun getValue(@PathParam("key") key: String) = Data()

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    fun insertValue(data: Data, request: HttpServletRequest) = true

    @PUT
    @Path("{key}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    fun updateValue(@PathParam("key") key: String, data: Data) = true

    @DELETE
    @RolesAllowed("ADMIN")
    @Path("{key}")
    @Operation(security = [SecurityRequirement(name = "basic")])
    fun removeValue(@PathParam("key") key: String) = true

    fun otherMethod() = "Do nothing"
}

data class Data(
        val key: String = UUID.randomUUID().toString(),
        val id: Long = 0,
        val name: String = "My Data",
        val timestamp: LocalDateTime = LocalDateTime.now(),
        val isRequired: Boolean = false,
        val subList: List<Double> = emptyList()
)
