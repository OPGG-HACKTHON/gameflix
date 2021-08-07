package gg.op.gameflix.infrastructure.epicgames

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameStoreAuthentication
import gg.op.gameflix.domain.game.GameStoreService

class EpicGamesService : GameStoreService {
    override fun getAllGameSlugsByAuthentication(authentication: GameStoreAuthentication): Collection<GameSlug> {
        TODO("Not yet implemented")
    }

    override fun supports(authentication: GameStoreAuthentication): Boolean {
        TODO("Not yet implemented")
    }
}