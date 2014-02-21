package ws.billdavis.account

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.PersistenceException

public open class AccountRepository() {
    private var entityManager: EntityManager = null
    private var passwordEncoder: PasswordEncoder = null
    public open fun save(account: Account): Account {
        account.setPassword(passwordEncoder.encode(account.getPassword()))
        entityManager.persist(account)
        return account
    }
    public open fun findByEmail(email: String): Account {
        try
        {
            return entityManager.createNamedQuery(Account.FIND_BY_EMAIL, javaClass<Account>()).setParameter("email", email).getSingleResult()
        }
        catch (e: PersistenceException) {
            return null
        }

    }


}
