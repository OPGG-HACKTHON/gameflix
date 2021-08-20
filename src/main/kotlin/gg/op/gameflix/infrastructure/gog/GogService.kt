package gg.op.gameflix.infrastructure.gog

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameStoreAuthentication
import gg.op.gameflix.domain.game.GameStoreService
import java.util.*

class GogService(private val client: GogClient) : GameStoreService {

    override fun getAllGameSlugsByAuthentication(authentication: GameStoreAuthentication): Collection<GameSlug> {

        val getGames: List<Int> = client.queryGetGames(authentication as GogAuthentication).owned

        val resultGames = ArrayList<GameSlug>()

        for(gameKey in getGames){
            val gameName= GameSlug(client.queryGetGameDetails(authentication,gameKey).title)
            resultGames.add(gameName)
        }

        return resultGames
    }


    override fun supports(authentication: GameStoreAuthentication)
        = authentication is GogAuthentication
}
class GogAuthentication : GameStoreAuthentication

