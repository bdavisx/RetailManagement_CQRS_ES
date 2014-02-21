package ws.billdavis.factories

import java.util.UUID

public open class UUIDFactory() {
    public open fun create(): UUID {
        return UUID.randomUUID()
    }


}
