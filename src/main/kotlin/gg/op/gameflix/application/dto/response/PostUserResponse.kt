package gg.op.gameflix.application.dto.response

data class PostUserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val picture: String
)