package ws.billdavis.services.validation.address;

import org.postgresql.Driver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import ws.billdavis.services.validation.ValidationConstraintError;
import ws.billdavis.services.validation.address.transferobjects.PostalCodeText;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AddressValidationService {
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    public AddressValidationService() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass( Driver.class );
        dataSource.setUsername( "postgres" );
        dataSource.setPassword( "c0m5Unix" );
        dataSource.setUrl( "jdbc:postgresql://localhost:5432/retail_management" );
        this.dataSource = dataSource;
        JdbcTemplate jdbcTemplate = new JdbcTemplate( dataSource );
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ValidationConstraintError> validatePostalCode( final PostalCodeText postalCodeText ) {
        final String postalCode = postalCodeText.getPostalCode();
        int numberOfRecords = getCountOfPostalCodeRecords( postalCode );
        List<ValidationConstraintError> errors = new ArrayList<>();
        if( numberOfRecords == 0 ) {
            ValidationConstraintError error = new ValidationConstraintError();
            error.setInvalidValue( postalCode );
            error.setItemBeingValidatedName( PostalCodeText.class.getCanonicalName() );
            error.setPropertyPath( "PostalCode" );
            // TODO: setup message resource
            error.setMessage( "The Postal Code does not exist." );
            errors.add( error );
        }
        return errors;
    }

    private int getCountOfPostalCodeRecords( String postalCode ) {
        return jdbcTemplate.queryForObject( "select count(*) from postal_codes where postal_code = ?;",
            Integer.class, postalCode );
    }
}
