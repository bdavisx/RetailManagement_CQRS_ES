package sqleventstore;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.SingleValueConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class XStreamEventStoreSerializer implements EventStoreSerializer {
    private final XStream xStream;

    public XStreamEventStoreSerializer( final XStream xStream ) {
        this.xStream = xStream;
    }

    public XStreamEventStoreSerializer() {
        this( new XStream() );
    }

    @Override
    public void registerConverter( final SingleValueConverter converter ) {
        xStream.registerConverter( converter );
    }

    @Override
    public String toXML( final Object object ) {
        return xStream.toXML( object );
    }

    @Override
    public ObjectInputStream createObjectInputStream( final InputStream inputStream ) throws IOException {
        return xStream.createObjectInputStream( inputStream );
    }
}
