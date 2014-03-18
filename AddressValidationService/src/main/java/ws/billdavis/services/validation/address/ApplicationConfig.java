package ws.billdavis.services.validation.address;

import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.stereotype.Controller;
import ws.billdavis.services.validation.ValidationConstraintErrorFactory;

import javax.sql.DataSource;

import static org.springframework.context.annotation.ComponentScan.Filter;

@Configuration
@ComponentScan(basePackageClasses = Application.class,
    excludeFilters = @Filter({Controller.class, Configuration.class}))
class ApplicationConfig {

	@Bean
	public static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
		PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
		ppc.setLocation(new FileSystemResource("d:\\source\\jdbcProperties.properties"));
		return ppc;
	}

    @Bean
    public AddressValidationService addressValidationService() {
        return new AddressValidationService( );
    }

    @Bean
    public AddressValidationDAO addressValidationDAO() {
        return new AddressValidationDAO();
    }

    @Bean
    public DataSource dataSource( @Value("${url}") String jdbcUrl, @Value("${username}") String user,
        @Value("${password}") String password) {

        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass( Driver.class );
        dataSource.setUsername( user );
        dataSource.setPassword( password );
        dataSource.setUrl( jdbcUrl );
        return dataSource;
    }

    @Bean
    public ValidationConstraintErrorFactory validationConstraintErrorFactory() {
        // TODO: this s/b in it's own config module in the support library
        return new ValidationConstraintErrorFactory();
    }

}

















