package ws.billdavis.services.validation.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class AddressValidationDAO {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public AddressValidationDAO( DataSource dataSource ) {
        this.jdbcTemplate = new JdbcTemplate( dataSource );
    }

    public boolean areThereRecordsForPostalCode( String postalCode ) {
        return jdbcTemplate.queryForObject( "select count(*) from postal_codes where postal_code = ?;",
            Integer.class, postalCode ) > 0;
    }
}
