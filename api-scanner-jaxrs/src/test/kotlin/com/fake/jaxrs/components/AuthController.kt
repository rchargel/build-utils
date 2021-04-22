package com.fake.jaxrs.components

import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import javax.ws.rs.HeaderParam
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/api/1.0/auth")
@Tags(Tag(name="Auth", description = "Authentication"))
class AuthController {

    @POST
    @RequestBody
    fun login(@HeaderParam("username") username: String, @HeaderParam("password") password: String) = true
}