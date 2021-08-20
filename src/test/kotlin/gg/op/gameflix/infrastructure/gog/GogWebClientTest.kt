package gg.op.gameflix.infrastructure.gog

import gg.op.gameflix.infrastructure.gog.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@TestInstance(PER_CLASS)
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [GogConfiguration::class], initializers = [ConfigDataApplicationContextInitializer::class])
internal class GogWebClientTest {

    @Autowired
    private lateinit var configurationProperties: GogConfigurationProperties

    private lateinit var gogClient: GogClient

    @BeforeAll
    fun initializeGogClient() {
        gogClient = GogWebClient(configurationProperties,"Bearer NoWcq3nV8US2g_fhsshqxor6IWg6aoKXPDMsKdsmRlvzZQN6mFeiYh79PgAWl_lVNBW__0faNY0CLhqyttY641tRv1zQqTIGWeywRtD3TsQHr5xbsNY2SidP7APA4vjnPV4BkiHBpdA-OlwAnKxO0PMtPvjE8LunBvwzJ1YwzXDY7ApQrKlkyhJAg2aV7WjQ")
    }

    @Test
    fun `when queryGetGames expect not empty`() {
        assertThat(gogClient.queryGetGames(GogAuthentication()).owned).isNotEmpty
    }

    @Test
    fun `when queryGetGameDetails expect not empty`() {
        assertThat(gogClient.queryGetGameDetails(GogAuthentication(),2078420771).title).isNotEmpty
    }
}