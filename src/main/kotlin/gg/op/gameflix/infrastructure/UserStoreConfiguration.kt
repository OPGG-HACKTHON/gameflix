package gg.op.gameflix.infrastructure

import gg.op.gameflix.domain.game.UserStoreService
import gg.op.gameflix.domain.user.UserGameService
import gg.op.gameflix.infrastructure.blizzard.BlizzardService
import gg.op.gameflix.infrastructure.gog.GogService
import gg.op.gameflix.infrastructure.steam.SteamService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UserStoreConfiguration(
    private val blizzardService: BlizzardService,
    private val gogService: GogService,
    private val steamService: SteamService,
    private val userGameService: UserGameService
) {

    @Bean
    fun userStoreService() : UserStoreService = listOf(blizzardService, gogService, steamService)
            .let { storeServices -> UserStoreService(storeServices, userGameService) }

}