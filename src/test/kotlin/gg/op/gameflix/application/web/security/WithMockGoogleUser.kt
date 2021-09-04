package gg.op.gameflix.application.web.security

import gg.op.gameflix.application.web.security.SecurityTestConfiguration.Companion.MOCK_USER_EMAIL
import gg.op.gameflix.application.web.security.SecurityTestConfiguration.Companion.MOCK_USER_ID
import gg.op.gameflix.domain.user.User
import gg.op.gameflix.domain.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory
import kotlin.annotation.AnnotationRetention.RUNTIME

@Retention(RUNTIME)
@WithSecurityContext(factory = WithMockGoogleUserSecurityContextFactory::class)
annotation class WithMockGoogleUser(
    val sub: String = MOCK_USER_ID,
    val email: String = MOCK_USER_EMAIL,
)

class WithMockGoogleUserSecurityContextFactory(
    private val userRepository: UserRepository) : WithSecurityContextFactory<WithMockGoogleUser> {

    override fun createSecurityContext(annotation: WithMockGoogleUser): SecurityContext
        = SecurityContextHolder.createEmptyContext()
            .apply {
                userRepository.findByIdOrNull(annotation.sub)
                    ?.let { authentication = UserAuthenticationToken(it) }
                    ?: let { authentication = UserAuthenticationToken(User(annotation.sub, annotation.email)) }
            }
}