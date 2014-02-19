package ws.billdavis.retailmanagement.domain.vendor;

import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public class CreateVendorCommand {
    @TargetAggregateIdentifier
    @NotNull
    private final UUID vendorId;
}
