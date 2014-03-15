package ws.billdavis.services.validation.address.transferobjects


public open class AddressText : PostalCodeText() {
    public var addressLines: String = ""
    public var locality: String = ""
    public var region: String = ""
}
