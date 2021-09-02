package gg.op.gameflix.application.web.security

import gg.op.gameflix.domain.user.User
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/users")
@RestController
class UserRestController {

    @PostMapping
    fun postUsers(@AuthenticationPrincipal user: User) = user
}