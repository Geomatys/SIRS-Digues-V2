package fr.sirs.core.model;

import fr.sirs.core.SirsCore;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.apache.sis.util.ArgumentChecks;

/**
 * Utility class to load/save {@link SQLQuery} into property file.
 *
 * @author Alexis Manin
 * @author Johann Sorel
 */
public class SQLQueries {

    /**
     * Open a file containing SQL queries, and put them in memory.
     *
     * Note : no syntax check is done on loaded requests, so every query even if
     * it's malformed, is loaded.
     *
     * @param queryFile The property file containing wanted requests.
     * @return A list of all queries listed in input file. Never null, but can
     * be empty.
     * @throws IOException If we cannot read into input file.
     */
    public static List<SQLQuery> openQueryFile(final Path queryFile) throws IOException {
        ArgumentChecks.ensureNonNull("Path to read queries from.", queryFile);
        final Properties props = new Properties();
        if (Files.isRegularFile(queryFile)) {
            try (InputStream in = Files.newInputStream(queryFile)) {
                props.load(in);
            }
        }

        final List<SQLQuery> queries = new ArrayList<>();
        for (Entry entry : props.entrySet()) {
            queries.add(new SQLQuery((String) entry.getKey(), (String) entry.getValue()));
        }

        return queries;
    }

    /**
     * Load locally saved queries into memory.
     *
     * @return Previously locally saved queries. Can be empty, never null.
     * @throws IOException If we failed to read in system property file.
     */
    public static List<SQLQuery> getLocalQueries() throws IOException {
        return openQueryFile(SirsCore.LOCAL_QUERIES_PATH);
    }

    /**
     * Save queries in system local property file.
     *
     * Note : System file is overriden, so if it contained queries which are not
     * in input list, they're lost.
     *
     * @param queries The list of queries to save.
     * @throws IOException If an error occurred at writing.
     */
    public static void saveQueriesLocally(List<SQLQuery> queries) throws IOException {
        saveQueriesInFile(queries, SirsCore.LOCAL_QUERIES_PATH);
    }

    /**
     * Save queries in specified property file.
     *
     * Note : The file is overriden, so if it contained queries which are not in
     * input list, they're lost.
     *
     * @param queries The list of queries to save.
     * @throws IOException If an error occurred at writing.
     */
    public static void saveQueriesInFile(List<SQLQuery> queries, final Path outputFile) throws IOException {
        ArgumentChecks.ensureNonNull("Queries to save.", queries);
        ArgumentChecks.ensureNonNull("File to save queries into.", outputFile);

        if (Files.isDirectory(outputFile)) {
            throw new IllegalArgumentException("Cannot save queries into a directory.");
        }
        final Properties props = new Properties();
        for (SQLQuery query : queries) {
            props.put(query.name.get(), query.getValueString());
        }
        try (OutputStream out = Files.newOutputStream(outputFile)) {
            props.store(out, "");
        }
    }

    public static class QueryListCellFactory implements Callback<ListView<SQLQuery>, ListCell<SQLQuery>> {

        @Override
        public ListCell<SQLQuery> call(ListView<SQLQuery> param) {
            return new SQLQueryListCell();
        }
    }

    public static class SQLQueryListCell extends ListCell<SQLQuery> {

        @Override
        protected void updateItem(SQLQuery item, boolean empty) {
            super.updateItem(item, empty);
            this.textProperty().bind(item.name);
        }
    }
}
