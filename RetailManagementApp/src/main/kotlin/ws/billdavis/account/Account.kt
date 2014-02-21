package ws.billdavis.account

import org.codehaus.jackson.annotate.JsonIgnore
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.NamedQuery
import javax.persistence.Table

public open class Account( val id: Long, var email: String, _password: String) : java.io.Serializable {
    public var role: String = "ROLE_USER"
    public var password: String

    {
        password = _password
    }

}
