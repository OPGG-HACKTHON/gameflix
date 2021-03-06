package gg.op.gameflix.application.web.security

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import gg.op.gameflix.domain.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.HEAD
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.logout.LogoutFilter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class SecurityConfiguration(
    private val securityConfigurationProperties: SecurityConfigurationProperties
): WebSecurityConfigurerAdapter() {

    @Autowired
    private lateinit var bearerAuthenticationProvider: BearerAuthenticationProvider

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests {
                auth -> auth.antMatchers("/swagger-ui/**", "/games/**", "/stores/**","/actuator/**").permitAll()
                            .anyRequest().authenticated() }
            .addFilterBefore(bearerAuthenticationTokenFilter(), LogoutFilter::class.java)
            .authenticationProvider(bearerAuthenticationProvider)
            .exceptionHandling().authenticationEntryPoint(HttpStatusEntryPoint(UNAUTHORIZED)).and()
            .cors().and()
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .sessionManagement().sessionCreationPolicy(STATELESS)
    }

    fun bearerAuthenticationTokenFilter() = BearerAuthenticationTokenFilter()

    @Bean
    fun corsConfigurer() = object : WebMvcConfigurer {
        override fun addCorsMappings(registry: CorsRegistry) {
            registry.addMapping("/**")
                .allowedMethods(*listOf(GET, POST, HEAD, DELETE).map { it.name }.toTypedArray())
                .allowedOrigins(*securityConfigurationProperties.allowedOrigins)
                .allowCredentials(true)
        }
    }

}

@EnableConfigurationProperties(SecurityConfigurationProperties::class)
@Configuration
class BearerTokenAuthenticationConfiguration(
    private val userRepository: UserRepository,
    private val properties: SecurityConfigurationProperties
) {
    @Bean
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
    val clientId: String,
    val allowedOrigins: Array<String>
)

