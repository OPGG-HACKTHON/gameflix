package gg.op.gameflix.domain.game

import gg.op.gameflix.domain.user.User
import gg.op.gameflix.domain.user.UserGameService

class UserStoreService(
    private val storeServices: Collection<GameStoreService>,
    private val userGameService: UserGameService
) {
    fun connectUserWithStore(user: User, auth: GameStoreAuthentication): User =
        storeServices.find { storeService -> storeService.supports(auth) }
            ?.let { userGameService.addAllGamesToUserStore(user, it.getAllGameSlugsByAuthentication(auth), it.store) }
            ?: throw IllegalStateException("Store service not found")
}