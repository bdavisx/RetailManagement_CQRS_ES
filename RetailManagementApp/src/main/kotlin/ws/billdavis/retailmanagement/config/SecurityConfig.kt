package ws.billdavis.retailmanagement.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportResource
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.crypto.password.StandardPasswordEncoder
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices
import ws.billdavis.account.AccountRepository
import ws.billdavis.account.UserService

open class SecurityConfig() {
    public open fun accountRepository(): AccountRepository {
        return AccountRepository()
    }
    public open fun userService(): UserService {
        return UserService()
    }
    public open fun rememberMeServices(): TokenBasedRememberMeServices {
        return TokenBasedRememberMeServices("remember-me-key", userService())
    }
    public open fun passwordEncoder(): PasswordEncoder {
        return StandardPasswordEncoder()
    }


}
