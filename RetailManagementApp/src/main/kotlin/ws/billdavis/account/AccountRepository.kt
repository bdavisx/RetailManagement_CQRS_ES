package ws.billdavis.account

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.PersistenceException
import javax.persistence.EntityManagerFactory

public class AccountRepository() {
    Inject private var entityManagerFactory:EntityManagerFactory? = null
    Inject private var passwordEncoder: PasswordEncoder? = null

    private val FindByEmail : String = "Account.findByEmail"

    public fun save(account: Account): Account {
        account.password = passwordEncoder?.encode(account.password) as String
        entityManagerFactory!!.createEntityManager()!!.persist(account)
        return account
    }

    // screw this, switch to cqrs style
    public fun findByEmail(email: String): Account {
        try {
            return entityManager.createNamedQuery( FindByEmail,
                    javaClass<Account>()).setParameter("email", email).getSingleResult()
        }
        catch (e: PersistenceException) {
            return null
        }

    }


}
