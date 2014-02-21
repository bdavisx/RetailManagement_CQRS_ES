package ws.billdavis.retailmanagement.domain.general.location


/**
Based on the following SO post: http://stackoverflow.com/a/930818/1424019.

Also see http://www.upu.int/en/activities/addressing/postal-addressing-systems-in-member-countries.html.
 */
public open class Address() {
    private var addressLines: AddressLines = null
    private var locality: Locality = null
    private var region: Region = null
    private var postalCode: PostalCode = null
    private var country: Country = null


}
