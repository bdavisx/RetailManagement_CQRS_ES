package ws.billdavis.utilities.conversion;

import org.junit.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.MatcherAssert.*;

public class PostalCodeRegExParserTest {
    private final String testData =
"<supplementalData><version/><generation/><postalCodeData><postCodeRegex territoryId=\"TS\">EXPRESSION</postCodeRegex></postalCodeData></supplementalData>";

    @Test
    public void itShouldFindTheCountry() throws Exception {
        final Supplier< Map<String, String> > stringStringMapFactory = () -> new HashMap<String, String>();
        PostalCodeRegExParser parser = new PostalCodeRegExParser( stringStringMapFactory );

        final Reader xmlReader = new StringReader( testData );
        Map< String, String > countryCodeToRegExMap = parser.parse( xmlReader );
        assertThat( countryCodeToRegExMap, hasEntry( "TS", "EXPRESSION" ) );
    }
}
