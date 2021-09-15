package gg.op.gameflix.infrastructure

import gg.op.gameflix.domain.game.GameStoreAuthentication
import gg.op.gameflix.domain.game.GameStoreAuthenticationFactory
import gg.op.gameflix.domain.game.Store
import gg.op.gameflix.infrastructure.blizzard.BlizzardAuthentication
import gg.op.gameflix.infrastructure.gog.GogAuthentication
import gg.op.gameflix.infrastructure.steam.SteamAuthentication
import org.springframework.stereotype.Component

@Component
class GameStoreAuthenticationFactoryImpl : GameStoreAuthenticationFactory {
    override fun createGameStoreAuthentication(store: Store, authentication: String): GameStoreAuthentication
        = when(store) {
            Store.STEAM -> SteamAuthentication(authentication)
            Store.GOG -> GogAuthentication(authentication)
            Store.BLIZZARD -> BlizzardAuthentication(authentication)
            Store.ETC -> throw IllegalArgumentException("This value is not used for store authentication")
        }
}