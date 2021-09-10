package gg.op.gameflix.infrastructure.igdb

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.reflect.full.declaredMemberProperties

internal class IGDBClientTest {

    @Test
    fun `when IGDBGame declaredMemberProperties expect all properties exists`() {
        val propertiesExpected = mutableSetOf("id", "name", "slug", "cover", "first_release_date", "updated_at",
            "url", "summary", "genres", "platforms", "total_rating", "total_rating_count", "involved_companies")

        assertThat(IGDBGame::class.declaredMemberProperties.map { it.name }.toSet()).isEqualTo(propertiesExpected)
    }
}