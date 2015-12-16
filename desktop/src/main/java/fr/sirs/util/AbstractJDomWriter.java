package fr.sirs.util;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class AbstractJDomWriter {

    protected final Document document;
    protected final Element root;

    protected static final String NULL_REPLACEMENT = " - ";
    protected static final String TRUE_REPLACEMENT = "Oui";
    protected static final String FALSE_REPLACEMENT = "Non";

    public AbstractJDomWriter(){
        document=null;
        root=null;
    }

    /**
     *
     * @param stream
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public AbstractJDomWriter(final InputStream stream) throws ParserConfigurationException, SAXException, IOException {

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setCoalescing(false);
        factory.setNamespaceAware(true);

        final DocumentBuilder builder = factory.newDocumentBuilder();

        document = builder.parse(stream);
        stream.close();
        root = document.getDocumentElement();
    }
}
