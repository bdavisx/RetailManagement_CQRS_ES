package ws.billdavis.utilities.conversion;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PostalCodeRegExParser {
    private final DocumentBuilderFactory documentBuilderFactory;

    // TODO: this needs to be class based vs. Strings
    private final Supplier<Map<String, String>> containerFactory;

    public PostalCodeRegExParser( final DocumentBuilderFactory documentBuilderFactory,
        final Supplier<Map<String, String>> containerFactory ) {
        this.documentBuilderFactory = documentBuilderFactory;
        this.containerFactory = containerFactory;
    }

    public Map<String, String> parse( final Reader xmlReader ) {
        Map<String, String> regExContainer = new HashMap<>();
        InputSource source = new InputSource( xmlReader );
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(source);

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression postCodeRegexExpression = xpath.compile( "//postCodeRegex" );
            final NodeList postCodeNodes = (NodeList) postCodeRegexExpression.evaluate( document, XPathConstants.NODESET );

            for( int i = 0; i < postCodeNodes.getLength(); i++ ) {
                Element postCodeRegexElement = (Element) postCodeNodes.item( i );
                final String territoryId = postCodeRegexElement.getAttribute( "territoryId" );
                final String postCodeRegex = postCodeRegexElement.getTextContent();
                regExContainer.put( territoryId, postCodeRegex );
            }
        } catch( ParserConfigurationException e ) {
            // TODO: change default exception handling
            e.printStackTrace();
        } catch( SAXException e ) {
            // TODO: change default exception handling
            e.printStackTrace();
        } catch( IOException e ) {
            // TODO: change default exception handling
            e.printStackTrace();
        } catch( XPathExpressionException e ) {
            // TODO: change default exception handling
            e.printStackTrace();
        }

        return regExContainer;
    }
}
