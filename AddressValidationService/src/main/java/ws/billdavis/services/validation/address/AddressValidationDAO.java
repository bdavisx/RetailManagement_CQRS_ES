package ws.billdavis.services.validation.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AddressValidationDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public AddressValidationDAO() {
    }

    public boolean areThereRecordsForPostalCode( String postalCode ) {
        return jdbcTemplate.queryForObject( "select count(*) from postal_codes where postal_code = ?;",
            Integer.class, postalCode ) > 0;
    }
}
