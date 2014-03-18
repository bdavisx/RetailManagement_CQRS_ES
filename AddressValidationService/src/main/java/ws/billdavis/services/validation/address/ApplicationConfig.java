package ws.billdavis.services.validation.address;

import org.postgresql.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.stereotype.Controller;

import javax.sql.DataSource;

import static org.springframework.context.annotation.ComponentScan.Filter;

@Configuration
@ComponentScan(basePackageClasses = Application.class,
    excludeFilters = @Filter({Controller.class, Configuration.class}))
class ApplicationConfig {

//	@Bean
//	public static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
//		PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
//		ppc.setLocation(new ClassPathResource("/persistence.properties"));
//		return ppc;
//	}

    @Bean
    public AddressValidationService addressValidationService() {
        return new AddressValidationService( );
    }

    @Bean
    public AddressValidationDAO addressValidationDAO() {
        return new AddressValidationDAO( dataSource() );
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        DataSource dataSource = dataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate( dataSource );
        return jdbcTemplate;
    }

    @Bean
    public DataSource dataSource() {
        // TODO: make configurable thru properties
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass( Driver.class );
        dataSource.setUsername( "postgres" );
        dataSource.setPassword( "c0m5Unix" );
        dataSource.setUrl( "jdbc:postgresql://localhost:5432/retail_management" );
        return dataSource;
    }
}
