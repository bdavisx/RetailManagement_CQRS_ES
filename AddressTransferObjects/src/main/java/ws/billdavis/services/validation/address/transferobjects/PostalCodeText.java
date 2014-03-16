package ws.billdavis.services.validation.address.transferobjects;

public class PostalCodeText extends CountryCodeText {
    private String postalCode;

    public String getPostalCode() { return postalCode; }
    public void setPostalCode( final String postalCode ) { this.postalCode = postalCode; }
}
