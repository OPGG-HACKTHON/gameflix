package gg.op.gameflix.application.web.security

import gg.op.gameflix.domain.user.User
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory
import kotlin.annotation.AnnotationRetention.RUNTIME

@Retention(RUNTIME)
@WithSecurityContext(factory = WithMockGoogleUserSecurityContextFactory::class)
annotation class WithMockGoogleUser(
    val sub: String = "0",
    val email: String = "test-user@gmail.com"
)

class WithMockGoogleUserSecurityContextFactory: WithSecurityContextFactory<WithMockGoogleUser> {

    override fun createSecurityContext(annotation: WithMockGoogleUser): SecurityContext
        = SecurityContextHolder.createEmptyContext()
            .apply { authentication = UserAuthenticationToken(User(annotation.sub, annotation.email)) }
}