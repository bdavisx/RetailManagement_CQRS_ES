package ws.billdavis.retailmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import ws.billdavis.account.AccountRepository;
import ws.billdavis.account.UserService;

@Configuration
@ImportResource(value = "WEB-INF/spring-security-context.xml")
class SecurityConfig {
    @Bean
    public AccountRepository accountRepository() {
        return new AccountRepository();
    }

	@Bean
	public UserService userService() {
		return new UserService();
	}

	@Bean
	public TokenBasedRememberMeServices rememberMeServices() {
		return new TokenBasedRememberMeServices("remember-me-key", userService());
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new StandardPasswordEncoder();
	}
}
