package gg.op.gameflix.application.web.security

import org.assertj.core.api.Assertions.assertThatNoException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.BDDMockito.times
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.security.authentication.BadCredentialsException
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@TestInstance(PER_CLASS)
@ExtendWith(MockitoExtension::class)
internal class BearerAuthenticationTokenFilterTest {

    private val bearerAuthenticationTokenFilter = BearerAuthenticationTokenFilter()

    @Mock
    private lateinit var request: HttpServletRequest
    @Mock
    private lateinit var response: HttpServletResponse
    @Mock
    private lateinit var filterChain: FilterChain

    @Test
    fun `when request not contains authorization header expect do nothing`() {
        `when`(request.getHeader(AUTHORIZATION)).thenReturn(null)

        assertThatNoException().isThrownBy { bearerAuthenticationTokenFilter.doFilter(request, response, filterChain) }
    }

    @Test
    fun `given request does not contains authorization header then filterChain should do filter`() {
        given(request.getHeader(AUTHORIZATION)).willReturn(null)

        bearerAuthenticationTokenFilter.doFilter(request, response, filterChain)

        then(filterChain).should(times(1)).doFilter(request, response)
    }

    @Test
    fun `when request contains different authorization header expect do nothing`() {
        `when`(request.getHeader(AUTHORIZATION)).thenReturn("Basic BASIC_AUTH_TOKEN")

        assertThatNoException().isThrownBy { bearerAuthenticationTokenFilter.doFilter(request, response, filterChain) }
    }

    @Test
    fun `when request contains Malformed bearer token expect BadCredentialException`() {
        `when`(request.getHeader(AUTHORIZATION)).thenReturn("Bearer TOKEN MAL FORMED")

        assertThatThrownBy { bearerAuthenticationTokenFilter.doFilter(request, response, filterChain) }
            .isInstanceOf(BadCredentialsException::class.java)
    }
}