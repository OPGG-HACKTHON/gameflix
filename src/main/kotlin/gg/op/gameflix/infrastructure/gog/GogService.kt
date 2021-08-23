package gg.op.gameflix.infrastructure.gog

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameStoreAuthentication
import gg.op.gameflix.domain.game.GameStoreService
import java.util.*

class GogService(private val client: GogClient) : GameStoreService {
    override fun getAllGameSlugsByAuthentication(authentication: GameStoreAuthentication): Collection<GameSlug> =
        client.queryGetGames()

    override fun supports(authentication: GameStoreAuthentication)
        = authentication is GogAuthentication
}
class GogAuthentication(val token: String) : GameStoreAuthentication

