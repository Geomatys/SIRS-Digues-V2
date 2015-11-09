package fr.sirs.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.ektorp.CouchDbConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.sirs.core.model.Element;
import org.ektorp.Options;
import org.ektorp.StreamingViewResult;
import org.ektorp.ViewQuery;

public class DocHelper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(DocHelper.class);

    private final CouchDbConnector connector;

    public DocHelper(CouchDbConnector db) {
        connector = db;
    }

    private Optional<String> getAsString(String id, Optional<String> rev) {
        try (final InputStream inputStream = rev.isPresent() ?
                connector.getAsStream(id, new Options().revision(rev.get())) :
                connector.getAsStream(id)) {
            final StringWriter stringWriter = new StringWriter();
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

    public Optional<Element> toElement(final JsonNode node) {
        final JsonNode classNode = node.get("@class");
        if (classNode != null) {
            final Optional<Class<?>> asClass = asClass(classNode.asText());
            if (asClass.isPresent()) {
                try {
                    final Object readValue = objectMapper.readValue(node.traverse(), asClass.get());
                    if (readValue instanceof Element) {
                        return Optional.of((Element) readValue);
                    }
                } catch (IOException ex) {
                    LOGGER.debug("Cannot cast input document into Element", ex);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<Element> toElement(String str, Class<?> clazz) {
        try {
            return Optional.of((Element) objectMapper.reader(clazz).readValue(str));
        } catch (Exception e) {
            LOGGER.debug("Cannot cast input document as Element", e);
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
            return Optional.of(Class.forName(clazz, true, Thread.currentThread().getContextClassLoader()));
        } catch (ClassNotFoundException e) {
            LOGGER.debug("Cannot get class from name : "+clazz, e);
            return Optional.empty();
        }
    }

    public Optional<Element> getElement(String id) {
        return getElement(id, Optional.empty());
    }

    public StreamingViewResult getAllDocsAsStream() {
        return connector.queryForStreamingView(new ViewQuery().allDocs().includeDocs(true));
    }
}
