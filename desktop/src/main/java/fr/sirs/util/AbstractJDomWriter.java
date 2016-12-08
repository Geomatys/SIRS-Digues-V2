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

    public static final String NULL_REPLACEMENT = " - ";
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
