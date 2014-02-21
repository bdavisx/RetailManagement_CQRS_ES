package ws.billdavis.retailmanagement.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.annotation.TransactionManagementConfigurer
import ws.billdavis.retailmanagement.Application
import javax.sql.DataSource
import java.util.Properties

open class JpaConfig() : TransactionManagementConfigurer {
    private var driver: String = null
    private var url: String = null
    private var username: String = null
    private var password: String = null
    private var dialect: String = null
    private var hbm2ddlAuto: String = null
    public open fun configureDataSource(): DataSource {
        val dataSource = DriverManagerDataSource()
        dataSource.setDriverClassName(driver)
        dataSource.setUrl(url)
        dataSource.setUsername(username)
        dataSource.setPassword(password)
        return dataSource
    }
    public open fun configureEntityManagerFactory(): LocalContainerEntityManagerFactoryBean {
        val entityManagerFactoryBean = LocalContainerEntityManagerFactoryBean()
        entityManagerFactoryBean.setDataSource(configureDataSource())
        entityManagerFactoryBean.setPackagesToScan("ws.billdavis")
        entityManagerFactoryBean.setJpaVendorAdapter(HibernateJpaVendorAdapter())
        val jpaProperties = Properties()
        jpaProperties.put(org.hibernate.cfg.Environment.DIALECT, dialect)
        jpaProperties.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, hbm2ddlAuto)
        entityManagerFactoryBean.setJpaProperties(jpaProperties)
        return entityManagerFactoryBean
    }
    public override fun annotationDrivenTransactionManager(): PlatformTransactionManager {
        return JpaTransactionManager()
    }


}
