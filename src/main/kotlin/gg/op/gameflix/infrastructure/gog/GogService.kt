package gg.op.gameflix.infrastructure.gog

import gg.op.gameflix.domain.game.GameStoreAuthentication
import gg.op.gameflix.domain.game.GameStoreService
import gg.op.gameflix.domain.game.Store
import gg.op.gameflix.domain.game.Store.GOG

class GogService(
    private val gogClient: GogClient,
): GameStoreService {

    override val store: Store = GOG

    override fun getAllGameSlugsByAuthentication(authentication: GameStoreAuthentication) =
        gogClient.queryGetGamesCode(authentication as GogAuthentication)
            .let { codes -> gogClient.queryGetGames(codes, authentication) }

    override fun supports(authentication: GameStoreAuthentication)
        = authentication is GogAuthentication
}
class GogAuthentication(val token: String) : GameStoreAuthentication

