package gg.op.gameflix.infrastructure.steam

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameStoreAuthentication
import gg.op.gameflix.domain.game.GameStoreService

class SteamService(private val client: SteamClient) : GameStoreService {

    override fun getAllGameSlugsByAuthentication(authentication: GameStoreAuthentication)
        = client.queryGetGames(authentication as SteamAuthentication)
            .response.games
            .map { it.name }
            .map { name -> GameSlug(name) }

    override fun supports(authentication: GameStoreAuthentication)
        = authentication is SteamAuthentication
}

data class SteamAuthentication(val steamId: String) : GameStoreAuthentication

