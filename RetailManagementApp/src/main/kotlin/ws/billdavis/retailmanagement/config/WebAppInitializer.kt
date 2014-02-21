package ws.billdavis.retailmanagement.config

import org.springframework.core.annotation.Order
import org.springframework.web.filter.CharacterEncodingFilter
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer
import javax.servlet.Filter
import javax.servlet.ServletRegistration

public open class WebAppInitializer() : AbstractAnnotationConfigDispatcherServletInitializer() {
    protected override fun getServletMappings(): Array<String> {
        return array<String>("/")
    }
    protected override fun getRootConfigClasses(): Array<Class<*>> {
        return array<Class<*>>(javaClass<ApplicationConfig>(), javaClass<AxonConfiguration>(), javaClass<DataSourceConfig>(), javaClass<JpaConfig>(), javaClass<SecurityConfig>())
    }
    protected override fun getServletConfigClasses(): Array<Class<*>> {
        return array<Class<*>>(javaClass<WebMvcConfig>())
    }
    protected override fun getServletFilters(): Array<Filter> {
        val characterEncodingFilter = CharacterEncodingFilter()
        characterEncodingFilter.setEncoding("UTF-8")
        characterEncodingFilter.setForceEncoding(true)
        return array<Filter>(characterEncodingFilter)
    }
    protected override fun customizeRegistration(registration: ServletRegistration.Dynamic): Unit {
        registration.setInitParameter("defaultHtmlEscape", "true")
        registration.setInitParameter("spring.profiles.active", "default")
    }


}
