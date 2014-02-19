package ws.billdavis.retailmanagement.domain.general.location;

/**
 Based on the following SO post: http://stackoverflow.com/a/930818/1424019.

 Also see http://www.upu.int/en/activities/addressing/postal-addressing-systems-in-member-countries.html.
 */
public class Address {
    private AddressLines addressLines;
    private Locality locality;
    private Region region;
    private PostalCode postalCode;
    private Country country;
}
