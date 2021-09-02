package gg.op.gameflix.application.web

import gg.op.gameflix.domain.user.User
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/users")
@RestController
class UserRestController {

    @ResponseStatus(CREATED)
    @PostMapping
    fun postUsers(@AuthenticationPrincipal user: User) = user
}