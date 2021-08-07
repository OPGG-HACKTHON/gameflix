package gg.op.gameflix.domain.game

interface GameStoreService {
    fun getAllGameSlugsByAuthentication(authentication: GameStoreAuthentication): Collection<GameSlug>
    fun supports(authentication: GameStoreAuthentication): Boolean
}

interface GameStoreAuthentication