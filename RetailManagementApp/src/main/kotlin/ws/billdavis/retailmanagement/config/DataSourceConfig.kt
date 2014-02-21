package ws.billdavis.retailmanagement.config

import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

public trait DataSourceConfig {
    open fun dataSource(): DataSource


}
