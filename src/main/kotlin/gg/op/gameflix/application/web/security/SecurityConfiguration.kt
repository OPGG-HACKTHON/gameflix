package gg.op.gameflix.application.web.security

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import gg.op.gameflix.domain.user.UserRepository
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.logout.LogoutFilter

@EnableConfigurationProperties(SecurityConfigurationProperties::class)
@EnableWebSecurity
class SecurityConfiguration(
    private val properties: SecurityConfigurationProperties,
    private val userRepository: UserRepository
): WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests {
                auth -> auth.antMatchers("/swagger-ui/**", "/games/**").permitAll()
                            .anyRequest().authenticated() }
            .addFilterBefore(bearerAuthenticationTokenFilter(), LogoutFilter::class.java)
            .authenticationProvider(bearerAuthenticationProvider())
            .exceptionHandling().authenticationEntryPoint(HttpStatusEntryPoint(UNAUTHORIZED)).and()
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .sessionManagement().sessionCreationPolicy(STATELESS)
    }

    fun bearerAuthenticationTokenFilter() = BearerAuthenticationTokenFilter()

    fun bearerAuthenticationProvider()
        = BearerAuthenticationProvider(googleIdTokenVerifier(), userRepository)

    private fun googleIdTokenVerifier(): GoogleIdTokenVerifier =
        GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(setOf(properties.clientId))
            .build()
}

@ConstructorBinding
@ConfigurationProperties("security")
class SecurityConfigurationProperties(
    val clientId: String
)

