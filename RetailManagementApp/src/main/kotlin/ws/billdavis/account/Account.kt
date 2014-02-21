package ws.billdavis.account

import org.codehaus.jackson.annotate.JsonIgnore
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.NamedQuery
import javax.persistence.Table

public open class Account(_FIND_BY_EMAIL: String, _id: Long, _email: String, _password: String) : java.io.Serializable {
    private var id: Long = null
    private var email: String = null
    private var password: String = null
    private var role: String = "ROLE_USER"
    public open fun getId(): Long {
        return id
    }
    public open fun getEmail(): String {
        return email
    }
    public open fun setEmail(email: String): Unit {
        this.email = email
    }
    public open fun getPassword(): String {
        return password
    }
    public open fun setPassword(password: String): Unit {
        this.password = password
    }
    public open fun getRole(): String {
        return role
    }
    public open fun setRole(role: String): Unit {
        this.role = role
    }
    {
        FIND_BY_EMAIL = _FIND_BY_EMAIL
        id = _id
        email = _email
        password = _password
    }
    class object {
        protected open fun init(): Account {
            val __ = Account(0, null, null, null)
            return __
        }
        public open fun init(email: String, password: String, role: String): Account {
            val __ = Account(0, null, email, password)
            this.__.role = role
            return __
        }
        public val FIND_BY_EMAIL: String = "Account.findByEmail"
    }
}
