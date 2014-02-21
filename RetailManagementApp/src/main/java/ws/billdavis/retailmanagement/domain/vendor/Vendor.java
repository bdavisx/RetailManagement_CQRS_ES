package ws.billdavis.retailmanagement.domain.vendor;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;
import ws.billdavis.retailmanagement.domain.general.location.Address;
import ws.billdavis.retailmanagement.domain.general.communication.PhoneNumber;

import java.util.UUID;

public class Vendor extends AbstractAnnotatedAggregateRoot {
    @AggregateIdentifier
    private UUID vendorId;

    private String vendorName;
    private Address mainAddress;
    private PhoneNumber mainPhoneNumber;

    @CommandHandler
    public Vendor( CreateVendorCommand command ) {

//        final CustomerCreatedEvent event =
//            new CustomerCreatedEvent( command.getCustomerId(), command.getCustomerName() );
//        event.setCustomerAddress( command.getCustomerAddress() );
//        event.setDoesCustomerRecycle( command.getDoesCustomerRecycle() );
//        event.setShouldCansBeTakenIn( command.getShouldCansBeTakenIn() );
//        event.setShouldCansBeTakenOut( command.getShouldCansBeTakenOut() );
//        apply( event );
    }


}
