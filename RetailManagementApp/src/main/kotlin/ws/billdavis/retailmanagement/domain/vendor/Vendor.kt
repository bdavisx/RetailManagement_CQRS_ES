package ws.billdavis.retailmanagement.domain.vendor

import org.axonframework.commandhandling.annotation.CommandHandler
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot
import org.axonframework.eventsourcing.annotation.AggregateIdentifier
import ws.billdavis.retailmanagement.domain.general.location.Address
import ws.billdavis.retailmanagement.domain.general.communication.PhoneNumber
import java.util.UUID

public open class Vendor(command: CreateVendorCommand) : AbstractAnnotatedAggregateRoot<*>() {
    private var vendorId: UUID = null
    private var vendorName: String = null
    private var mainAddress: Address = null
    private var mainPhoneNumber: PhoneNumber = null
    {
        //        final CustomerCreatedEvent event =
        //            new CustomerCreatedEvent( command.getCustomerId(), command.getCustomerName() );
        //        event.setCustomerAddress( command.getCustomerAddress() );
        //        event.setDoesCustomerRecycle( command.getDoesCustomerRecycle() );
        //        event.setShouldCansBeTakenIn( command.getShouldCansBeTakenIn() );
        //        event.setShouldCansBeTakenOut( command.getShouldCansBeTakenOut() );
        //        apply( event );
    }

}
