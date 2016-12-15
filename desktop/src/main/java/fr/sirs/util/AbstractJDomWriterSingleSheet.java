/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.util;

import static fr.sirs.SIRS.BUNDLE_KEY_CLASS;
import static fr.sirs.util.JRUtils.ATT_CLASS;
import static fr.sirs.util.JRUtils.ATT_NAME;
import static fr.sirs.util.JRUtils.TAG_BAND;
import static fr.sirs.util.JRUtils.TAG_COLUMN_FOOTER;
import static fr.sirs.util.JRUtils.TAG_COLUMN_HEADER;
import static fr.sirs.util.JRUtils.TAG_DETAIL;
import static fr.sirs.util.JRUtils.TAG_FIELD;
import static fr.sirs.util.JRUtils.TAG_FIELD_DESCRIPTION;
import static fr.sirs.util.JRUtils.TAG_PAGE_FOOTER;
import static fr.sirs.util.JRUtils.TAG_PAGE_HEADER;
import static fr.sirs.util.JRUtils.TAG_STATIC_TEXT;
import static fr.sirs.util.JRUtils.TAG_TEXT;
import static fr.sirs.util.JRUtils.TAG_TITLE;
import static fr.sirs.util.JRUtils.getCanonicalName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
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

    // Template elements.
    protected final Element title;
    protected final Element pageHeader;
    protected final Element columnHeader;
    protected final Element detail;
    protected final Element columnFooter;
    protected final Element pageFooter;

    public AbstractJDomWriterSingleSheet() {
        super();
        title = null;
        pageHeader = null;
        columnHeader = null;
        detail = null;
        columnFooter = null;
        pageFooter = null;
    }

    public AbstractJDomWriterSingleSheet(final InputStream stream) throws ParserConfigurationException, SAXException, IOException{
        super(stream);
        title = (Element) root.getElementsByTagName(TAG_TITLE).item(0);
        pageHeader = (Element) root.getElementsByTagName(TAG_PAGE_HEADER).item(0);
        columnHeader = (Element) root.getElementsByTagName(TAG_COLUMN_HEADER).item(0);
        detail = (Element) root.getElementsByTagName(TAG_DETAIL).item(0);
        columnFooter = (Element) root.getElementsByTagName(TAG_COLUMN_FOOTER).item(0);
        pageFooter = (Element) root.getElementsByTagName(TAG_PAGE_FOOTER).item(0);
    }

    /**
     * <p>This method writes the fiels user by the Jasper Reports template.</p>
     * @param method must be a setter method starting by "set"
     */
    protected void writeField(final Method method) {

        // Builds the name of the field.----------------------------------------
        final String fieldName = method.getName().substring(3, 4).toLowerCase()
                        + method.getName().substring(4);
        writeField(method.getParameterTypes()[0], fieldName, "Champ ajouté par introspection.");
    }

    protected void writeField(final Class type, final String name, final String description){

        // Creates the field element.-------------------------------------------
        final Element field = document.createElement(TAG_FIELD);
        field.setAttribute(ATT_NAME, name);

        final Optional<String> canonicalName = getCanonicalName(type);
        if(canonicalName.isPresent()) field.setAttribute(ATT_CLASS, canonicalName.get());

        final Element fieldDescription = document.createElement(TAG_FIELD_DESCRIPTION);
        final CDATASection desc = document.createCDATASection(description);

        // Builds the DOM tree.-------------------------------------------------
        fieldDescription.appendChild(desc);
        field.appendChild(fieldDescription);
        root.appendChild(field);
    }

    protected void writePageHeader(){
        root.appendChild(pageHeader);
    }

    protected void writeColumnHeader(){
        root.appendChild(columnHeader);
    }

    protected void writePageFooter(){
        root.appendChild(pageFooter);
    }

    protected void writeColumnFooter(){
        root.appendChild(columnFooter);
    }

    /**
     * <p>This method writes the title of the template.</p>
     * @param titlePrefix
     * @param classToMap
     */
    protected void writeTitle(final String titlePrefix, final Class classToMap) {

        if(title==null) return;
        
        // Looks for the title content.-----------------------------------------
        final Element band = (Element) title.getElementsByTagName(TAG_BAND).item(0);
        if(band==null) return;
        
        final Element staticText = (Element) band.getElementsByTagName(TAG_STATIC_TEXT).item(0);
        if(staticText==null) return;
        
        final Element text = (Element) staticText.getElementsByTagName(TAG_TEXT).item(0);
        if(text==null) return;
        
        // Sets the title.------------------------------------------------------
        final String className;
        final ResourceBundle resourceBundle = ResourceBundle.getBundle(classToMap.getName(), Locale.getDefault(),
                Thread.currentThread().getContextClassLoader());
        if(resourceBundle!=null){
            className = (resourceBundle.containsKey(BUNDLE_KEY_CLASS)) ?
                    resourceBundle.getString(BUNDLE_KEY_CLASS) : classToMap.getSimpleName();
        }
        else{
            className = classToMap.getSimpleName();
        }
        ((CDATASection) text.getChildNodes().item(0)).setData(titlePrefix + className);

        // Builds the DOM tree.-------------------------------------------------
        root.appendChild(title);
    }

    /**
     * <p>This method sets the output to write the modified DOM in.</p>
     * @param output
     */
    public void setOutput(final File output) {
        this.output = output;
    }
}
