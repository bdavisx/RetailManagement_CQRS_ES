package ws.billdavis.account

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured
import org.springframework.stereotype.Controller
import org.springframework.util.Assert
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import java.security.Principal

open class AccountController(accountRepository: AccountRepository) {
    private var accountRepository: AccountRepository = null
    public open fun accounts(principal: Principal): Account {
        Assert.notNull(principal)
        return accountRepository.findByEmail(principal.getName())
    }
    {
        this.accountRepository = accountRepository
    }

}
