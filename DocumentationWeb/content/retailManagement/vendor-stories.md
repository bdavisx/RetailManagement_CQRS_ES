<h1>Vendor Stories</h1>

<p>Initially we are going to need to create vendors. Vendors are the companies that supply the store(s) with products to
    sell. I'm not completely sure what we will need with respect to Vendors, but I have the following in mind as far as
    what will need to go into a Create Vendor command:
<ul>
    <li>A UUID for the Ids. This allows us to generate the Ids ourselves instead of having a database do it for us. I
        plan on using UUIDs for all objects in the system, not just vendors. This is the <a
            href="http://www.axonframework.org/docs/2.1/command-handling.html#d5e415">recommended approach</a> with Axon
        and I have found it just works better in general whether or not you are using CQRS.
    </li>
    <li>Vendor name.</li>
    <li>An address - the main address for the vendor. They might have many addresses such as warehouses. We will need to
        deal with that in the future.
    </li>
    <li>Main phone number - we will deal with other phone numbers in the future also.</li>
</ul>
</p>

<p>Take a look at the <a href="../../oldHTML/address-stories.html">address stories</a> page to see the initial address code.</p>
