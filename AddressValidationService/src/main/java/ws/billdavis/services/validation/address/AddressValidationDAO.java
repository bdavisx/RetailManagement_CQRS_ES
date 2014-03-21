package ws.billdavis.services.validation.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class AddressValidationDAO {
    private JdbcOperations jdbcOperations;

    public AddressValidationDAO(  ) {
    }

    public boolean areThereRecordsForPostalCode( String countryCode, String postalCode ) {
        return jdbcOperations.queryForObject(
            "select count(*) from postal_codes where postal_code = ? and country_code = ?;",
            Integer.class, postalCode, countryCode ) > 0;
    }

    @Autowired
    public void setDataSource( final DataSource dataSource ) {
        jdbcOperations = new JdbcTemplate( dataSource );
    }
}
