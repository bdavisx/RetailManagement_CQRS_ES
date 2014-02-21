package ws.billdavis.retailmanagement.config

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.stereotype.Controller
import org.springframework.validation.Validator
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.thymeleaf.extras.springsecurity3.dialect.SpringSecurityDialect
import org.thymeleaf.spring4.SpringTemplateEngine
import org.thymeleaf.spring4.view.ThymeleafViewResolver
import org.thymeleaf.templateresolver.ServletContextTemplateResolver
import org.thymeleaf.templateresolver.TemplateResolver
import ws.billdavis.retailmanagement.Application
import org.springframework.context.annotation.ComponentScan.Filter

open class WebMvcConfig() : WebMvcConfigurationSupport() {
    public override fun requestMappingHandlerMapping(): RequestMappingHandlerMapping {
        val requestMappingHandlerMapping = super.requestMappingHandlerMapping()
        requestMappingHandlerMapping.setUseSuffixPatternMatch(false)
        requestMappingHandlerMapping.setUseTrailingSlashMatch(false)
        return requestMappingHandlerMapping
    }
    public open fun messageSource(): MessageSource {
        val messageSource = ReloadableResourceBundleMessageSource()
        messageSource.setBasename(MESSAGE_SOURCE)
        messageSource.setCacheSeconds(5)
        return messageSource
    }
    public open fun templateResolver(): TemplateResolver {
        val templateResolver = ServletContextTemplateResolver()
        templateResolver.setPrefix(VIEWS)
        templateResolver.setSuffix(".html")
        templateResolver.setTemplateMode("HTML5")
        templateResolver.setCacheable(false)
        return templateResolver
    }
    public open fun templateEngine(): SpringTemplateEngine {
        val templateEngine = SpringTemplateEngine()
        templateEngine.setTemplateResolver(templateResolver())
        templateEngine.addDialect(SpringSecurityDialect())
        return templateEngine
    }
    public open fun viewResolver(): ThymeleafViewResolver {
        val thymeleafViewResolver = ThymeleafViewResolver()
        thymeleafViewResolver.setTemplateEngine(templateEngine())
        thymeleafViewResolver.setCharacterEncoding("UTF-8")
        return thymeleafViewResolver
    }
    public override fun getValidator(): Validator {
        val validator = LocalValidatorFactoryBean()
        validator.setValidationMessageSource(messageSource())
        return validator
    }
    public override fun addResourceHandlers(registry: ResourceHandlerRegistry): Unit {
        registry.addResourceHandler(RESOURCES_HANDLER).addResourceLocations(RESOURCES_LOCATION)
    }
    public override fun configureDefaultServletHandling(configurer: DefaultServletHandlerConfigurer): Unit {
        configurer.enable()
    }

    class object {
        private val MESSAGE_SOURCE: String = "/WEB-INF/i18n/messages"
        private val VIEWS: String = "/WEB-INF/views/"
        private val RESOURCES_HANDLER: String = "/resources/"
        private val RESOURCES_LOCATION: String = RESOURCES_HANDLER + "**"
    }
}
