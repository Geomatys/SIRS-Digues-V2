package fr.symadrem.sirs.core.component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import javax.annotation.PostConstruct;

import org.ektorp.CouchDbConnector;
import org.ektorp.Options;
import org.ektorp.changes.ChangesCommand;
import org.ektorp.changes.ChangesFeed;
import org.ektorp.changes.DocumentChange;
import org.ektorp.support.Revisions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.symadrem.sirs.core.SymadremCore;
import fr.symadrem.sirs.core.model.Element;

/**
 * This component forward all document events to its listener.
 * 
 * @author olivier.nouguier@geomatys.com
 *
 */
@Component
public class DocumentChangeEmiter {

	private List<DocumentListener> listeners = new ArrayList<DocumentListener>();

	@Autowired
	private CouchDbConnector connector;

	private ObjectMapper objectMapper = new ObjectMapper();

	@PostConstruct
	public void init() {

		ChangesCommand cmd = new ChangesCommand.Builder().build();

		ChangesFeed feed = connector.changesFeed(cmd);

		new Thread() {
			public void run() {
				while (feed.isAlive()) {
					try {
						handlerChanges(feed);
					} catch (Exception e) {
						log(e);
					}
				}
			};
		}.start();

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

	private <T> Optional<T> getAs(Class<T> clazz, String id,
			Optional<String> rev) {
		try {
			if (rev.isPresent()) {
				Options options = new Options().revision(rev.get());
				return Optional.of(connector.get(clazz, id, options));
			} else
				return Optional.of(connector.get(clazz, id));
		} catch (Exception e1) {
			log(e1);
			return Optional.empty();
		}
	}

	private static Optional<Class<?>> asClass(String clazz) {
		try {
			return Optional.of(Class.forName(clazz));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	private Optional<Element> getElement(String id, Optional<String> rev) {
		Optional<String> str = getAs(String.class, id, rev);

		return str.flatMap(s -> toJsonNode(s)).map(node -> node.get("@class"))
				.map(json -> json.asText())
				.flatMap(DocumentChangeEmiter::asClass)
				.flatMap(clazz -> toElement(str.get(), clazz));

	}

	private Optional<Element> toElement(String str, Class<?> clazz) {
		try {
			return Optional.of((Element) objectMapper.reader(clazz).readTree(
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

	private void handlerChanges(ChangesFeed feed) {
		DocumentChange change;
		try {
			change = feed.next();
		} catch (InterruptedException e) {
			return;
		}

		if (listeners.isEmpty())
			return;

		for (DocumentListener listener : listeners) {

			if (change.isDeleted()) {
				retrieveDeletedElement(change.getId()).map(
						element -> listener.documentDeleted(element));

			} else {
				if (change.getRevision().startsWith("1")) {
					System.out
							.println("DocumentChangeEmiter.handlerChanges() CREATE");
					getElement(change.getId(), Optional.empty()).map(
							element -> listener.documentCreated(element));
				} else {
					System.out
							.println("DocumentChangeEmiter.handlerChanges() UPDATE");

					getElement(change.getId(), Optional.empty()).map(
							element -> listener.documentChanged(element));
				}
			}
		}
		return;
	}

	private void log(Exception e) {
		SymadremCore.LOGGER.log(Level.WARNING, e.getMessage(), e);
	}

}
