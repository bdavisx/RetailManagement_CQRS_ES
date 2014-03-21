package ws.billdavis.services.validation.address;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import ws.billdavis.services.validation.ValidationConstraintError;
import ws.billdavis.services.validation.ValidationConstraintErrorFactory;
import ws.billdavis.services.validation.address.transferobjects.PostalCodeText;

import java.util.List;

public class AddressValidationServiceTest {
    public static final String CountryCodeForUS = "US";
    public static final String Valid5DigitUSPostalCode = "61704";
    public static final String Valid9DigitUSPostalCodeWithoutDash = "617045555";
    private AddressValidationDAO addressValidationDAO;
    private ValidationConstraintErrorFactory validationConstraintErrorFactory;
    private AddressValidationService service;

    @Before
    public void setUp() throws Exception {
        addressValidationDAO = mock( AddressValidationDAO.class );
        validationConstraintErrorFactory = new ValidationConstraintErrorFactory();
        service = new AddressValidationService( addressValidationDAO,
            validationConstraintErrorFactory );
    }

    @Test
    public void itShouldValidateUS5DigitCode() throws Exception {
        setupDAOForValid5DigitUSPostalCode();

        final List<ValidationConstraintError> errors =
            service.validatePostalCode( new PostalCodeText( CountryCodeForUS, Valid5DigitUSPostalCode ) );

        assertThat( errors.size(), equalTo( 0 ) );
    }

    @Test
    public void itShouldValidateUS9DigitCode() throws Exception {
        setupDAOForValid5DigitUSPostalCode();

        final List<ValidationConstraintError> errors =
            service.validatePostalCode( new PostalCodeText( CountryCodeForUS, Valid9DigitUSPostalCodeWithoutDash ) );

        assertThat( errors.size(), equalTo( 0 ) );
    }

    private void setupDAOForValid5DigitUSPostalCode() {
        when( addressValidationDAO.areThereRecordsForPostalCode( CountryCodeForUS, Valid5DigitUSPostalCode ) )
            .thenReturn( true );
    }
}



















