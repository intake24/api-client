package uk.ac.ncl.openlab.intake24.client

import org.http4k.core.*

data class EmailCredentials(val email: String, val password: String)

data class SurveyAliasCredentials(val surveyId: String, val userName: String, val password: String)

data class SigninResult(val refreshToken: String)

data class RefreshResult(val accessToken: String)

class Intake24ClientException(message: String) : RuntimeException(message)

class ThrowOnHttpErrorFilter : Filter {
    override fun invoke(inner: HttpHandler): HttpHandler {
        return {
            request ->
            val response = inner.invoke(request)

            if (response.status.successful)
                response
            else
                throw Intake24ClientException("HTTP request failed unexpectedly with status ${response.status.code} ${response.status.description}:\n$request")
        }
    }
}

class Intake24AuthFilter(val baseUrl: String, val authClient: HttpHandler, val codec: JacksonCodec, val credentials: EmailCredentials) : Filter {

    private var refreshToken: String? = null
    private var accessToken: String? = null

    private fun signin() {
        val request = Request(Method.POST, "$baseUrl/signin")
                .header("Content-Type", ContentType.APPLICATION_JSON.toHeaderValue())
                .body(codec.encode(credentials))

        val response = authClient.invoke(request)

        if (response.status.successful) {
            val result = codec.decode(response.bodyString(), SigninResult::class)
            refreshToken = result.refreshToken

            println("Got new refresh token")
        } else
            throw Intake24ClientException("Sign in failed with code ${response.status.code} ${response.status.description}")
    }

    private fun refresh() {
        if (refreshToken == null)
            signin()

        val response = authClient.invoke(Request(Method.POST, "$baseUrl/refresh")
                .header("X-Auth-Token", refreshToken))
                .header("Content-Type", ContentType.APPLICATION_JSON.toHeaderValue())

        if (response.status.successful) {
            val result = codec.decode(response.bodyString(), RefreshResult::class)
            accessToken = result.accessToken

            println("Got new access token")
        } else if (response.status == Status.UNAUTHORIZED) { // Refresh token expired
            refreshToken = null
            refresh()
        } else {
            throw Intake24ClientException("Access token refresh failed with code ${response.status.code} ${response.status.description}")
        }
    }

    override fun invoke(inner: HttpHandler): HttpHandler {
        return { request ->

            fun retry(): Response {
                val response = inner.invoke(request.header("X-Auth-Token", accessToken))

                if (response.status == Status.UNAUTHORIZED) { // Access token expired
                    refresh()
                    return retry()
                } else {
                    return response
                }
            }

            if (accessToken == null)
                refresh()

            retry()
        }
    }

}
