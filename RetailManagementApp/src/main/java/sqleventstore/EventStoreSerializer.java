package sqleventstore;

import com.thoughtworks.xstream.converters.SingleValueConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public interface EventStoreSerializer {
    void registerConverter( SingleValueConverter converter );
    String toXML( Object object );
    ObjectInputStream createObjectInputStream( InputStream inputStream ) throws IOException;
}
