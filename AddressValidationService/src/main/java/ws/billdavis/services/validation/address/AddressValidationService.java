package ws.billdavis.services.validation.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ws.billdavis.services.validation.ValidationConstraintError;
import ws.billdavis.services.validation.address.transferobjects.PostalCodeText;

import java.util.ArrayList;
import java.util.List;

@Component
public class AddressValidationService {
    @Autowired
    private AddressValidationDAO addressValidationDAO;

    public AddressValidationService() {
    }

    public List<ValidationConstraintError> validatePostalCode( final PostalCodeText postalCodeText ) {
        final String postalCode = postalCodeText.getPostalCode();
        boolean areThereRecordsForPostalCode = addressValidationDAO.areThereRecordsForPostalCode( postalCode );
        List<ValidationConstraintError> errors = new ArrayList<>();
        if( !areThereRecordsForPostalCode ) {
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

}
