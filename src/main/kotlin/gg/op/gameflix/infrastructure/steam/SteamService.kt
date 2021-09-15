package gg.op.gameflix.infrastructure.steam

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameStoreAuthentication
import gg.op.gameflix.domain.game.GameStoreService
import gg.op.gameflix.domain.game.Store
import gg.op.gameflix.domain.game.Store.STEAM

class SteamService(
    private val client: SteamClient
) : GameStoreService {

    override val store: Store = STEAM

    override fun getAllGameSlugsByAuthentication(authentication: GameStoreAuthentication)
        = client.queryGetGames(authentication as SteamAuthentication)
            .response.games
            ?.map { it.name }
            ?.map { name -> GameSlug(name) } ?: emptyList()

    override fun supports(authentication: GameStoreAuthentication)
        = authentication is SteamAuthentication
}

data class SteamAuthentication(val steamId: String) : GameStoreAuthentication

