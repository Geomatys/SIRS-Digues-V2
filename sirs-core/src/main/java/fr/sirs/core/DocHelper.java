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
package fr.sirs.core;

import java.io.IOException;
import java.util.Optional;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

import fr.sirs.core.model.Element;
import org.apache.sis.util.Static;

public class DocHelper extends Static {

    public static final ObjectMapper SIMPLE_MAPPER = new ObjectMapper();

    public static Optional<Element> toElement(final String docContent) throws IOException, ClassNotFoundException {
        return toElement(SIMPLE_MAPPER.readTree(docContent));
    }

    /**
     * Try to parse given document. If it represents an object inheriting {@link Element},
     * then it is parsed and returned. Otherwise, an empty optional is returned.
     * @param node The node to parse.
     * @return Read object if it's an {@link Element}, an empty optional otherwise.
     * @throws IOException If an error occurs while reading input node.
     * @throws ClassNotFoundException If class described by given node cannot be loaded.
     */
    public static Optional<Element> toElement(final JsonNode node) throws IOException, ClassNotFoundException {
        final JsonNode classNode = node.get("@class");
        if (classNode != null && !(classNode instanceof NullNode)) {
            final Class targetClass = Class.forName(classNode.asText(), true, Thread.currentThread().getContextClassLoader());
            if (Element.class.isAssignableFrom(targetClass)) {
                return Optional.of(SIMPLE_MAPPER.readValue(node.traverse(), (Class<Element>) targetClass));
            }
        }
        return Optional.empty();
    }
}
