package fr.sirs.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.ektorp.CouchDbConnector;
import org.ektorp.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.sirs.core.model.Element;

public class DocHelper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    
    private static final Logger LOGGER = LoggerFactory.getLogger(DocHelper.class);
    
    private CouchDbConnector connector;
    
    public DocHelper(CouchDbConnector db) {
        connector = db;
    }

    private Optional<String> getAsString(String id, Optional<String> rev) {
        InputStream inputStream;
        try {
            if (rev.isPresent()) {
                Options options = new Options().revision(rev.get());
                inputStream = connector.getAsStream(id, options);
            } else
                inputStream = connector.getAsStream(id);
            StringWriter stringWriter = new StringWriter();
            IOUtils.copy(inputStream, stringWriter, "UTF-8");
            return Optional.of(stringWriter.toString());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    private Optional<Element> getElement(String id, Optional<String> rev) {
        final Optional<String> str = getAsString(id, rev);

        return str.flatMap(s -> toJsonNode(s))
                .map(node -> node.get("@class"))
                .map(json -> json.asText())
                .flatMap(DocHelper::asClass)
                .flatMap(clazz -> toElement(str.get(), clazz));
    }
    
    private Optional<Element> toElement(String str, Class<?> clazz) {
        try {
            return Optional.of((Element) objectMapper.reader(clazz).readValue(
                    str));
        } catch (IOException e) {
            return Optional.empty();
        }

    }

    private Optional<JsonNode> toJsonNode(String s) {
        try {
            return Optional.of(objectMapper.readTree(s));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
    
    public static Optional<Class<?>> asClass(String clazz) {
        try {
            return Optional.of(Class.forName(clazz));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<Element> getElement(String id) {
        return getElement(id, Optional.empty());
        
    }
}
