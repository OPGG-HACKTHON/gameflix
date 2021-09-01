package gg.op.gameflix.domain.user

import gg.op.gameflix.application.dto.response.PostUserResponse
import gg.op.gameflix.infrastructure.google.GoogleService
import gg.op.gameflix.infrastructure.google.GoogleUserInformation
import org.springframework.stereotype.Service
import java.lang.RuntimeException

@Service
class UserService(private val userRepository: UserRepository, private val googleService: GoogleService) {

    fun accessGoogleAccount(accessToken: String): PostUserResponse {
        val user: GoogleUserInformation = googleService.getUserInformation(accessToken) ?: throw RuntimeException()
        userRepository.findById(user.id)
            ?: run {
                userRepository.save(User(id = user.id))
            }
        return PostUserResponse(user.id, email = user.email, name = user.name, picture = user.picture )
    }
}