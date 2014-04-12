package ws.billdavis.services.validation.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ws.billdavis.services.validation.ValidationConstraintError;
import ws.billdavis.services.validation.ValidationConstraintErrorFactory;
import ws.billdavis.services.validation.address.transferobjects.PostalCodeText;

import java.util.ArrayList;
import java.util.List;

@Component
public class AddressValidationService {
    @Autowired
    private AddressValidationDAO addressValidationDAO;
    @Autowired
    private ValidationConstraintErrorFactory validationConstraintErrorFactory;

    public AddressValidationService() {}

    public AddressValidationService( AddressValidationDAO addressValidationDAO,
        ValidationConstraintErrorFactory validationConstraintErrorFactory ) {
        this.addressValidationDAO = addressValidationDAO;
        this.validationConstraintErrorFactory = validationConstraintErrorFactory;
    }

    public List<ValidationConstraintError> validatePostalCode( final PostalCodeText postalCodeText ) {
        final String countryCode = postalCodeText.getCountryCode();

        List<ValidationConstraintError> errors = new ArrayList<>();
        if( !checkPostalCodeForCorrectFormat( postalCodeText.getPostalCode() ) ) {
            ValidationConstraintError error = validationConstraintErrorFactory.createConstraintError(
                PostalCodeText.class, "PostalCode", postalCodeText.getPostalCode(),
                "The Postal Code is not in the correct format." );
            errors.add( error );
        }

        String adjustedPostalCode = adjustPostalCodeForCorrectLength( postalCodeText.getPostalCode() );

        boolean areThereRecordsForPostalCode = addressValidationDAO.areThereRecordsForPostalCode(
            countryCode, adjustedPostalCode );

        if( !areThereRecordsForPostalCode ) {
            // TODO: setup constraint error factory w/ message resource builder
            ValidationConstraintError error = validationConstraintErrorFactory.createConstraintError(
                PostalCodeText.class, "PostalCode", adjustedPostalCode, "The Postal Code does not exist." );
            errors.add( error );
        }
        return errors;
    }

    private boolean checkPostalCodeForCorrectFormat( final String postalCode ) {
        return false;
    }

    private String adjustPostalCodeForCorrectLength( String postalCode ) {
        if( postalCode.length() == 9 || postalCode.length() == 10 ) {
            postalCode = postalCode.substring( 0, 5 );
        }
        return postalCode;
    }


}
