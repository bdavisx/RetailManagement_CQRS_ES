package ws.billdavis.services.validation.address.transferobjects;

public class AddressText extends PostalCodeText {
    private String addressLines;
    private String locality;
    private String region;

    public String getAddressLines() { return addressLines; }
    public void setAddressLines( final String addressLines ) { this.addressLines = addressLines; }
    public String getLocality() { return locality; }
    public void setLocality( final String locality ) { this.locality = locality; }
    public String getRegion() { return region; }
    public void setRegion( final String region ) { this.region = region; }
}
