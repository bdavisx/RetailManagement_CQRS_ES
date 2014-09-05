# Address Stories

#### This is currently in a very rough draft state...

I'll say up front that I'm probably over-complicating the address handling and validation (if this was for a client),
    but since we're just starting out, I want to lay a foundation for how Validation is going to be handled since it's
    such an important part of any business application. I want to use "standards" based validation as much as possible,
    but I've always found that it needs additional help once you get beyond the basics (and that's how the standard
    stuff is designed, it can't cover every possibility obviously).

Initially, we want to do the vendor stories. This means handling the addresses for vendors, so we need to work out
    general requirements and validation for addresses. In general, we don't want to force validation on our customers,
    the addresses are too varied and it will just cause problems. We do want to be able to give our customers pluggable
    validation ability if they request it though.

We need to design the address and phone number - I'm not talking about a big design up front, we just need to get a
    general idea of what our addresses are going to encompass, then we can start writing tests for them. We will refine
    our design as we go along.

## Encapsulating the Address Components

You might initially think that everything should be in Strings for the fields - for example, Street Address, Locality
    (City), etc. While this is what you will often see in applications, this often points to the [Primitive Obsession](http://c2.com/cgi/wiki?PrimitiveObsession) smell. To avoid this, you should not
    use primitives (and primitives in this case include Strings) for properties inside of classes. Of course, at some
    point you have to use a primitive, so you encapsulate a single primitive inside of a class. Does this mean that you
    should never have more than one primitive inside of a class? No, but when you use multiple primitives, you need to
    watch for violations of the Single Responsibility Principle. You have to be careful about how many primitives your
    class is going to handle. As we discuss the address requirements, you'll see that it may not be a simple as you
    first thought. This concept is taken to the extreme in the [object calisthenics](http://www.cs.helsinki.fi/u/luontola/tdd-2009/ext/ObjectCalisthenics.pdf) article.
    While I don't plan on following every item in that article, it does have some thought provoking ideas. 

So for each of the types in the address we will use a class:

*   Locality (City)
*   Region (State)
*   Postal Code
*   Country

And this gives us an initial Address class that has the following fields:

<pre class="prettyprint">
public class Address {
    private AddressLines addressLines;
    private Locality locality;
    private Region region;
    private PostalCode postalCode;
    private Country country;
</pre>

Initially we want to support the [G20 countries,](http://en.wikipedia.org/wiki/G-20_major_economies) since
    they have the most money. So this means we need to support their addresses. We're only going to worry about 3 of the
    countries for now, which will hopefully give us enough variety that we can handle changes in the future. We will use
    the following countries (with links to their address information):

*   United States - [http://www.upu.int/fileadmin/documentsFiles/activities/addressingUnit/usaEn.pdf](http://www.upu.int/fileadmin/documentsFiles/activities/addressingUnit/usaEn.pdf)
*   China - [http://www.upu.int/fileadmin/documentsFiles/activities/addressingUnit/chnEn.pdf](http://www.upu.int/fileadmin/documentsFiles/activities/addressingUnit/chnEn.pdf)
*   Germany - [http://www.upu.int/fileadmin/documentsFiles/activities/addressingUnit/deuEn.pdf](http://www.upu.int/fileadmin/documentsFiles/activities/addressingUnit/deuEn.pdf)

Since we want to have pluggable validation, we'll develop the validation for these three countries - but have it
    turned off by default. It will be turned off by default because we don't want to force the validation on our
    customers. There are invariably some addresses that don't fit the "validation rules" that each postal service has,
    but the mail usually ends up getting to those addresses anyway. Normally, I would recommend to a business customer
    that they only have the most minimal validation on the addresses possible unless they really need something more. We
    are going to use the validation as a starting point for our exploration though. The validation is going to be based
    on the documents linked above for the most part. We will assume that we can validate any part of an address in a
    pluggable fashion.

## Validate the Commands, not the Events

We have to validate the address commands we create, because we can't validate the events - this is how you have to
    handle any validation when working with Axon. This is a very important concept to understand. Events have to be
    applied to the aggregates, they can't be rejected. Imagine what would happen if you originally applied an event to
    an aggregate, and then for whatever reason didn't apply it when the events were being replayed from the event store.
    Now you've got a real problem, the event stream is invalid, what would you do? Start the whole system over and erase
    everything? So you have to be able to apply any events once the commands are processed and the events are created.
    If you discover that an event is invalid, you have to create a [compensating event](http://blog.jonathanoliver.com/cqrs-event-sourcing-and-immutable-data/) (or
    multiple compensating events) to undo whatever the event changed that was wrong.

While our domain objects are encapsulating the Strings, the commands do not, so the validators will need to handle
    Strings. Below is an initial cut at a CreateAddressCommand. We can't really use any Bean Validation / Hibernate
    Validation annotations because our validation has to change based on both the country, and the possibility that our
    customers want to turn it off. We also can't use the XML configuration from Hibernate Validator because it only
    allows a class to be configured once for validation - so we couldn't have one validation for the US and one for
    China if we were going to use the XML configuration. We could work around that by having a single validation class
    that actually handled calling the "real" validators, but I'm leaning towards handling it a little differently (I
    could end up changing my mind, this is an exploration). We also want to be able to use the same validation on the
    front end if at all possible.

<pre class="prettyprint">public class CreateAddressCommand {
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
</pre>

If we want to be able to validate different objects with the same validation, we generally are going to want to have
    an Interface that our validator can use. So we will go ahead and extract an interface off the CreateAddressCommand.
    Then we can start to build a our validators and see what kind of commonalities they have.

A question to consider is: Should we have an interface for each Class? In other words, should there be a validator
    for the AddressLines, Locality, Region, etc? Or should there just be one validator that handles the entire Address
    String interface? Since we are considering pluggable validation, and that the validation can be different for each
    class depending on the Country, I'm thinking that we should have an overall address interface, as well as interfaces
    for each individual class. This follows the [Interface
        Segregation Principle](http://en.wikipedia.org/wiki/Interface_segregation_principle) that a class shouldn't have to depend on an interface that has more methods than the
    class needs. In other words, if you have a PostalCode validator class, and the only thing the validator needs is a
    Postal Code to do its validation, then it shouldn't be working with the entire Address interface that includes a
    bunch of other methods.

address lines, locality, region, postal code, country code? Has address

how do we know what to pass the validator? In other words how do we know which interfaces it is interested in. Our
    choices are reflection, interface implementation or annotations reflection in this case means going through an
    entire class to find methods that take a particular has interface: this doesn't seem like a good idea because it
    could pick up a method that is it really meant for validation but just takes one of the has interface classes as a
    parameter that leaves us with interface implementation and annotations I'm leaning towards interface implementation
    because it is more explicit.

Examples: ValidatesAddress, ValidatesLocality, etc. [ValidatesAddressComponent as a "parent" interface (don't like
    the Component part of the name.

So the next decision is how to know when to apply a particular validator against a particular address the following
    for example could come into play:

*   Country Code - the validator is designed for US addresses only.
*   Context of Address - we might have different validation for vendors versus customers versus what else?
*   Other address values – specific validator for certain cities or regions.

We need an AddressValidatorChain. Validators will register with the chain and then each one will get passed addresses
    that need to be validated. The Validators themselves will need to decide if they should validate a particular
    address. Technically this may violate the SRP, but the actual decision whether or not to validate can be delegated
    to an object held within the validator to mitigate that particular issue. The validation method(s) will return the
    same values as a JSR 303 Bean validator. We will need some way to pass in the context of the address to the
    validators to help them decide whether or not they should validate. Remember that we may want some validators
    activated only for particular types of addresses.

Not sure how to handle the contexts, some type of outside object (or closure); or an enum. I prefer to not use enums
    in a case like this, because that generally will violate the Open Closed Principal. This is because to add a new
    context we would have to basically add a new a new enum value. Whereas if we can figure out a way to do it with an
    interface or a closure than we basically just have to add a new interface implementation or closure to the code
    somewhere. We don't have to "open" any other classes up.

The next thing we need to decide is where to hook the validator into Axon. This probably should be handled using a [CommandDispatchInterceptor](http://www.axonframework.org/docs/2.1/command-handling.html#d5e591). See the
    Axon docs for how standard being validation on commands is handled and hooked into the command chain within a handle
    ours in a similar manner.

So do we need an annotation on commands that should have address validation performed? If the ValidatorChain is hooked
    into the command dispatch chain, it needs some way to know that the command contains an address to validate. We
    shouldn't have to change it each time we add an address class (the OCP again).

## PostalCode Validators

Let's start out with validating PostalCodes for the three countries mentioned above. For a US address, it consists
    of either 5 or 9 digits. If it is 9 digits, there should be a dash between the 5th and 6th digits, like this:
    '12345-6789'. I'm not a big fan of forcing the user to actually enter things in the correct format if the
    computer is capable of fixing the format. In other words, if the computer can insert the '-' between the
    digits, then let it. Forcing the user to enter anything exactly right when the computer is capable of correcting
    the misformat is a really bad UX design.

Just because the PostalCode is valid in terms of digits, that doesn't mean it's a valid US PostalCode. The US does
    not use all combinations of PostalCodes. So we need to decide (or in the real world the Customer needs to decide)
    whether or not to validate against a PostalCode database, or just check the format. We're going to go with the
    database method, since that would be the best way to do it, assuming that the database is up to date (I don't know
    how often the US Postal Service updates postal codes). A quick search for 'free postal code database'
    returns [http://download.geonames.org/export/zip/](http://download.geonames.org/export/zip/).
    So we will download the database and figure out how to import it into Postgres. In the real world, we would probably
    consider subscribing to a commercial version if it appeared to be more up to date, but this will do for now.

If you want to read about importing and creating the postal code file, it's on [this page](postal-code-database-import.html). Otherwise let's continue with building our validator.

# Creating Components/Services for the System

As I'm getting to the point of putting some "real" code down, I'm thinking about how to split things of project wise
    (or module's in the case of IntelliJ). It seems obvious that the address validation can be usable from a lot of
    different areas of the system - vendors, customers, stores, etc. So it should be a "component". But how are we going
    to define component as far as the system is concerned.

A monolithic system is not going to work, period. I've already discussed the fact that there are going to be
    different pieces of the system that could be written using different front end architectures, so a single giant
    application isn't going to work. So then we need to decide how to group the different items into components /
    services.

I think that since the address validation can be used from many different places, it should be it's own service. So
    I'm going to create a new IntelliJ module (equivalent to an Eclipse project) for the address validation. In this
    case we're going to implement something of a [micro-service](http://yobriefca.se/blog/2013/04/29/micro-service-architecture/). While I'm not
    advocating that the entire system be composed of micro-services (although perhaps I'll move that way as we go
    along), in this case it seems like the perfect solution. It's tempting to use [DropWizard](http://dropwizard.codahale.com/) for this part of the system, but I'm a fan of consistency,
    and unless we switch to DropWizard for everything, then I don't want to use it for just one piece.

So I'm going to build a small service with Spring. I'm going to use JDBC instead of Hibernate for this part, since
    the service will mainly be doing lookups.

Version 1 is a simple spike to explore a possible interface. It does not have tests and only handles very simple
    input. From here, we will build the actual service that can handle multiple countries with pluggable validators.
