package ws.billdavis.services.validation.address;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.springframework.util.ObjectUtils;
import ws.billdavis.services.validation.ValidationConstraintError;

public class ValidationContraintErrorAllFieldsMatcher extends TypeSafeMatcher<ValidationConstraintError> {
    private String message;
    private String itemBeingValidatedName;
    private String propertyPath;
    private String invalidValue;

    public ValidationContraintErrorAllFieldsMatcher( final Class<?> expectedType, final String message,
        final String itemBeingValidatedName, final String propertyPath, final String invalidValue ) {
        super( expectedType );
        this.message = message;
        this.itemBeingValidatedName = itemBeingValidatedName;
        this.propertyPath = propertyPath;
        this.invalidValue = invalidValue;
    }

    @Override
    protected boolean matchesSafely( final ValidationConstraintError item ) {
        if( item == null ) return false;

        return ObjectUtils.nullSafeEquals( message, item.getMessage() ) &&
            ObjectUtils.nullSafeEquals( itemBeingValidatedName, item.getItemBeingValidatedName() ) &&
            ObjectUtils.nullSafeEquals( propertyPath, item.getPropertyPath() ) &&
            ObjectUtils.nullSafeEquals( invalidValue, item.getInvalidValue() );
    }

    @Override
    public void describeTo( final Description description ) {
        description
            .appendText( "Validation Constraint: message: " ).appendValue( message )
            .appendText( " itemBeingValidatedName: " ).appendValue( itemBeingValidatedName )
            .appendText( " propertyPath: " ).appendValue( propertyPath )
            .appendText( " invalidValue: " ).appendValue( invalidValue );
    }
}
