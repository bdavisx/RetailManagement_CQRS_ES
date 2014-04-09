package sqleventstore;

import org.mockito.ArgumentMatcher;

import java.util.UUID;

public class UUIDMatcher extends ArgumentMatcher<UUID> {
    private UUID uuidToMatch;

    public static boolean doUUIDsMatch( UUID lhs, UUID rhs ) {
        return lhs.toString().equals( rhs.toString() );
    }

    public UUIDMatcher( final UUID uuidToMatch ) {
        this.uuidToMatch = uuidToMatch;
    }

    @Override
    public boolean matches( final Object o ) {
        return doUUIDsMatch( uuidToMatch, (UUID) o );
    }
}
