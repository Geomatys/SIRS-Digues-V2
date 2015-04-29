package fr.sirs.core.component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.springframework.context.support.ClassPathXmlApplicationContext;


import fr.sirs.core.SirsCore;
import fr.sirs.core.SirsDBInfo;
import fr.sirs.index.ElasticSearchEngine;
import fr.sirs.util.property.SirsPreferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ektorp.DbAccessException;
import org.ektorp.http.RestTemplate;
import org.geotoolkit.util.FileUtilities;

import static fr.sirs.util.property.SirsPreferences.PROPERTIES.*;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.ReplicationStatus;
import org.ektorp.ReplicationTask;
import org.ektorp.http.HttpResponse;
import org.ektorp.impl.StdReplicationTask;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Create a wrapper for connections on a CouchDb service.
 * 
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class DatabaseRegistry {

    private static final Pattern BASIC_AUTH = Pattern.compile("([^\\:/@]+)(?:\\:([^\\:/@]+))?@");
    private static final Pattern LOCAL_URL = Pattern.compile("(?i)^([A-Za-z]+://)?(localhost|127\\.0\\.0\\.1)(:\\d+)?");
    private static final Pattern URL_START = Pattern.compile("(?i)^[A-Za-z]+://([^@]+@)?");
    private static final Pattern AUTH_ERROR_CODE = Pattern.compile("40(1|3)");
    
    private static final int SOCKET_TIMEOUT = 45000;
    private static final int CONNECTION_TIMEOUT = 5000;
    
    /**
     * A boolean indicating if program runs on the same host than target CouchDb
     * service (true). False if we work with a service on a distant host.
     */
    private final boolean isLocal;
    
    /**
     * URL of the CouchDb service our registry is working with.
     */
    public final URL couchDbUrl;
    
    /**
     * Login for connexion on CouchDb service.
     */
    private String username;
    /** Password for cconnexion on CouchDb service. */
    private String userPass;
    
    private CouchDbInstance couchDbInstance;
    
    /**
     * Create a connection on local database, using address and login found in {@link SirsPreferences}.
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
     */
    public List<String> listSirsDatabases() {
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
     * @param initIndex True if we must start an elastic search index (creates an {@link ElasticSearchEngine}) on database.
     * @param initChangeListener True if we must start a {@link DocumentChangeEmiter} on database changes, false otherwise.
     * @return A connector to queried database.
     * @throws java.io.IOException If an error occurs while connecting to couchDb database.
     */
    public ConfigurableApplicationContext connectToSirsDatabase(String dbName, final boolean createIfNotExists, final boolean initIndex, final boolean initChangeListener) throws IOException {
        final String dbContext = getContextPath(dbName);
        try {
            CouchDbConnector connector = couchDbInstance.createConnector(dbContext, createIfNotExists);
            // Initializing application context will load application repositories, which will publish their views on the new database.
            final ClassPathXmlApplicationContext parentContext = new ClassPathXmlApplicationContext();
            parentContext.refresh();
            parentContext.getBeanFactory().registerSingleton(CouchDbConnector.class.getSimpleName(), connector);

            final ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
                    new String[]{SirsCore.SPRING_CONTEXT}, parentContext);

            appContext.getBean(SirsDBInfoRepository.class).get().ifPresent(info -> SirsCore.LOGGER.info(info.toString()));

            if (initIndex) {
                ElasticSearchEngine elasticEngine = new ElasticSearchEngine(
                        couchDbUrl.getHost(), (couchDbUrl.getPort() < 0) ? 5984 : couchDbUrl.getPort(), dbContext, username, userPass);
                appContext.getBeanFactory().registerSingleton(ElasticSearchEngine.class.getSimpleName(), elasticEngine);
            }

            if (initChangeListener) {
                DocumentChangeEmiter changeEmmiter = new DocumentChangeEmiter(connector);
                appContext.getBeanFactory().registerSingleton(DocumentChangeEmiter.class.getSimpleName(), changeEmmiter);
                changeEmmiter.start();
            }

            return appContext;

            // If an exception occurs, we check if its due to an authorization problem. If true, we ask for new login and retry.
        } catch (RuntimeException e) {
            handleAccessException(e);
            return connectToSirsDatabase(dbContext, createIfNotExists, initIndex, initChangeListener);
        }
    }

    /**
     * Delete the queried database of current CouchDb service. 
     * @param dbName Name of the database (must be a name valid on current CouchDb service) to remove.
     * @throws IOException If an error happened while connecting to CouchDb service.
     */
    public void dropDatabase(String dbName) throws IOException {
        try {
            cancelAllSynchronizations(dbName);
            couchDbInstance.deleteDatabase(dbName);
        } catch (RuntimeException e) {
            handleAccessException(e);
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
    
    /**
     * Analyze input exception to determine if it's due to bad login. If true, we 
     * ask for a login to user. If none provided, a DbAccessException is thrown.
     * @throws IOException If an error happens when trying to connect to database
     * with newly provided login.
     * @throws  DbAccessException If user does not give any login.
     * @throws T original exception, if it's not an authentication exception, or user cannot give valid login.
     */
    private <T extends Exception> void handleAccessException(final T origin) throws T, IOException {
            Throwable tmpEx = origin;
            boolean accessException = false;
            while (!accessException && tmpEx != null) {
                if (tmpEx instanceof DbAccessException) {
                    accessException = true;
                } else {
                    tmpEx = tmpEx.getCause();
                }
            }
            if (accessException) {
                if (!AUTH_ERROR_CODE.matcher(tmpEx.getMessage()).find()) {
                    accessException = false;
                }
            }
            
            if (!accessException) {
                throw origin;
            }
            
        final Map.Entry<String, String> newLogin = askForLogin(username, userPass);
        if (newLogin != null) {
            username = newLogin.getKey();
            userPass = newLogin.getValue();

            // Refresh connection.
            connect();
        } else {
            throw origin;
        }
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    //  
    // REPLICATION UTILITIES
    //
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Synchronize two databases content.
     * 
     * @param distant The source database name if it's in current service, complete URL otherwise.
     * @param local Name of target database. It have to be in current service.
     * @param continuous True if we must keep databases synchronized over time. False for one shot synchronization.
     * @throws java.io.IOException If an error occurs while rying to connect to one database.
     * @throws java.io.IllegalArgumentException If databases are incompatible (different SRID, or distant bdd is not a SIRS one).
     */
    public void synchronizeSirsDatabases(String distant, String local, boolean continuous) throws IOException {
        Optional<SirsDBInfo> distantInfo = getInfo(distant);
        if (!distantInfo.isPresent()) {
            throw new IllegalArgumentException("Input distant database is not a SIRS application one.");
        }

        final String srid = distantInfo.get().getEpsgCode();

        String localPath = getContextPath(local);
            
        try {
            CouchDbConnector localConnector = couchDbInstance.createConnector(localPath, true); 
            Optional<SirsDBInfo> info = getInfo(localConnector);
            if (info.isPresent()) {
                SirsDBInfo localInfo = info.get();
                
                // TODO : check that SRID of local and distant database are one and the same.
                if (localInfo.getEpsgCode() != null && !localInfo.getEpsgCode().equals(srid)) {
                    throw new IllegalArgumentException("Impossible de synchroniser les bases de données car elles n'utilisent pas le même système de projection :\n"
                            + "Base distante : " + srid + "\n"
                            + "Base locale : " + localInfo.getEpsgCode());
                }
                
                if (localInfo.getRemoteDatabase() != null && !localInfo.getRemoteDatabase().equals(distant)) {
                    Optional<ButtonType> showAndWait = new Alert(
                            Alert.AlertType.WARNING, 
                            "Vous êtes sur le point de changer le point de synchronisation de la base de données.", 
                            ButtonType.CANCEL, ButtonType.OK).showAndWait();
                    if (showAndWait.isPresent() && showAndWait.get().equals(ButtonType.OK)) {
                        cancelAllSynchronizations(localPath);
                    } else {
                        return;
                    }
                }
            }
            
            // Copy source data in our database.
            ReplicationCommand cmd = new ReplicationCommand.Builder()
                    .continuous(continuous).source(distant).target(localPath)
                    .createTarget(true).build();

            // Send back local data to source database.
            ReplicationCommand cmdRev = new ReplicationCommand.Builder()
                    .continuous(continuous).source(localPath).target(distant)
                    .createTarget(true).build();

            couchDbInstance.replicate(cmd);
            couchDbInstance.replicate(cmdRev);
            
            new SirsDBInfoRepository(localConnector).set(srid, distant);
            
        } catch (RuntimeException e) {
            handleAccessException(e);
            synchronizeSirsDatabases(distant, localPath, continuous);
        }
    }
    
    /**
     * Retrieve list of Synchronization tasks the input database is part of. Even 
     * paused tasks are returned.
     * 
     * @param dbName Target database name or path for synchronizations.
     * @return List of source database names or path for continuous copies on input database.
     */
    public Stream<ReplicationTask> getSynchronizationTasks(final String dbName) throws IOException {
        ArgumentChecks.ensureNonEmpty("Input database name", dbName);
        return getReplicationTasksBySourceOrTarget(dbName)
                .filter((ReplicationTask task)-> task.isContinuous());
    }
    
    /**
     * Copy source database content to destination database. If destination database 
     * does not exists, it will be created. No synchronization over time here.
     * 
     * @param dbToCopy Database to copy. Only its name if it's in current service, complete URL otherwise.
     * @param dbToPasteInto Database to paste content into. Only its name if it's in current service, complete URL otherwise.
     * @return A status of started replication task.
     */
    public ReplicationStatus copyDatabase(final String dbToCopy, final String dbToPasteInto) throws IOException {
        ReplicationCommand cmd = new ReplicationCommand.Builder()
                .continuous(false).source(dbToCopy).target(dbToPasteInto)
                .createTarget(true).build();
        try {
            return couchDbInstance.replicate(cmd);
        } catch (DbAccessException e) {
            handleAccessException(e);
            return copyDatabase(dbToCopy, dbToPasteInto);
        }
    }
    
    /**
     * Cancel the database copy pointed by given replication status.
     * @param operationStatus Status of the copy to stop.
     * @return The status of cancelled task.
     */
    public ReplicationStatus cancelCopy(final ReplicationStatus operationStatus) {
        return couchDbInstance.replicate(new ReplicationCommand.Builder()
                .id(operationStatus.getId()).cancel(true).build());
    }
        
    /**
     * Cancel all replication tasks implying input database.
     * @param dbName Name of the database to deconnect.
     * @return Number of cancelled replication tasks.
     * @throws IOException If a connection error happens while retrieving replication tasks. 
     */
    public int cancelAllSynchronizations(final String dbName) throws IOException {
        final AtomicInteger counter = new AtomicInteger(0);
        ReplicationCommand.Builder cancel = new ReplicationCommand.Builder().cancel(true);
        getReplicationTasksBySourceOrTarget(dbName).forEach(task -> {
            couchDbInstance.replicate(cancel.id(task.getReplicationId()).build());
            counter.incrementAndGet();
        });
        return counter.get();
    }

    ArrayList<ReplicationTask> getReplicationTasks() throws IOException {
        HttpResponse httpResponse = couchDbInstance.getConnection().get("/_active_tasks");
        
        final ArrayList<ReplicationTask> result = new ArrayList();
        
            final ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JsonNode tasks = mapper.readTree(httpResponse.getContent());
            if (tasks.isArray()) {
                Iterator<JsonNode> elements = tasks.elements();
                while (elements.hasNext()) {
                    final JsonNode next = elements.next();
                    if (next.has("type") && "replication".equals(next.get("type").asText())) {
                        result.add(mapper.treeToValue(next, StdReplicationTask.class));
                    }
                }
            }
            
        return result;
    }

    Stream<ReplicationTask> getReplicationTasksBySourceOrTarget(String dst) throws IOException {
        return getReplicationTasks().stream().filter(
                t -> (t.getSourceDatabaseName().equals(dst) || t.getTargetDatabaseName().equals(dst)));
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    //  
    // MINOR UTILITY METHODS
    //
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Build an {@link  URL} object from given string. Add protocol http prefix 
     * if no prefix is defined in input string.
     * @param baseURL The string to make URL from.
     * @return An URL representing input path.
     * @throws MalformedURLException If input string does not represent a path.
     */
    public static URL toURL(final String baseURL) throws MalformedURLException {
        if (URL_START.matcher(baseURL).find()) {
            return new URL(baseURL);
        } else {
            return new URL("http://"+baseURL);
        }
    }
    
    private static CouchDbConnector connectToDistant(final String distantService) throws IOException {
        // Connect to distant database.
        final DatabaseRegistry regis = new DatabaseRegistry(distantService);
        // Connection succeed, which means we must extract database name from it.
        String dbPath = URL_START.matcher(distantService).replaceFirst("");
        final String[] splittedPath = dbPath.split("/");
        if (splittedPath.length < 1) throw new IllegalArgumentException("Input path does not contain database name.");
        int splitIndex = splittedPath.length;
        final StringBuilder pathBuilder = new StringBuilder(splittedPath[--splitIndex]);
        while (splitIndex > 0) {
            try {
                return regis.couchDbInstance.createConnector(pathBuilder.toString(), false);
            } catch (Exception e) {
                pathBuilder.insert(0, '/').insert(0, splittedPath[--splitIndex]);
            }
        }
        throw new IllegalArgumentException("Input path does not contain database name.");
    }
    
    public Optional<SirsDBInfo> getInfo(final String dbName) {
        final CouchDbConnector connector;
        
        final Optional<String> localPath = getLocalPath(dbName);
        if (localPath.isPresent()) {
            connector = couchDbInstance.createConnector(dbName, false);
        } else {
            try {
                connector = connectToDistant(dbName);
            } catch (IOException ex) {
                SirsCore.LOGGER.log(Level.WARNING, null, ex);
                return Optional.empty();
            }
        }
        return getInfo(connector);
    }
    
    /**
     * Test if the given path or name represents a database instance hosted by current 
     * service. If its true, we return a path whose all information before context has been truncated.
     * @param dbName Name or URL of the database to test.
     * @return true if given database has been found in current service, false otherwise.
     */
    private Optional<String> getLocalPath(final String dbName) {
        String toTest = getContextPath(dbName);
        try {
            if (couchDbInstance.checkIfDbExists(toTest)) {
                return Optional.of(toTest);
            }
        } catch (Exception e) {
            SirsCore.LOGGER.log(Level.FINE, "An error occurred while testing existance of a database : " + dbName, e);
        }
        return Optional.empty();
    }
    
    /**
     * If the given string represents an URL pointing on current service, truncate all information before context path.
     * @param dbPath The path to truncate.
     * @return The context path for input string, or untouched input if it does 
     * not represent a local URL.
     */
    private String getContextPath(final String dbPath) {
        Matcher urlMatcher;
        if (isLocal && (urlMatcher = LOCAL_URL.matcher(dbPath)).find()) {
            return urlMatcher.replaceFirst("");
        } else {
            final String tmpServiceURL = URL_START.matcher(couchDbUrl.toExternalForm()).replaceFirst("");
            final String tmpParam = URL_START.matcher(dbPath).replaceFirst("");
            if (tmpParam.startsWith(tmpServiceURL)) {
                return tmpParam.replace(tmpServiceURL, "");
            } else {
                return dbPath;
            }
        }
    }
    
    public static Optional<SirsDBInfo> getInfo(CouchDbConnector connector) {
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
                question.setHeaderText("Veuillez rentrer des identifiants de connexion à CouchDb pour l'adresse suivante :\n"
                        + (isLocal? "Service local" : couchDbUrl.toExternalForm()));

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
