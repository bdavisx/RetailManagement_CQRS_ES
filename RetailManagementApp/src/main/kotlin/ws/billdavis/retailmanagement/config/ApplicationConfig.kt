package ws.billdavis.retailmanagement.config

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Controller
import ws.billdavis.retailmanagement.Application
import org.springframework.context.annotation.ComponentScan.Filter

open class ApplicationConfig() {


    class object {
        public open fun propertyPlaceholderConfigurer(): PropertyPlaceholderConfigurer {
            val ppc = PropertyPlaceholderConfigurer()
            ppc.setLocation(ClassPathResource("/persistence.properties"))
            return ppc
        }
    }
}
