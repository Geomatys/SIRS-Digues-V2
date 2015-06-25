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
    
    public AbstractJDomWriter(){
        document=null;
        root=null;
    }
    
    public AbstractJDomWriter(final InputStream stream) throws ParserConfigurationException, SAXException, IOException{
        
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder constructeur = factory.newDocumentBuilder();
        factory.setNamespaceAware(true);
        
        document = constructeur.parse(stream);
        stream.close();
        root = document.getDocumentElement();
    }
}
