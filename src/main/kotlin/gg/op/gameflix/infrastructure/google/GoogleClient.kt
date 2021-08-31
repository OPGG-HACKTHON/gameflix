package gg.op.gameflix.infrastructure.google

import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import java.lang.RuntimeException

data class GoogleUserInformation(
    val id: String,
    val email: String,
    val name: String,
    val picture: String
)

sealed interface GoogleClient {
    fun queryGetUserInformation(accessToken: String): GoogleUserInformation?
}

class GoogleWebClient(properties: GoogleConfigurationProperties): GoogleClient {

    override fun queryGetUserInformation(accessToken: String)
        = queryGetGoogleUserInformation(accessToken)

    private val webClient = WebClient.builder()
        .baseUrl(properties.baseUrl)
        .build()

    private fun queryGetGoogleUserInformation(accessToken: String) =
        webClient.get()
            .uri("/oauth2/v1/userinfo?access_token=$accessToken")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus({
                status -> status.is4xxClientError || status.is5xxServerError }, {
                    clientResponse ->
                        clientResponse.bodyToMono(String::class.java)
                            .map { body -> RuntimeException(body) }
            })
            .bodyToMono(GoogleUserInformation::class.java)
            .block()
}