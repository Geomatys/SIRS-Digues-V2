package fr.sirs.util;

import fr.sirs.SIRS;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
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
    
    
    protected static final String NULL_REPLACEMENT = "Non renseigné";
    protected static final String TRUE_REPLACEMENT = "Oui";
    protected static final String FALSE_REPLACEMENT = "Non";
    
    public AbstractJDomWriter(){
        document=null;
        root=null;
    }
    
    public AbstractJDomWriter(final InputStream stream) throws ParserConfigurationException, SAXException, IOException{
        
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setCoalescing(false);
        factory.setNamespaceAware(true); 
        final DocumentBuilder constructeur = factory.newDocumentBuilder();
        
        document = constructeur.parse(stream);
        stream.close();
        root = document.getDocumentElement();
    }
}
