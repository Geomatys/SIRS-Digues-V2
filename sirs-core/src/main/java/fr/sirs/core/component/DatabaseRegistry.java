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
import fr.sirs.util.property.SirsPreferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ektorp.DbAccessException;
import org.ektorp.http.RestTemplate;
import org.geotoolkit.util.FileUtilities;

import static fr.sirs.util.property.SirsPreferences.PROPERTIES.*;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Create a wrapper for connections on a couchDb service.
 * 
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class DatabaseRegistry {

    private static final Pattern BASIC_AUTH = Pattern.compile("([^\\:/@]+)(?:\\:([^\\:/@]+))?@");
    
    private static final int SOCKET_TIMEOUT = 45000;
    private static final int CONNECTION_TIMEOUT = 5000;
    
    private final boolean isLocal;
    
    public final URL couchDbUrl;
    
    private String username;
    private String userPass;
    
    private CouchDbInstance couchDbInstance;
    
    /**
     * Create a connexion on local database, using address and login found in {@link SirsPreferences}.
     * 
     * @throws IOException If URL found in configuration is not valid, or a connection problem occurs.
     */
    public DatabaseRegistry() throws IOException {
        this(null);
    }
    
    /**
     * Try to connect to CouchDb service pointed by input URL.
     * @param couchDbURL The URL to CouchDB server.
     * @throws IOException If URL found in configuration is not valid, or a connection problem occurs.
     */
    public DatabaseRegistry(final String couchDbURL) throws IOException {
        this(couchDbURL, null, null);
    }

    /**
     * Try to connect to CouchDb service pointed by input URL with given login.
     * @param urlParam The URL to CouchDB server.
     * @param userParam User name to log in with.
     * @param passParam Password for given user.
     * @throws IOException If URL found in configuration is not valid, or a connection problem occurs.
     */
    public DatabaseRegistry(final String urlParam, final String userParam, final String passParam) throws IOException {
        if (urlParam == null || urlParam.equals(SirsPreferences.INSTANCE.getPropertySafe(COUCHDB_LOCAL_ADDR))) {
            this.couchDbUrl = toURL(SirsPreferences.INSTANCE.getProperty(COUCHDB_LOCAL_ADDR));
            isLocal = true;
        } else {
            this.couchDbUrl = toURL(urlParam);
            isLocal = false;
        }
        username = userParam;
        userPass = passParam;

        // No login, we try to extract it from URL.
        if (username == null || username.isEmpty()) {
            final String userInfo = this.couchDbUrl.getUserInfo();
            if (userInfo != null) {
                String[] split = userInfo.split(":");
                if (split.length > 0) {
                    username = split[0]; 
                }
                if (split.length > 1) {
                    userPass = split[1];
                }
            }
            // In case authority is not embedded in url. If we're on local connection, login should be written in preferences.
            if (username == null || username.isEmpty() && isLocal) {
                username = SirsPreferences.INSTANCE.getPropertySafe(DEFAULT_LOCAL_USER);
                userPass = SirsPreferences.INSTANCE.getPropertySafe(DEFAULT_LOCAL_PASS);
            }
        }
        
        connect();
    }

    /**
     * @return User name used to log in current CouchDb service. Can be null or empty.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return Password of the user we log in with. Can be null or empty.
     */
    public String getUserPass() {
        return userPass;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //  
    // CONNECTION MANAGEMENT
    //
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Create a new connection for input parameters. If we cannot connect because
     * provided user does not exists in given database, we will try to create it.
     * If we cannot, we'll ask user for authentication login.
     * 
     * @param couchDbUrl The database to connect to.
     * @param user The user login for CouchDb connection.
     * @param password The user password.
     * @return A connection instance, with sufficient rights for read operations.
     * @throws IOException If input url is not valid, or a connection failure happens.
     * @throws DbAccessException If login information is invalid.
     * @throws IllegalArgumentExeption If login information is null.
     */
    private void connect() throws IOException {
        StdHttpClient.Builder builder = new StdHttpClient.Builder()
                .url(couchDbUrl)
                .connectionTimeout(CONNECTION_TIMEOUT)
                .socketTimeout(SOCKET_TIMEOUT)
                .relaxedSSLSettings(true);
        final boolean userGiven = (username != null && !username.isEmpty());
        if (userGiven) {
            builder.username(username);
            if (userPass != null && !userPass.isEmpty()) {
                builder.password(userPass);
            }
        }
        couchDbInstance = new StdCouchDbInstance(builder.build());
        try {
            couchDbInstance.getAllDatabases();
        } catch (DbAccessException e) {
            try {
                if (userGiven) {
                    createUser(couchDbUrl, username, userPass);
                }
                couchDbInstance.getAllDatabases();
            } catch (DbAccessException | IllegalArgumentException ex) {
                final Map.Entry<String, String> newLogin = askForLogin(username, userPass);
                if (newLogin != null) {
                    username = newLogin.getKey();
                    userPass = newLogin.getValue();
                    
                    // Refresh connection.
                    connect();
                    if (isLocal) {
                        final HashMap<SirsPreferences.PROPERTIES, String> login = new HashMap<>();
                        login.put(DEFAULT_LOCAL_USER, newLogin.getKey());
                        login.put(DEFAULT_LOCAL_PASS, newLogin.getValue());
                        SirsPreferences.INSTANCE.store(login);
                    }
                } else {
                    throw ex;
                }
            }
        }
    }
    
    /**
     * List SIRS application databases found on current CouchDb server.
     * @return The name of all databases which contain SIRS data.
     * @throws IOException If an error happens while connecting to database.
     */
    public List<String> listSirsDatabases() throws IOException {
        final List<String> dbList = couchDbInstance.getAllDatabases();
        
        List<String> res = new ArrayList<>();
        for (String db : dbList) {
            if (db.startsWith("_"))
                continue;
            // If there's a "sirsInfo" document, it's an app db.
            CouchDbConnector connector = couchDbInstance.createConnector(db, false);
            try {
                getInfo(connector).map(info -> res.add(db));
            } catch (Exception e) {
                SirsCore.LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return res;
    }

    /**
     * Connect to database with given name on current CouchDb service.
     * @param dbName Name of the database to create.
     * @param createIfNotExists True to create the database if it does not exists. false otherwise.
     * @return A connector to queried database.
     */
    public CouchDbConnector connectToDatabase(String dbName, final boolean createIfNotExists) {
        try {
            return couchDbInstance.createConnector(dbName, createIfNotExists);
        } catch (DbAccessException e) {
            try {
                handleAccessException();
            } catch (IOException ex) {
                e.addSuppressed(ex);
                throw e;
            }
            return connectToDatabase(dbName, createIfNotExists);
        }
    }

    public void dropDatabase(String dbName) throws IOException {
        try {
        cancelReplication(buildDatabaseURL(dbName));
        couchDbInstance.deleteDatabase(dbName);
        } catch (DbAccessException e) {
            handleAccessException();
            dropDatabase(dbName);
        }
    }
    
    /**
     * Creates a CouchDb user using login and password given in input URL.
     * 
     * @param urlWithBasicAuth URL to target database, embedding login as : 
     * http://$USER:$PASSWORD@$HOST:$PORT
     * @throws IOException If an error occurs while querying CouchDb
     */
    private static void createUser(final String urlWithBasicAuth) throws IOException {
        Matcher matcher = BASIC_AUTH.matcher(urlWithBasicAuth);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Input URL does not contain valid basic authentication login : "+urlWithBasicAuth);
        }
        final String username = matcher.group(1);
        final String password = matcher.group(2);
        createUser(new URL(matcher.replaceFirst("")), username, password);
    }
    
    /**
     * Try to create an user in the given database
     * @param couchDbUrl URL to the CouchDB database on which to create user.
     * @param username The login of the user to create.
     * @param password The password of the user to create.
     * @throws IOException If we have a problem while building PUT documents.
     */
    private static void createUser(final URL couchDbUrl, final String username, final String password) throws IOException {
        RestTemplate template = new RestTemplate(new StdHttpClient.Builder().url(couchDbUrl).build());
        String userContent = FileUtilities.getStringFromStream(DatabaseRegistry.class.getResourceAsStream("/fr/sirs/launcher/user-put.json"));
        
        // try to send user as database admin. If it's a fail, we will try to add him as simple user.
        try {
            template.put("/_config/admins/"+username, password == null? "" : "\""+password+"\"");
        } catch (DbAccessException e) {
            SirsCore.LOGGER.log(Level.WARNING, "Cannot create an admin.", e);
            template.put("/_users/org.couchdb.user:" + username,
                    userContent.replaceAll("\\$ID", username)
                    .replaceAll("\\$PASSWORD", password == null ? "" : password));
        }
    }    
    
    private void handleAccessException() throws IOException {
        final Map.Entry<String, String> newLogin = askForLogin(username, userPass);
        if (newLogin != null) {
            username = newLogin.getKey();
            userPass = newLogin.getValue();

            // Refresh connection.
            connect();
        } else {
            throw new DbAccessException("Pas de login fourni.");
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //  
    // REPLICATION UTILITIES
    //
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Synchronize two databases content.
     * @param src The name of the first database if it's in current service, complete URL otherwise.
     * @param dest Name of the second database if it's in current service, complete URL otherwise.
     * @param continuous A flag to indicate if databases should be kept synchronized over time (true) or just once.
     */
    public void synchronizeDatabases(String src, String dest, boolean continuous) {
        try {
            // Copy source data in our database.
            ReplicationCommand cmd = new ReplicationCommand.Builder()
                    .continuous(continuous).source(src).target(dest)
                    .createTarget(true).build();

            // Send back local data to source database.
            ReplicationCommand cmdRev = new ReplicationCommand.Builder()
                    .continuous(continuous).source(dest).target(src)
                    .createTarget(true).build();

            couchDbInstance.replicate(cmd);
            couchDbInstance.replicate(cmdRev);
        } catch (DbAccessException e) {
            try {
                handleAccessException();
                synchronizeDatabases(src, dest, continuous);
            } catch (IOException ex) {
                e.addSuppressed(ex);
                throw e;
            }
        }
    }
    
    public void startReplication(CouchDbConnector connector,
            String remoteDatabaseURL, boolean continuous)
            throws MalformedURLException {
        String buildDatabaseLocalURL = buildDatabaseURL(connector
                .getDatabaseName());
        synchronizeDatabases(buildDatabaseLocalURL, remoteDatabaseURL,
                continuous);
    }

    public void startReplication(final CouchDbConnector connector, final boolean continuous) throws MalformedURLException {
        String buildDatabaseLocalURL = buildDatabaseURL(connector
                .getDatabaseName());

        Optional<SirsDBInfo> filter = getInfo(connector).filter(info -> StringUtils.hasText(info.getRemoteDatabase()));
        if (filter.isPresent()) {
            synchronizeDatabases(buildDatabaseLocalURL, filter.get().getRemoteDatabase(), continuous);
        } else {
            throw new SirsCoreRuntimeExecption("Cannot start replication.");
        }
    }

    public void cancelReplication(CouchDbConnector connector) {
        cancelReplication(buildDatabaseURL(connector.getDatabaseName()));
    }

    private String cancelReplication(String databaseURL) {
        getReplicationTasksBySourceOrTarget(databaseURL)
                .map(t -> t.get("replication_id"))
                .filter(n -> n != null)
                .map(t -> t.asText())
                .map(id -> new ReplicationCommand.Builder().id(id).cancel(true)
                        .build())
                .forEach(cmd -> couchDbInstance.replicate(cmd));
        return databaseURL;
    }

    Stream<JsonNode> getReplicationTasks() {
        HttpResponse httpResponse = couchDbInstance.getConnection().get(
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

    Stream<JsonNode> getReplicationTasksBySourceOrTarget(String dst) {
        return getReplicationTasks().filter(
                t -> match(t.get("target"), dst) || match(t.get("source"), dst));
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //  
    // MINOR UTILITY METHODS
    //
    ////////////////////////////////////////////////////////////////////////////
    
    private static boolean match(JsonNode jsonNode, String dst) {
        if (jsonNode == null)
            return false;
        String dst2 = jsonNode.asText();
        int i = dst.indexOf('@');
        int j = dst2.indexOf('@');
        return dst.substring(i).equals(dst2.substring(j));
    }
    
    /**
     * Build an {@link  URL} object from given string. Add protocol http prefix 
     * if no prefix is defined in input string.
     * @param baseURL The string to make URL from.
     * @return An URL representing input path.
     * @throws MalformedURLException If input string does not represent a path.
     */
    public static URL toURL(final String baseURL) throws MalformedURLException {
        if (baseURL.matches("[A-Za-z]+://.*")) {
            return new URL(baseURL);
        } else {
            return new URL("http://"+baseURL);
        }
    }
        
    private String buildDatabaseURL(String database) {
        if (database.matches("https?://.+")) {
            return database;
        }
        String localAdress = couchDbUrl.toExternalForm();
        if (!localAdress.endsWith("/")) {
            localAdress = localAdress + "/";
        }
        return localAdress + database + "/";
    }
    
    private static Optional<SirsDBInfo> getInfo(CouchDbConnector connector) {
        if (connector.contains("$sirs")) {
            return Optional.of(connector.get(SirsDBInfo.class, "$sirs"));
        }
        return Optional.empty();
    }

    /**
     * Display a dialog to ask user a login and password to allow connection to CouchDB service.
     * @return An entry whose key is login and value is password typed by user. Null if user cancelled dialog.
     */
    private Map.Entry<String, String> askForLogin(final String defaultUser, final String defaultPass) {
        final Task<Map.Entry<String, String>> askLogin = new Task() {

            @Override
            protected Object call() throws Exception {
                final TextField userInput = new TextField(defaultUser);
                final PasswordField passInput = new PasswordField();
                passInput.setText(defaultPass);

                final GridPane gPane = new GridPane();
//                gPane.setMaxWidth(Double.MAX_VALUE);
//                gPane.setPrefWidth(400);
//                gPane.getColumnConstraints().addAll(
//                        new ColumnConstraints(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE, Region.USE_PREF_SIZE, Priority.NEVER, HPos.LEFT, true),
//                        new ColumnConstraints(0, Region.USE_PREF_SIZE, Double.MAX_VALUE, Priority.ALWAYS, HPos.LEFT, true)
//                );
                gPane.add(new Label("Login : "), 0, 0);
                gPane.add(userInput, 1, 0);
                gPane.add(new Label("Mot de passe : "), 0, 1);
                gPane.add(passInput, 1, 1);

                final Alert question = new Alert(Alert.AlertType.NONE, "Veuillez rentrer des identifiants de connexion à CouchDb : ", ButtonType.CANCEL, ButtonType.OK);
                question.getDialogPane().setContent(gPane);
                question.setResizable(true);
                question.setHeaderText("Veuillez rentrer des identifiants de connexion à CouchDb : ");

                Optional<ButtonType> result = question.showAndWait();
                if (result.isPresent() && result.get().equals(ButtonType.OK)) {
                    return new AbstractMap.SimpleEntry<>(userInput.getText(), passInput.getText());
                } else {
                    return null;
                }
            }
        };

        if (Platform.isFxApplicationThread()) {
            askLogin.run();
        } else {
            Platform.runLater(askLogin);
        }

        try {
            return askLogin.get();
        } catch (InterruptedException | ExecutionException ex) {
            return null;
        }
    }
}
