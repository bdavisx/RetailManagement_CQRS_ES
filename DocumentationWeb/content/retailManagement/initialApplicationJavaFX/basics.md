# Basic Thoughts

## Application Requirements

1. Architecture

    1. JavaFX Front End
    1. Should "back end" be server or fat client?
        1. server based on JSON makes it easier to have other front ends
        1. server based allows more flexibility on where processing takes place
        1. fat client simplifies architecture
        1. fat client makes it harder to scale
        
    * **Going to go with server based on JSON**
             
1. Startup
    1. Throw Up Splash Screen
        Needs to show progress...
    1. Spring configuration

1. Inventory of Items for sale

    1. Item Editing
        1. Add/Edit Item need:
            * barcode
            * name
            * manufacturer / supplier
            * categories
            * dates item is available
        1. Add/Edit Item Delivery Package
            * Delivery Package is how it's shipped to store, 
            * so need number of items in package at a minimum
            * price of package
            * dates item is available in package

    1. Item Pricing 
        * General Pricing - by
            * item
            * supplier
            * category
            * can be done different ways
                * markup from our cost
                    * how to determine cost? average, last, ?

        * Create sale on item - or group of items, lots of ways to do this - supplier, category

1. POS application
    1. menu
        1. start sale
        1. refund
        1. etc...
    1. start sale
        1. take values from bar code to lookup item
        1. multiple items until complete sale
            1. calcs tax
            1. gives total
            1. takes payment

* Getting the data from: http://www.product-open-data.com/download/ (requires attribution) *

