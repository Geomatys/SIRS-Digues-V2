package fr.sirs.core;

import java.io.IOException;
import java.util.Optional;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

import fr.sirs.core.model.Element;
import org.apache.sis.util.Static;

public class DocHelper extends Static {

    public static Optional<Element> toElement(final JsonNode node) {
        final JsonNode classNode = node.get("@class");
        if (classNode != null && !(classNode instanceof NullNode)) {
            try {
                final Object readValue = new ObjectMapper().readValue(
                        node.traverse(),
                        Class.forName(classNode.asText(), true, Thread.currentThread().getContextClassLoader()));
                if (readValue instanceof Element) {
                    return Optional.of((Element) readValue);
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new SirsCoreRuntimeException("Cannot read a json element.", e);
            }
        }
        return Optional.empty();
    }
}
