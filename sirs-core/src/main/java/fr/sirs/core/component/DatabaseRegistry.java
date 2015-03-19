package fr.sirs.core.component;

import static fr.sirs.core.CouchDBInit.DB_CONNECTOR;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.ReplicationCommand;
import org.ektorp.http.HttpClient;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.sirs.core.SirsCore;
import fr.sirs.core.SirsCoreRuntimeExecption;
import fr.sirs.core.SirsDBInfo;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ektorp.DbAccessException;
import org.ektorp.http.RestTemplate;
import org.geotoolkit.util.FileUtilities;

public class DatabaseRegistry {

    private static final String BASE_LOCAL_HTTP = "http://geouser:geopw@127.0.0.1:5984/";

    // private static final String BASE_LOCAL_HTTP =
    // "https://geouser:geopw@127.0.0.1:6984/";

    private DatabaseRegistry() {
    }

    public static List<String> listSirsDatabase(URL base) throws IOException {

        HttpClient client = new StdHttpClient.Builder().url(base).build();
        CouchDbInstance dbInstance = new StdCouchDbInstance(client);

        List<String> dbList;
        try {
            dbList = dbInstance.getAllDatabases();
        } catch (DbAccessException e) {
            createUser(base.toExternalForm());
            dbList = dbInstance.getAllDatabases();
        }
        List<String> res = new ArrayList<>();
        for (String db : dbList) {
            if (db.startsWith("_"))
                continue;
            CouchDbConnector connector = dbInstance.createConnector(db, false);
            try {
                getInfo(connector).map(info -> res.add(db));
            } catch (Exception e) {
                SirsCore.LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return res;
    }

    private static Optional<SirsDBInfo> getInfo(CouchDbConnector connector) {
        if (connector.contains("$sirs")) {
            return Optional.of(connector.get(SirsDBInfo.class, "$sirs"));
        }
        return Optional.empty();
    }

    public static void newLocalDB(String database) throws MalformedURLException {

        final CouchDbInstance couchsb = buildLocalInstance();
        final CouchDbConnector connector = couchsb.createConnector(database,
                true);

        final ClassPathXmlApplicationContext applicationContextParent = new ClassPathXmlApplicationContext();
        applicationContextParent.refresh();
        applicationContextParent.getBeanFactory().registerSingleton(
                DB_CONNECTOR, connector);

        final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                new String[] { "classpath:/fr/sirs/spring/couchdb-context.xml" },
                applicationContextParent);

        applicationContext.close();

    }

    public static void dropLocalDB(String database)
            throws MalformedURLException {

        cancelReplication(buildDatabaseLocalURL(database));

        final CouchDbInstance couchsb = buildLocalInstance();
        couchsb.deleteDatabase(database);
    }

    private static String buildDatabaseLocalURL(String database) {
        if (database.matches("https?://"))
            return database;
        return BASE_LOCAL_HTTP + database + "/";
    }

    public static String newLocalDBFromRemote(String src, String dest,
            boolean continuous) {
        final CouchDbInstance couchsb = buildLocalInstance();

        ReplicationCommand cmd = new ReplicationCommand.Builder()
                .continuous(continuous).source(src).target(dest)
                .createTarget(true).build();
        ReplicationCommand cmdRev = new ReplicationCommand.Builder()
                .continuous(continuous).source(dest).target(src)
                .createTarget(true).build();

        couchsb.replicate(cmd);
        couchsb.replicate(cmdRev);
        return src;
    }

    private static CouchDbInstance buildLocalInstance() {
        HttpClient httpClient;
        try {
            httpClient = new StdHttpClient.Builder().url(BASE_LOCAL_HTTP)
                    .relaxedSSLSettings(true).build();
        } catch (MalformedURLException e) {
            throw new SirsCoreRuntimeExecption(e);
        }
        return new StdCouchDbInstance(httpClient);
    }

    public static void startReplication(CouchDbConnector connector,
            String remoteDatabaseURL, boolean continuous)
            throws MalformedURLException {
        String buildDatabaseLocalURL = buildDatabaseLocalURL(connector
                .getDatabaseName());
        newLocalDBFromRemote(buildDatabaseLocalURL, remoteDatabaseURL,
                continuous);

    }

    public static void startReplication(CouchDbConnector connector,
            boolean continuous) throws MalformedURLException {
        String buildDatabaseLocalURL = buildDatabaseLocalURL(connector
                .getDatabaseName());

        getInfo(connector)
                .filter(info -> StringUtils.hasText(info.getRemoteDatabase()))
                .map(info -> newLocalDBFromRemote(buildDatabaseLocalURL,
                        info.getRemoteDatabase(), continuous))
                .orElseThrow(
                        () -> new SirsCoreRuntimeExecption(
                                "Could not start replication. "));

    }

    public static void cancelReplication(CouchDbConnector connector) {
        String buildDatabaseLocalURL = buildDatabaseLocalURL(connector
                .getDatabaseName());
        cancelReplication(buildDatabaseLocalURL);
    }

    private static String cancelReplication(String databaseURL) {

        CouchDbInstance buildLocalInstance = buildLocalInstance();
        DatabaseRegistry
                .getReplicationTasksBySourceOrTarget(databaseURL)
                .map(t -> t.get("replication_id"))
                .filter(n -> n != null)
                .map(t -> t.asText())
                .map(id -> new ReplicationCommand.Builder().id(id).cancel(true)
                        .build())
                .forEach(cmd -> buildLocalInstance.replicate(cmd));
        return databaseURL;

    }

    static Stream<JsonNode> getReplicationTasks() {
        CouchDbInstance buildLocalInstance = buildLocalInstance();
        HttpResponse httpResponse = buildLocalInstance.getConnection().get(
                "/_active_tasks");

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            List<JsonNode> tasks = objectMapper.readValue(httpResponse
                    .getContent(), objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, JsonNode.class));

            return tasks.stream().filter(
                    t -> t.get("type") == null ? false : t.get("type").asText()
                            .equals("replication"));

        } catch (IOException e) {
            throw new SirsCoreRuntimeExecption(e);
        }

    }

    static Stream<JsonNode> getReplicationTasksBySourceOrTarget(String dst) {
        return getReplicationTasks()
                .filter(t -> match(t.get("target"), dst)
                        || match(t.get("source"), dst));
    }

    private static boolean match(JsonNode jsonNode, String dst) {
        if (jsonNode == null)
            return false;
        String dst2 = jsonNode.asText();
        int i = dst.indexOf('@');
        int j = dst2.indexOf('@');
        return dst.substring(i).equals(dst2.substring(j));
    }
    
    /**
     * Creates a CouchDb user using login and password given in input URL.
     * 
     * @param urlWithBasicAuth URL to target database, embedding login as : 
     * http://$USER:$PASSWORD@$HOST:$PORT
     * @throws IOException If an error occurs while querying CouchDb
     */
    private static void createUser(final String urlWithBasicAuth) throws IOException {
        Matcher matcher = Pattern.compile("([^\\:/@]+)(?:\\:([^\\:/@]+))?@").matcher(urlWithBasicAuth);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Input URL does not contain valid basic authentication login : "+urlWithBasicAuth);
        }
        final String username = matcher.group(1);
        final String password = matcher.group(2);
        RestTemplate template = new RestTemplate(new StdHttpClient.Builder().url(matcher.replaceFirst("")).build());
        String userContent = FileUtilities.getStringFromStream(DatabaseRegistry.class.getResourceAsStream("/fr/sirs/launcher/user-put.json"));
        
        // try to send user as database admin. If it's a fail, we will try to add him as simple user.
        try {
            template.put("/_config/admins/"+username, "\""+password+"\"");
        } catch (DbAccessException e) {
            SirsCore.LOGGER.log(Level.WARNING, "Cannot create an admin.", e);
            template.put("/_users/org.couchdb.user:" + username,
                    userContent.replaceAll("\\$ID", username)
                    .replaceAll("\\$PASSWORD", password == null ? "" : password));
        }
        
    }
}
