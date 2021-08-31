package gg.op.gameflix.infrastructure.google

class GoogleService(private val client: GoogleClient) {

    fun getUserInformation(accessToken: String)
        = client.queryGetUserInformation(accessToken)
}