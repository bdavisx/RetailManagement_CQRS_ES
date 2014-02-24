package ws.billdavis.retailmanagement.domain.general.location;

public class CreateAddressCommand {
    private String addressLines;
    private String locality;
    private String region;
    private String postalCode;
    private String countryCode;

    public CreateAddressCommand() {}

    public String getAddressLines() { return addressLines; }
    public void setAddressLines( final String addressLines ) { this.addressLines = addressLines; }
    public String getLocality() { return locality; }
    public void setLocality( final String locality ) { this.locality = locality; }
    public String getRegion() { return region; }
    public void setRegion( final String region ) { this.region = region; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode( final String postalCode ) { this.postalCode = postalCode; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode( final String countryCode ) { this.countryCode = countryCode; }
}
