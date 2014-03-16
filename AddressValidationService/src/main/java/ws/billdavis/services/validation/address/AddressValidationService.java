package ws.billdavis.services.validation.address;

import ws.billdavis.services.validation.ValidationConstraintError;
import ws.billdavis.services.validation.address.transferobjects.PostalCodeText;

import java.util.ArrayList;
import java.util.List;

public class AddressValidationService {
    public List<ValidationConstraintError> validatePostalCode( final PostalCodeText postalCode ) {
        final ArrayList<ValidationConstraintError> validationConstraintErrors = new ArrayList<>();
        final ValidationConstraintError error = new ValidationConstraintError();
        error.setInvalidValue( "Some Invalid Value" );
        error.setItemBeingValidatedName( "SomeInvalidItemName" );
        error.setMessage( "Some error message." );
        error.setPropertyPath( "SomePropertyPath" );
        validationConstraintErrors.add( error );
        return validationConstraintErrors;
    }
}
