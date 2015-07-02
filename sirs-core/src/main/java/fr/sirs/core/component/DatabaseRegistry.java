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
import static fr.sirs.core.SirsCore.INFO_DOCUMENT_ID;
import fr.sirs.core.SirsDBInfo;
import fr.sirs.core.authentication.AuthenticationWallet;
import fr.sirs.index.ElasticSearchEngine;
import fr.sirs.util.property.SirsPreferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ektorp.DbAccessException;
import org.ektorp.http.RestTemplate;
import org.geotoolkit.util.FileUtilities;

import static fr.sirs.util.property.SirsPreferences.PROPERTIES.*;
import java.io.InputStream;
import java.net.ProxySelector;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.SystemDefaultCredentialsProvider;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.ReplicationStatus;
import org.ektorp.ReplicationTask;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.PreemptiveAuthRequestInterceptor;
import org.ektorp.impl.StdReplicationTask;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Create a wrapper for connections on a CouchDb service.
 * 
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class DatabaseRegistry {

    /** Administrator list section */
    private static final String ADMIN_SECTION = "admins";
    
    // Httpd related configurations
    private static final String HTTPD_SECTION = "httpd";
    private static final String SOCKET_OPTION = "socket_options";
    private static final String NO_DELAY_KEY = "nodelay";
    private static final String BIND_ADDRESS_OPTION = "bind_address";
    private static final String ENABLE_CORS_OPTION = "enable_cors";
    
    // Cors configuration
    private static final String CORS_SECTION = "cors";
    private static final String CREDENTIALS_OPTIONS = "credentials";
    private static final String METHODS_OPTIONS = "methods";
    private static final String ORIGINS_OPTIONS = "origins";
    
    // Authentication options
    private static final String AUTH_SECTION = "couch_httpd_auth";
    private static final String REQUIRE_USER_OPTION = "require_valid_user";
    
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
     * Login for connection on CouchDb service.
     */
    private String username;
    /** Password for connection on CouchDb service. */
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
            
            AuthenticationWallet.Entry entry = AuthenticationWallet.getDefault().get(couchDbUrl);
            if (entry != null && entry.login != null) {
                username = entry.login;
                userPass = entry.password;
            }
        }
        
        // Create default user if it does not exists.
        if (username != null && isLocal) {
            try {
                createUserIfNotExists(couchDbUrl, username, userPass);
            } catch (Exception e) {
                // normal behavior if authentication is required.
                SirsCore.LOGGER.log(Level.FINE, "User check / creation failed.", e);
            }
        }
        
        connect();
        
        // last thing to do : we configure CouchDB wire related parameters.
        if (isLocal) {
            try {
                setAuthenticationRequired();
                setOpenWorld();
                setCorsUseless();
                ensureNoDelay();
            } catch (DbAccessException e) {
                SirsCore.LOGGER.log(Level.WARNING, "CouchDB configuration cannot be overriden !", e);
            }
        }
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
        final AuthenticationWallet wallet = AuthenticationWallet.getDefault();
        /*
         * First, we will open a connection with a java.net url to initialize authentication.
         */
        try (final InputStream stream = couchDbUrl.openStream()) {
            if (wallet != null) {
                AuthenticationWallet.Entry authEntry = wallet.get(couchDbUrl);
                if (authEntry != null) {
                    username = authEntry.login;
                    userPass = authEntry.password;
                }
            }
        }
        
        // Configure http client
        final StdHttpClient.Builder builder = new SirsClientBuilder()
                .url(couchDbUrl)
                .connectionTimeout(CONNECTION_TIMEOUT)
                .socketTimeout(SOCKET_TIMEOUT)
                .relaxedSSLSettings(true);
        
        couchDbInstance = new StdCouchDbInstance(builder.build());
        couchDbInstance.getAllDatabases();
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
    public ConfigurableApplicationContext connectToSirsDatabase(String dbName, 
            final boolean createIfNotExists, final boolean initIndex,
            final boolean initChangeListener) throws IOException {
        final String dbContext = getContextPath(dbName);
        CouchDbConnector connector = couchDbInstance.createConnector(dbContext, createIfNotExists);
        // Initializing application context will load application repositories, which will publish their views on the new database.
        final ClassPathXmlApplicationContext parentContext = new ClassPathXmlApplicationContext();
        parentContext.refresh();
        parentContext.getBeanFactory().registerSingleton(CouchDbConnector.class.getSimpleName(), connector);

        final ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
                new String[]{SirsCore.SPRING_CONTEXT}, parentContext);

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
    }

    /**
     * Delete the queried database of current CouchDb service. 
     * @param dbName Name of the database (must be a name valid on current CouchDb service) to remove.
     * @throws IOException If an error happened while connecting to CouchDb service.
     */
    public void dropDatabase(String dbName) throws IOException {
        cancelAllSynchronizations(dbName);
        couchDbInstance.deleteDatabase(dbName);
    }
        
    /**
     * Try to create an user in the given database if he does not already exists.
     * 
     * Note: We use a direct HTTP call, without any credential. We cannot use provided
     * login information, because if it describes a inexisting user, couchdb will 
     * fail on authentication requests.
     * 
     * Note 2 : Although we test user existence, its password is not checked (because hash). 
     * 
     * @param couchDbUrl URL to the CouchDB database on which to create user.
     * @param username The login of the user to check/create.
     * @param password The password to use if we have to create user.
     * @throws IOException If we have a problem while building PUT documents.
     */
    private static void createUserIfNotExists(final URL couchDbUrl, final String username, final String password) throws IOException {
        RestTemplate template = new RestTemplate(new StdHttpClient.Builder().url(couchDbUrl).build());
        String userContent = FileUtilities.getStringFromStream(DatabaseRegistry.class.getResourceAsStream("/fr/sirs/launcher/user-put.json"));

        final String adminConfig = "/_config/admins/" + username;
        try {
            template.getUncached(adminConfig);
        } catch (Exception e) {
            SirsCore.LOGGER.log(Level.FINE, "No administrator can be found for login " + username, e);
            final String userConfig = "/_users/org.couchdb.user:" + username;
            try {
                template.getUncached(userConfig);
            } catch (Exception e2) {
                SirsCore.LOGGER.log(Level.FINE, "No user can be found for login " + username, e);
                SirsCore.LOGGER.fine("Attempt to create administrator " + username);

                // try to send user as database admin. If it's a fail, we will try to add him as simple user.
                try {
                    template.put(adminConfig, password == null ? "" : "\"" + password + "\"");
                } catch (DbAccessException e3) {
                    SirsCore.LOGGER.log(Level.FINE, "Cannot create administrator " + username, e);
                    SirsCore.LOGGER.fine("Attempt to create simple user " + username);
                    template.put(userConfig,
                            userContent.replaceAll("\\$ID", username)
                            .replaceAll("\\$PASSWORD", password == null ? "" : password));
                }
            }
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
                final Alert alert = new Alert(
                        Alert.AlertType.WARNING,
                        "Vous êtes sur le point de changer le point de synchronisation de la base de données.",
                        ButtonType.CANCEL, ButtonType.OK);
                final Optional<ButtonType> showAndWait = alert.showAndWait();

                if (showAndWait.isPresent() && showAndWait.get().equals(ButtonType.OK)) {
                    cancelAllSynchronizations(localPath);
                } else {
                    return;
                }
            }
        }

        // Force authentication on distant database. We can rely on wallet information
        // because a connection should have been opened already to retrieve SIRS information.
        URL distantURL = toURL(distant);
        if (distantURL.getUserInfo() == null) {
            AuthenticationWallet.Entry entry = AuthenticationWallet.getDefault().get(distantURL);
            if (entry != null && entry.login != null && !entry.login.isEmpty()) {
                final String userInfo;
                if (entry.password == null || entry.password.isEmpty()) {
                    userInfo = entry.login + "@";
                } else {
                    userInfo = entry.login + ":" + entry.password + "@";
                }
                distant = distantURL.toExternalForm().replaceFirst("(^\\w+://)", "$1" + userInfo);
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
    }
    
    /**
     * Retrieve list of Synchronization tasks the input database is part of. Even 
     * paused tasks are returned.
     * 
     * @param dbName Target database name or path for synchronizations.
     * @return List of source database names or path for continuous copies on input database.
     * @throws java.io.IOException If we cannot get list of active synchronisation from CouchDB.
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
    public ReplicationStatus copyDatabase(final String dbToCopy, final String dbToPasteInto) {
        ReplicationCommand cmd = new ReplicationCommand.Builder()
                .continuous(false).source(dbToCopy).target(dbToPasteInto)
                .createTarget(true).build();
        
        return couchDbInstance.replicate(cmd);
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
        if (connector.contains(INFO_DOCUMENT_ID)) {
            return Optional.of(connector.get(SirsDBInfo.class, INFO_DOCUMENT_ID));
        }
        return Optional.empty();
    }

    private void ensureNoDelay() {
        try {
            String socketConfig;
            // Catch exception at read because if property does not exists, an error is thrown.
            try {
                socketConfig = couchDbInstance.getConfiguration(HTTPD_SECTION, SOCKET_OPTION);
            } catch (DbAccessException e) {
                socketConfig = null;
            }
            
            if (socketConfig == null || socketConfig.trim().matches("\\[\\s*\\]")) {
                socketConfig = "[{" + NO_DELAY_KEY + ", true}]";
            } else {
                final Matcher matcher = Pattern.compile(NO_DELAY_KEY + "\\s*,\\s*(true|false)").matcher(socketConfig);
                if (matcher.find()) {
                    if (matcher.group(1).equalsIgnoreCase("true")) {
                        SirsCore.LOGGER.info("SOCKET configuration found with right value.");
                        return;
                    } else {
                        socketConfig = matcher.replaceAll(NO_DELAY_KEY + ", true");
                    }
                } else {
                    socketConfig = socketConfig.replaceFirst("\\]$", ", {" + NO_DELAY_KEY + ", true}]");
                }
            }

            SirsCore.LOGGER.info("SOCKET configuration about to be SET");
            couchDbInstance.setConfiguration(HTTPD_SECTION, SOCKET_OPTION, socketConfig);

            // Do not make application fail because of an optimisation.
        } catch (Exception e) {
            SirsCore.LOGGER.log(Level.WARNING, "Cannot check 'no delay' configuration", e);
        }
    }
    
    /**
     * Configure CouchDB to force client authentication. Not avoidable.
     */
    private void setAuthenticationRequired() {
        String authValue;
        try {
            authValue = couchDbInstance.getConfiguration(AUTH_SECTION, REQUIRE_USER_OPTION);
        } catch (DbAccessException e) {
            authValue = null;
        }
        if (!"true".equals(authValue)) {
            couchDbInstance.setConfiguration(AUTH_SECTION, REQUIRE_USER_OPTION, "true");
        }
    }
    
    /**
     * Allow current couchdb service to be requested by any host. The "bind_adress"
     * property, which lists allowed hosts, will be overrided only if it's null or 
     * if it contains a single local address (default couchdb configuration). It 
     * means you still can restrain accesses to localhost by setting it as follow :
     * "localhost, 127.0.0.1".
     */
    private void setOpenWorld() {
        String bindAdress;
        try {
            bindAdress = couchDbInstance.getConfiguration(HTTPD_SECTION, BIND_ADDRESS_OPTION);
        } catch (DbAccessException e) {
            bindAdress = null;
        }
        if (bindAdress == null || LOCAL_URL.matcher(bindAdress).matches()) {
            couchDbInstance.setConfiguration(HTTPD_SECTION, BIND_ADDRESS_OPTION, "0.0.0.0");
        }
        
        String corsValue;
        try {
            corsValue = couchDbInstance.getConfiguration(HTTPD_SECTION, ENABLE_CORS_OPTION);
        } catch (DbAccessException e) {
            corsValue = null;
        }
        if (!"true".equals(corsValue)) {
            couchDbInstance.setConfiguration(HTTPD_SECTION, ENABLE_CORS_OPTION, "true");
        }
    }
    
    /**
     * Make Cors filter accept all types of requests from any host.
     * Cors accepted origins will be overrided only if its not already present in
     * couchdb configuration (default configuration).
     * 
     * Cors accepted methods will be overrided only if its not already present in
     * couchdb configuration (default configuration).
     */
    private void setCorsUseless() {
        String credentials;
        try {
            credentials = couchDbInstance.getConfiguration(CORS_SECTION, CREDENTIALS_OPTIONS);
        } catch (DbAccessException e) {
            credentials = null;
        }
        if (!"true".equals(credentials)) {
            couchDbInstance.setConfiguration(CORS_SECTION, CREDENTIALS_OPTIONS, "true");
        }
        
        try {
            couchDbInstance.getConfiguration(CORS_SECTION, ORIGINS_OPTIONS);
        } catch (Exception e) {
            couchDbInstance.setConfiguration(CORS_SECTION, ORIGINS_OPTIONS, "*");
        }
        
        try {
            couchDbInstance.getConfiguration(CORS_SECTION, METHODS_OPTIONS);
        } catch (Exception e) {
            couchDbInstance.setConfiguration(CORS_SECTION, METHODS_OPTIONS, "GET, POST, PUT, DELETE");
        }
    }
    
    
    private static class SirsClientBuilder extends StdHttpClient.Builder {

        @Override
        public HttpClient configureClient() {
            // TODO : build our own non-deprecated http client. See https://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html
            HttpClient client = super.configureClient();
            if (client instanceof DefaultHttpClient) {
                final DefaultHttpClient tmpClient = (DefaultHttpClient) client;
                tmpClient.setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()));
                tmpClient.setCredentialsProvider(new SystemDefaultCredentialsProvider());
                tmpClient.addRequestInterceptor(new PreemptiveAuthRequestInterceptor(), 0);
            } else {
                throw new IllegalArgumentException("Cannot configure http connection parameters.");
            }
            return client;
        }        
    }
}
