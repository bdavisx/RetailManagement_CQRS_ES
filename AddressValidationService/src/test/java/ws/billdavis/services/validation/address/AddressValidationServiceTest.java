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
    private static final String CountryCodeForUS = "US";
    private static final String Valid5DigitUSPostalCode = "61704";
    private static final String Valid9DigitUSPostalCodeWithoutDash = "617045555";
    private static final String Valid9DigitUSPostalCodeWithDash = "61704-5555";
    private static final String NonExistent5DigitUSPostalCode = "69999";

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

        validateThatThereAreNoErrors( errors );
    }

    @Test
    public void itShouldValidateUS9DigitCodeWithoutDash() throws Exception {
        setupDAOForValid5DigitUSPostalCode();

        final List<ValidationConstraintError> errors =
            service.validatePostalCode( new PostalCodeText( CountryCodeForUS, Valid9DigitUSPostalCodeWithoutDash ) );

        validateThatThereAreNoErrors( errors );
    }

    @Test
    public void itShouldValidateUS9DigitCodeWithDash() throws Exception {
        setupDAOForValid5DigitUSPostalCode();

        final List<ValidationConstraintError> errors =
            service.validatePostalCode( new PostalCodeText( CountryCodeForUS, Valid9DigitUSPostalCodeWithDash ) );

        validateThatThereAreNoErrors( errors );
    }

    @Test
    public void itShouldReturnErrorsForNonExistentUS5DigitCode() throws Exception {
        setupDAOForValid5DigitUSPostalCode();

        final List<ValidationConstraintError> errors =
            service.validatePostalCode( new PostalCodeText( CountryCodeForUS, NonExistent5DigitUSPostalCode ) );

        assertThat( errors, contains( new ValidationContraintErrorAllFieldsMatcher( ValidationConstraintError.class,
            "The Postal Code does not exist.", PostalCodeText.class.getCanonicalName(),
            "PostalCode", NonExistent5DigitUSPostalCode ) ) );
    }

    private void setupDAOForValid5DigitUSPostalCode() {
        when( addressValidationDAO.areThereRecordsForPostalCode( CountryCodeForUS, Valid5DigitUSPostalCode ) )
            .thenReturn( true );
    }

    private void validateThatThereAreNoErrors( final List<ValidationConstraintError> errors ) {
        assertThat( errors.size(), equalTo( 0 ) );
    }


}



















