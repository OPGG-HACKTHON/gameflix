package gg.op.gameflix.application.web

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.user.User
import gg.op.gameflix.domain.user.UserGameService
import gg.op.gameflix.domain.user.UserRepository
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Suppress("kotlin:S1192")
@RequestMapping("/users")
@RestController
class UserRestController(
    private val userRepository: UserRepository
) {
    @ResponseStatus(CREATED)
    @PostMapping
    fun postUsers(@AuthenticationPrincipal user: User): UserModel =
        UserModel(user)

    @PreAuthorize("#id == #user.id")
    @GetMapping("/{id}")
    fun getUsersById(@PathVariable id: String, @AuthenticationPrincipal user: User): UserModel =
        UserModel(user)

    @PreAuthorize("#id == #user.id")
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{id}")
    fun deleteUsersById(@PathVariable id: String, @AuthenticationPrincipal user: User) =
        userRepository.delete(user)
}

@RequestMapping("/users")
@RestController
class UserGameRestController(
    private val userGameService: UserGameService
) {
    @PreAuthorize("#id == #user.id")
    @ResponseStatus(CREATED)
    @PostMapping("/{id}/games")
    fun postUserGames(@PathVariable id: String, @AuthenticationPrincipal user: User,
        @RequestBody requestDTO: GamePostRequestDTO): GameSummaryModel =
        userGameService.addGameToUser(user, GameSlug(requestDTO.slug))
            .let { GameSummaryModel(it) }

    @PreAuthorize("#id == #user.id")
    @GetMapping("/{id}/games")
    fun getUserGames(@PathVariable id: String, @AuthenticationPrincipal user: User): MultipleGameSummaryModel =
        user.games.map { gameSummary -> GameSummaryModel(gameSummary) }
            .let { MultipleGameSummaryModel(it) }

    @PreAuthorize("#id == #user.id")
    @GetMapping("/{id}/games/{slug}")
    fun getUserGamesBySlug(@PathVariable id: String, @AuthenticationPrincipal user: User, @PathVariable slug: String): GameModel =
        userGameService.findGameInUser(user, GameSlug(slug))
            ?.let { GameModel(it) }
            ?: throw NoSuchElementException("No such game($slug) exists in User")

    @PreAuthorize("#id == #user.id")
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{id}/games/{slug}")
    fun deleteUserGamesBySlug(@PathVariable id: String, @AuthenticationPrincipal user: User, @PathVariable slug: String): Unit =
        userGameService.deleteGameInUser(user, GameSlug(slug))
}


data class UserModel(
    val id: String,
    val email: String,
    val games: List<GameSummaryModel>
) {
    constructor(user: User): this(
        user.id,
        user.email,
        user.games.map { GameSummaryModel(it) }
    )
}

data class GamePostRequestDTO(
    val slug: String
)