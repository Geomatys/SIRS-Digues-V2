package fr.sirs.core.component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.ektorp.CouchDbConnector;
import org.ektorp.Options;
import org.ektorp.changes.ChangesCommand;
import org.ektorp.changes.ChangesFeed;
import org.ektorp.changes.DocumentChange;
import org.ektorp.support.Revisions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Element;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * This component forward all document events to its listener.
 *
 * @author olivier.nouguier@geomatys.com
 */
public class DocumentChangeEmiter {

    /** A pattern to identify new elements. They must have a revision number equal to 1 */
    private static final Pattern FIRST_REVISION = Pattern.compile("^1\\D.*");

    // TODO : Transform to Observable list ?
    private final List<DocumentListener> listeners = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CouchDbConnector connector;

    /**
     * Group all evenements spaced by less than specified bulk time in a single event shot.
     */
    public volatile long bulkTime = 1;
    public volatile TimeUnit bulkUnit = TimeUnit.SECONDS;

    public DocumentChangeEmiter(CouchDbConnector connector) {
        this.connector = connector;
    }

    public Thread start() {

        Thread thread = new Thread() {
            @Override
            public void run() {
                final ChangesCommand cmd = new ChangesCommand.Builder().build();
                final ChangesFeed feed = connector.changesFeed(cmd);
                while (feed.isAlive()) {
                    try {
                        handlerChanges(feed);
                    } catch (Exception e) {
                        log(e);
                    }
                }
            };
        };

        thread.setDaemon(true);
        thread.start();

        return thread;

    }

    protected Optional<Element> retrieveDeletedElement(String docId) {
        return retrieveDeleted(docId)
                .flatMap(doc -> retrieveDeletedObject(doc));
    }

    protected Optional<Element> retrieveDeletedObject(
            DeletedCouchDbDocument deleted) {

        Revisions revisions = deleted.getRevisions();

        String rev = revisions.getStart() - 1 + "-" + revisions.getIds().get(1);

        return getElement(deleted.getId(), Optional.of(rev));

    }


    private static Optional<Class<?>> asClass(String clazz) {
        try {
            return Optional.of(Class.forName(clazz, true, Thread.currentThread().getContextClassLoader()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private Optional<Element> getElement(String id, Optional<String> rev) {
        final Optional<String> str = getAsString(id, rev);

        return str.flatMap(s -> toJsonNode(s))
                .map(node -> node.get("@class"))
                .map(json -> json.asText())
                .flatMap(DocumentChangeEmiter::asClass)
                .flatMap(clazz -> toElement(str.get(), clazz));
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
        } catch (Exception e1) {
            log(e1);
            return Optional.empty();
        }
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

    protected Optional<DeletedCouchDbDocument> retrieveDeleted(String docId) {
        Options options = new Options().includeRevisions().param("open_revs",
                "all");
        InputStream stream = connector.getAsStream(docId, options);
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(stream))) {
            String line;
            while (((line) = bufferedReader.readLine()) != null) {
                if (line.startsWith("{")) {
                    return Optional.of(objectMapper.readValue(line,
                            DeletedCouchDbDocument.class));

                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean addListener(DocumentListener listener) {
        return listeners.add(listener);

    }

    public boolean removeListener(DocumentListener listener) {
        return listeners.remove(listener);

    }

    /**
     * @return a view of all listeners currently active. Never null, but can be empty.
     */
    public List<DocumentListener> getListenersUnmodifiable() {
        return Collections.unmodifiableList(listeners);
    }

    private void handlerChanges(ChangesFeed feed) throws InterruptedException {

        final Thread currentThread = Thread.currentThread();
        final HashMap<Class, List<Element>> addedElements = new HashMap<>();
        final HashMap<Class, List<Element>> changedElements = new HashMap<>();
        final HashMap<Class, List<Element>> removedElements = new HashMap<>();

        while (!currentThread.isInterrupted()) {
            final DocumentChange change = feed.next(bulkTime, bulkUnit);
            if (change == null) break;

            if (change.isDeleted()) {
                retrieveDeletedElement(change.getId()).ifPresent((Element e)-> putElement(e, removedElements));
            } else if (FIRST_REVISION.matcher(change.getRevision()).find()) {
                getElement(change.getId(), Optional.empty()).ifPresent((Element e)-> putElement(e, addedElements));
            } else {
                getElement(change.getId(), Optional.empty()).ifPresent((Element e)-> putElement(e, changedElements));
            }
        }

        for (DocumentListener listener : listeners) {
            if (!addedElements.isEmpty()) {
                listener.documentCreated(addedElements);
            }
            if (!changedElements.isEmpty()) {
                listener.documentChanged(changedElements);
            }
            if (!removedElements.isEmpty()) {
                listener.documentDeleted(removedElements);
            }
        }
    }

    private static void putElement(final Element e, final HashMap<Class, List<Element>> output) {
        List<Element> registry = output.get(e.getClass());
        if (registry == null) {
            registry = new ArrayList<>();
            output.put(e.getClass(), registry);
        }
        registry.add(e);
    }

    private void log(Exception e) {
        SirsCore.LOGGER.log(Level.WARNING, e.getMessage(), e);
    }

}
