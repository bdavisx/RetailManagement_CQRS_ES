package ws.billdavis.services.validation;

public class ValidationConstraintError {
    private String message;
    private String itemBeingValidatedName;
    private String propertyPath;
    private String invalidValue;

    public String getMessage() { return message; }
    public void setMessage( final String message ) { this.message = message; }
    public String getItemBeingValidatedName() { return itemBeingValidatedName; }
    public void setItemBeingValidatedName( final String itemBeingValidatedName ) { this.itemBeingValidatedName = itemBeingValidatedName; }
    public String getPropertyPath() { return propertyPath; }
    public void setPropertyPath( final String propertyPath ) { this.propertyPath = propertyPath; }
    public String getInvalidValue() { return invalidValue; }
    public void setInvalidValue( final String invalidValue ) { this.invalidValue = invalidValue; }
}
