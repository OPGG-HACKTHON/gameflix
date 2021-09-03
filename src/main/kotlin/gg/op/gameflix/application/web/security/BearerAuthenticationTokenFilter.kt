package gg.op.gameflix.application.web.security

import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.text.RegexOption.IGNORE_CASE

class BearerAuthenticationTokenFilter: OncePerRequestFilter() {

    companion object {
        private val REGEX_BEARER_TOKEN = Regex("^Bearer ([a-zA-Z0-9-._~+/]+=*)\$", IGNORE_CASE)
    }

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse,
        filterChain: FilterChain) {
        with(request) {
            if (containsBearerToken()) {
                parseBearerToken()
                    .let { stringToken -> BearerAuthenticationToken(stringToken) }
                    .also { SecurityContextHolder.getContext().authentication = it }
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun HttpServletRequest.containsBearerToken() =
        getHeader(AUTHORIZATION)
            ?.startsWith("Bearer ", ignoreCase = true) ?: false

    private fun HttpServletRequest.parseBearerToken() =
        getHeader(AUTHORIZATION)
            .takeIf { it.matches(REGEX_BEARER_TOKEN) }
            ?.substring("Bearer ".length) ?: throw BadCredentialsException("Malformed Bearer token")
}

class BearerAuthenticationToken(private val token: String) : AbstractAuthenticationToken(null) {

    override fun getPrincipal() = token

    override fun getCredentials() = null

}