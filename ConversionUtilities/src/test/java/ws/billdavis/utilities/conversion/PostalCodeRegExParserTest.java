package ws.billdavis.utilities.conversion;

import org.hamcrest.collection.IsMapContaining;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.hamcrest.collection.IsMapContaining.*;
import static org.junit.Assert.*;

public class PostalCodeRegExParserTest {
    private final String testData =
"<supplementalData><version/><generation/><postalCodeData><postCodeRegex territoryId=\"TS\">EXPRESSION</postCodeRegex></postalCodeData></supplementalData>";

    @Test
    public void itShouldFindTheCountry() throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        final Supplier< Map<String, String> > stringStringMapFactory = () -> new HashMap<String, String>();
        PostalCodeRegExParser parser = new PostalCodeRegExParser( documentBuilderFactory,
            stringStringMapFactory );

        final Reader xmlReader = new StringReader( testData );
        Map< String, String > countryCodeToRegExMap = parser.parse( xmlReader );
        assertThat( countryCodeToRegExMap, hasEntry( "TS", "EXPRESSION" ) );
    }
}
