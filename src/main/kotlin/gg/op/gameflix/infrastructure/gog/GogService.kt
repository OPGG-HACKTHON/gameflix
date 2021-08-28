package gg.op.gameflix.infrastructure.gog

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameStoreAuthentication
import gg.op.gameflix.domain.game.GameStoreService

class GogService(private val clientGames: GogClient,private val clientGameList: GogClient) : GameStoreService {
    override fun getAllGameSlugsByAuthentication(authentication: GameStoreAuthentication): Collection<GameSlug> =
        clientGames.queryGetGames(clientGameList.queryGetGamesCode())

    override fun supports(authentication: GameStoreAuthentication)
        = authentication is GogAuthentication
}
class GogAuthentication(val token: String) : GameStoreAuthentication

