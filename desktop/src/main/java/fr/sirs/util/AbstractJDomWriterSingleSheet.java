package fr.sirs.util;

import static fr.sirs.util.JRUtils.ATT_CLASS;
import static fr.sirs.util.JRUtils.ATT_NAME;
import static fr.sirs.util.JRUtils.TAG_FIELD;
import static fr.sirs.util.JRUtils.TAG_FIELD_DESCRIPTION;
import static fr.sirs.util.JRUtils.getCanonicalName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Optional;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public abstract class AbstractJDomWriterSingleSheet extends AbstractJDomWriter {

    protected File output;
    
    public AbstractJDomWriterSingleSheet() {
        super();
    }

    public AbstractJDomWriterSingleSheet(final InputStream stream) throws ParserConfigurationException, SAXException, IOException{
        super(stream);
    }

    /**
     * <p>This method writes the fiels user by the Jasper Reports template.</p>
     * @param method must be a setter method starting by "set"
     */
    protected void writeField(final Method method) {

        // Builds the name of the field.----------------------------------------
        final String fieldName = method.getName().substring(3, 4).toLowerCase()
                        + method.getName().substring(4);

        // Creates the field element.-------------------------------------------
        final Element field = document.createElement(TAG_FIELD);
        field.setAttribute(ATT_NAME, fieldName);

        final Optional<String> canonicalName = getCanonicalName(method.getParameterTypes()[0]);
        if(canonicalName.isPresent()) field.setAttribute(ATT_CLASS, canonicalName.get());

        final Element fieldDescription = document.createElement(TAG_FIELD_DESCRIPTION);
        final CDATASection description = document.createCDATASection("Mettre ici une description du champ.");

        // Builds the DOM tree.-------------------------------------------------
        fieldDescription.appendChild(description);
        field.appendChild(fieldDescription);
        root.appendChild(field);
    }

    /**
     * <p>This method sets the output to write the modified DOM in.</p>
     * @param output
     */
    public void setOutput(final File output) {
        this.output = output;
    }
}
