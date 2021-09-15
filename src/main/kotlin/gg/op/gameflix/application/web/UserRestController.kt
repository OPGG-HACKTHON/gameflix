package gg.op.gameflix.application.web

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.Store
import gg.op.gameflix.domain.game.UserStoreService
import gg.op.gameflix.domain.user.User
import gg.op.gameflix.domain.user.UserGameService
import gg.op.gameflix.domain.user.UserRepository
import org.springframework.beans.support.PagedListHolder
import org.springframework.data.domain.Pageable
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
import java.util.Locale

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

@Suppress("kotlin:S1192")
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
    fun getUserGames(pageable: Pageable, @PathVariable id: String, @AuthenticationPrincipal user: User): PagedGameSummaryModel =
        user.games
            .toMutableList()
            .let { gameSummaries -> pagedListHolder(gameSummaries, pageable)}
            .let { PagedGameSummaryModel(it) }

    @PreAuthorize("#id == #user.id")
    @GetMapping("/{id}/games", params = ["search"])
    fun getUserGamesBySearch(pageable: Pageable, @PathVariable id: String, search: String, @AuthenticationPrincipal user: User): PagedGameSummaryModel =
        user.games
            .filter { it.slug.name.contains(search, ignoreCase = true) }
            .toMutableList()
            .let { gameSummaries -> pagedListHolder(gameSummaries, pageable) }
            .let { PagedGameSummaryModel(it) }

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

@RequestMapping("/users")
@RestController
class UserStoreRestController(
    private val userStoreService: UserStoreService
) {
    @PreAuthorize("#id == #user.id")
    @ResponseStatus(CREATED)
    @PostMapping("/{id}/stores")
    fun postUserStores(@PathVariable id: String, @AuthenticationPrincipal user: User, @RequestBody dto: StorePostRequestDTO): UserModel =
        userStoreService.connectUserWithStore(user, dto.store, dto.authentication)
            .let { UserModel(it) }

    @PreAuthorize("#id == #user.id")
    @GetMapping("/{id}/stores")
    fun getUserStores(@PathVariable id: String, @AuthenticationPrincipal user: User): MultipleStoreModel =
        user.games.map { it.store }
            .toCollection(HashSet())
            .map { createStoreModel(it) }
            .let { storeModels -> MultipleStoreModel(storeModels) }

    @PreAuthorize("#id == #user.id")
    @GetMapping("/{id}/stores/{storeSlug}")
    fun getUserStoresBySlug(@PathVariable id: String, @PathVariable storeSlug: String, @AuthenticationPrincipal user: User): StoreModel =
        user.games.map { it.store }
            .find { store -> store == Store.fromSlug(storeSlug) }
            ?.let { store -> createStoreModel(store) } ?: throw NoSuchElementException("No such store in user")

    @PreAuthorize("#id == #user.id")
    @GetMapping("/{id}/stores/{storeSlug}/games")
    fun getUserStoreGames(@PathVariable id: String, @PathVariable storeSlug: String, @AuthenticationPrincipal user: User, pageable: Pageable): PagedGameSummaryModel =
        user.games.filter { it.store == Store.fromSlug(storeSlug) }
            .toMutableList()
            .let { pagedListHolder(it, pageable) }
            .let { page -> PagedGameSummaryModel(page) }

    @PreAuthorize("#id == #user.id")
    @GetMapping("/{id}/stores/{storeSlug}/games", params = ["search"])
    fun getUserStoreGamesBySearch(@PathVariable id: String, @PathVariable storeSlug: String, search: String, @AuthenticationPrincipal user: User, pageable: Pageable): PagedGameSummaryModel =
        user.games.filter { it.store == Store.fromSlug(storeSlug) }
            .filter { it.slug.name.contains(search, ignoreCase = true) }
            .toMutableList()
            .let { pagedListHolder(it, pageable)}
            .let { page -> PagedGameSummaryModel(page) }
}

private fun <T> pagedListHolder(source: List<T>, pageable: Pageable): PagedListHolder<T> {
    return PagedListHolder(source)
        .apply {
            page = pageable.pageNumber
            pageSize = pageable.pageSize
        }
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

data class StorePostRequestDTO(
    val slug: String,
    val authentication: String
) {
    val store: Store = slug.replace("-", "_")
        .uppercase(Locale.getDefault())
        .let { Store.valueOf(it) }
}
