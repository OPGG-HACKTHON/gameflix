package gg.op.gameflix.application.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.data.domain.PageRequest
import org.springframework.data.support.PageableExecutionUtils.getPage

internal class PaginationTest {

    @ValueSource(ints = [0,1,2,3])
    @ParameterizedTest
    fun pagination_test(pageNumber: Int) {
        val list = listOf(1, 2, 3, 4)
        val page = getPage(list, PageRequest.of(pageNumber, 4)) {20}

        assertThat(page.hasNext()).isTrue
        assertThat(page.content).isEqualTo(list)
    }
}