package ws.billdavis.services.validation;

public class ValidationConstraintErrorFactory {

    public <T> ValidationConstraintError createConstraintError( final Class<T> typeBeingValidated,
        final String propertyPath, final String invalidValue, final String errorMessage ) {
        ValidationConstraintError error = new ValidationConstraintError();
        error.setInvalidValue( invalidValue );
        error.setItemBeingValidatedName( typeBeingValidated.getCanonicalName() );
        error.setPropertyPath( propertyPath );
        // TODO: setup message resource
        error.setMessage( errorMessage );
        return error;
    }

}
