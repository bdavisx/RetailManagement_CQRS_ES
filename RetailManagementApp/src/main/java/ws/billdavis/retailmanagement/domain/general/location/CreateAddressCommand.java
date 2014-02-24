package ws.billdavis.retailmanagement.domain.general.location;

public class CreateAddressCommand implements HasAddress {
    private String addressLines;
    private String locality;
    private String region;
    private String postalCode;
    private String countryCode;

    public CreateAddressCommand() {}

    @Override
    public String getAddressLines() { return addressLines; }
    public void setAddressLines( final String addressLines ) { this.addressLines = addressLines; }

    @Override
    public String getLocality() { return locality; }
    public void setLocality( final String locality ) { this.locality = locality; }

    @Override
    public String getRegion() { return region; }
    public void setRegion( final String region ) { this.region = region; }

    @Override
    public String getPostalCode() { return postalCode; }
    public void setPostalCode( final String postalCode ) { this.postalCode = postalCode; }

    @Override
    public String getCountryCode() { return countryCode; }
    public void setCountryCode( final String countryCode ) { this.countryCode = countryCode; }
}
