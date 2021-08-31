package gg.op.gameflix.application.web

import gg.op.gameflix.application.dto.response.PostUserResponse
import gg.op.gameflix.domain.user.UserService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserRestController(private val userService: UserService) {

    @PostMapping()
    fun postUser(@RequestHeader("Authorization") accessToken: String?): PostUserResponse {
        return userService.accessGoogleAccount(accessToken?.split(" ")?.get(1) ?: throw RuntimeException())
    }
}