package gg.op.gameflix.application.web.security

import gg.op.gameflix.application.web.security.WithMockGoogleUserSecurityContextFactory.Companion.MOCK_USER_EMAIL_DEFAULT
import gg.op.gameflix.application.web.security.WithMockGoogleUserSecurityContextFactory.Companion.MOCK_USER_ID_DEFAULT
import gg.op.gameflix.domain.user.User
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory
import kotlin.annotation.AnnotationRetention.RUNTIME

@Retention(RUNTIME)
@WithSecurityContext(factory = WithMockGoogleUserSecurityContextFactory::class)
annotation class WithMockGoogleUser(

    val sub: String = MOCK_USER_ID_DEFAULT,
    val email: String = MOCK_USER_EMAIL_DEFAULT
)

class WithMockGoogleUserSecurityContextFactory: WithSecurityContextFactory<WithMockGoogleUser> {

    companion object {
        const val MOCK_USER_ID_DEFAULT = "user-id-default"
        const val MOCK_USER_EMAIL_DEFAULT = "user@email.com"
        val MOCK_USER_DEFAULT = User(MOCK_USER_ID_DEFAULT, MOCK_USER_EMAIL_DEFAULT)
    }

    override fun createSecurityContext(annotation: WithMockGoogleUser): SecurityContext
        = SecurityContextHolder.createEmptyContext()
            .apply { authentication = UserAuthenticationToken(User(annotation.sub, annotation.email)) }
}