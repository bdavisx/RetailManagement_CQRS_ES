package ws.billdavis.services.validation.address;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import ws.billdavis.services.validation.ValidationConstraintError;
import ws.billdavis.services.validation.ValidationConstraintErrorFactory;
import ws.billdavis.services.validation.address.transferobjects.PostalCodeText;

import java.util.List;

public class AddressValidationServiceTest {
    public static final String CountryCodeForUS = "US";
    public static final String Valid5DigitUSPostalCode = "61704";

    @Test
    public void itShouldValidateUS5DigitCode() throws Exception {
        AddressValidationDAO dao = mock( AddressValidationDAO.class );
        ValidationConstraintErrorFactory validationConstraintErrorFactory = new ValidationConstraintErrorFactory();

        when( dao.areThereRecordsForPostalCode( CountryCodeForUS, Valid5DigitUSPostalCode ) ).thenReturn( true );

        AddressValidationService service = new AddressValidationService( dao, validationConstraintErrorFactory );

        final List<ValidationConstraintError> errors =
            service.validatePostalCode( new PostalCodeText( CountryCodeForUS, Valid5DigitUSPostalCode ) );

        assertThat( errors.size(), equalTo( 0 ) );
    }
}
