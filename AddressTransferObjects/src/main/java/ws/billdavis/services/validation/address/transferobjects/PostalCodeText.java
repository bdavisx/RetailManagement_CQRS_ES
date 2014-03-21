package ws.billdavis.services.validation.address.transferobjects;

public class PostalCodeText extends CountryCodeText {
    private String postalCode;

    public PostalCodeText() {}

    public PostalCodeText( final String countryCode, final String postalCode ) {
        super( countryCode );
        this.postalCode = postalCode;
    }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode( final String postalCode ) { this.postalCode = postalCode; }
}
