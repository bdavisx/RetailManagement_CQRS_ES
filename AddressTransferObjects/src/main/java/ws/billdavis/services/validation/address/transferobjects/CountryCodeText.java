package ws.billdavis.services.validation.address.transferobjects;

public class CountryCodeText {
    private String countryCode;

    public CountryCodeText() {}

    public CountryCodeText( final String countryCode ) {
        this.countryCode = countryCode;
    }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode( final String countryCode ) { this.countryCode = countryCode; }
}
