package ws.billdavis.account

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import javax.annotation.PostConstruct
import java.util.Collections

public open class UserService() : UserDetailsService {
    private var accountRepository: AccountRepository = null
    protected open fun initialize(): Unit {
        accountRepository.save(Account("user", "demo", "ROLE_USER"))
        accountRepository.save(Account("admin", "admin", "ROLE_ADMIN"))
    }
    public override fun loadUserByUsername(username: String): UserDetails {
        val account = accountRepository.findByEmail(username)
        if (account == null)
        {
            throw UsernameNotFoundException("user not found")
        }

        return createUser(account)
    }
    public open fun signin(account: Account): Unit {
        SecurityContextHolder.getContext().setAuthentication(authenticate(account))
    }
    private fun authenticate(account: Account): Authentication {
        return UsernamePasswordAuthenticationToken(createUser(account), null, Collections.singleton(createAuthority(account)))
    }
    private fun createUser(account: Account): User {
        return User(account.getEmail(), account.getPassword(), Collections.singleton(createAuthority(account)))
    }
    private fun createAuthority(account: Account): GrantedAuthority {
        return SimpleGrantedAuthority(account.getRole())
    }


}
