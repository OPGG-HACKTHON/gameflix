package gg.op.gameflix.application.web.security

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatNoException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.security.authentication.BadCredentialsException
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@TestInstance(PER_CLASS)
@ExtendWith(MockKExtension::class)
internal class BearerAuthenticationTokenFilterTest {

    private val bearerAuthenticationTokenFilter = BearerAuthenticationTokenFilter()

    @RelaxedMockK
    private lateinit var request: HttpServletRequest
    @RelaxedMockK
    private lateinit var response: HttpServletResponse
    @RelaxedMockK
    private lateinit var filterChain: FilterChain

    @Test
    fun `when request not contains authorization header expect do nothing`() {
        every { request.getAttribute(any()) } returns null
        every { request.getHeader(AUTHORIZATION) } returns null

        assertThatNoException().isThrownBy { bearerAuthenticationTokenFilter.doFilter(request, response, filterChain) }
    }

    @Test
    fun `given request does not contains authorization header then filterChain should do filter`() {
        every { request.getAttribute(any()) } returns null
        every { request.getHeader(AUTHORIZATION) } returns null

        bearerAuthenticationTokenFilter.doFilter(request, response, filterChain)

        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `when request contains different authorization header expect do nothing`() {
        every { request.getAttribute(any()) } returns null
        every { request.getHeader(AUTHORIZATION) } returns "Basic BASIC_AUTN_TOKEN"

        assertThatNoException().isThrownBy { bearerAuthenticationTokenFilter.doFilter(request, response, filterChain) }
    }

    @Test
    fun `when request contains Malformed bearer token expect BadCredentialException`() {
        every { request.getAttribute(any()) } returns null
        every { request.getHeader(AUTHORIZATION) } returns "Bearer TOKEN MAL FORMED"

        assertThatThrownBy { bearerAuthenticationTokenFilter.doFilter(request, response, filterChain) }
            .isInstanceOf(BadCredentialsException::class.java)
    }
}