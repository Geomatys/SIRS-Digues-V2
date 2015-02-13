package fr.sirs;

import fr.sirs.core.Repository;
import fr.sirs.core.model.RefConduiteFermee;
import fr.sirs.core.model.RefNature;
import fr.sirs.core.model.RefProprietaire;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ReferenceChecker {

    private static final String MODEL_PACKAGE = "fr.sirs.core.model";

    private String referencesDirectoryPath;
    
    private ReferenceChecker(){}
    
    public ReferenceChecker(final String referencesDirectoryPath){
        this.referencesDirectoryPath = referencesDirectoryPath;
    }

    private final Map<Class, Map<Object, Object>> incoherentReferences = new HashMap<>();

    public Map<Class, Map<Object, Object>> getIncoherentReferences() {
        return incoherentReferences;
    }
    private final Map<Class, List<Object>> serverInstancesNotLocal = new HashMap<>();

    public Map<Class, List<Object>> getServerInstancesNotLocal() {
        return serverInstancesNotLocal;
    }

    private final Map<Class, List<Object>> localInstancesNotOnTheServer = new HashMap<>();

    public Map<Class, List<Object>> getLocalInstancesNotOnTheServer() {
        return localInstancesNotOnTheServer;
    }

    private List<Class> serverClassReferences;

    public List<Class> getServerClassReferences() {
        return serverClassReferences;
    }

    private List<String> serverClassNameReferencesNotLocal;

    public List<String> getServerClassNameReferencesNotLocal() {
        return serverClassNameReferencesNotLocal;
    }

    private List<Class> localClassReferences;

    public List<Class> getLocalClassReferences() {
        return localClassReferences;
    }

    private List<String> localClassNameReferencesNotOnServer;

    public List<String> getLocalClassNameReferencesNotOnServer() {
        return localClassNameReferencesNotOnServer;
    }

    public void checkAllReferences() throws IOException {

        /*
         Récupération des classes de référence du serveur. Inclut la
         vérification que ces classes existent bien dans l'application.
         */
        serverClassReferences = buildServerClassReferences();

        ////////////////////////////////////////////////////////////////////////
        /*
         Récupération des classes de référence de l'application. 
         */
        localClassReferences = Session.getReferences();

        /*
         Vérification que les classes de l'application sont toutes recensées sur
         le serveur.
         */
        localClassNameReferencesNotOnServer = new ArrayList<>();
        for (final Class reference : localClassReferences) {
            if (!serverClassReferences.contains(reference)) {
                localClassNameReferencesNotOnServer.add(reference.getSimpleName());
            }
        }
        
        /*
         On vérifie ensuite l'identité des instances : pour chaque classe de 
         référence locale, pourvu qu'elle soit référencée sur le serveur,
         on va vérifier que les instances sont "identiques" (identité à définir).
         */
        for (final Class reference : localClassReferences) {
            if (serverClassReferences.contains(reference)) {
                final ReferenceClassChecker referenceClassChecker = new ReferenceClassChecker(reference);
                referenceClassChecker.checkReferenceClass();
                referenceClassChecker.update();
            }
        }
    }

    public final class ReferenceClassChecker {

        private final Class referenceClass;
        private List<Object> fileReferences;
        private List<Object> localReferences;

        private ReferenceClassChecker(final Class referenceClass) {
            this.referenceClass = referenceClass;
        }

        /**
         * Check one reference class
         *
         * @param referenceClass
         */
        private void checkReferenceClass() {
            try {
                final URL referenceURL = referenceURL();
                final File referenceFile = retriveFileFromURL(referenceURL);

                fileReferences = readReferenceFile(referenceFile);
                localReferences = Injector.getSession().getRepositoryForClass(referenceClass).getAll();

                final List<Object> currentServerInstances = new ArrayList(fileReferences);

                /*
                 On vérifie que toutes les instances du serveur sont présentes localement.
                 Sinon, on récupère les instances du serveur non présentes localement.
                 */
                final Method getId = referenceClass.getMethod("getId");

                /*
                 Pour toutes les instances de références locales :
                 1) On vérifie qu'elle est bien présente sur les serveur ;
                 2) Que son état sur le serveur est identique à son état local ;
                 3) Si elle est présente sur le serveur et que son état a changé, on enregistre les deux états dans la map dédiée.
                 4) Si elle n'est pas présente sur le serveur, on l'enregistre dans la liste dédiée.
                 */
                for (final Object localReferenceInstance : localReferences) {
                    boolean presentOnServer = false;

                    try {
                        final Object localId = getId.invoke(localReferenceInstance);
                        for (final Object serverReferenceInstance : currentServerInstances) {
                            try {
                                final Object serverId = getId.invoke(serverReferenceInstance);
                                if (localId instanceof String
                                        && localId.equals(serverId)) {
                                    presentOnServer = true;
                                    currentServerInstances.remove(serverReferenceInstance);
                                    if (!sameReferences(localReferenceInstance, serverReferenceInstance)) {
                                        registerIncoherentReferences(localReferenceInstance, serverReferenceInstance);
                                    }
                                    break;
                                }
                            } catch (IllegalAccessException | InvocationTargetException ex) {
                                SIRS.LOGGER.log(Level.SEVERE, ex.getMessage());
                            }
                        }
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        SIRS.LOGGER.log(Level.SEVERE, ex.getMessage());
                    }

                    if (!presentOnServer) {
                        if (localInstancesNotOnTheServer.get(referenceClass) == null) {
                            localInstancesNotOnTheServer.put(referenceClass, new ArrayList());
                        }
                        localInstancesNotOnTheServer.get(referenceClass).add(localReferenceInstance);
                    }
                }

                /*
                 Maintenant qu'on a vérifié que toutes les références locales de la 
                 classe étaient sur le serveur (cas des instances de références qui 
                 auraient été supprimées sur le serveur), il faut vérifier s'il n'y 
                 aurait pas des instances de références qui seraient présentes sur 
                 le serveur mais pas localement (cas des instances de références qui 
                 auraient été ajoutées sur le serveur). 
            
                 Pour cela Il faut examiner uniquement les instances de références du 
                 serveur qui n'auraient pas trouvé de référence locale 
                 correspondante (danslla liste currentServerInstances qui a été mise 
                 à jour au fur et à mesure).
                 */
                serverInstancesNotLocal.put(referenceClass, currentServerInstances);

            } catch (MalformedURLException ex) {
                SIRS.LOGGER.log(Level.SEVERE, ex.getMessage());
            } catch (IOException ex) {
                SIRS.LOGGER.log(Level.SEVERE, ex.getMessage());
            } catch (NoSuchMethodException | SecurityException ex) {
                SIRS.LOGGER.log(Level.SEVERE, ex.getMessage());
            }
        }

        /**
         * Build URL for given reference class.
         *
         * @param locationDir
         * @param referenceClass
         * @return
         * @throws MalformedURLException
         */
        private URL referenceURL() throws MalformedURLException {
            return new URL(referencesDirectoryPath + referenceClass.getSimpleName() + ".csv");
        }
        
        
        /**
         * 
         * Builds a reference instance form A CSV record.
         *
         * Note : this method needs the name fiels of the given referenceClass
         * map the header column names.
         *
         * This method supports the following field types :
         *
         * <ol>
         * <li>String. Note if the column name is set to "id", the method
         * generates an id by concatenating the referenceClass simple name to
         * ":" and to the value of the id column;</li>
         * <li>LocalDate (see ISO_DATE format);</li>
         * <li>LocalDateTime (see ISO_DATE_TIME format).</li>
         * </ol>
         * 
         * @param record
         * @return
         * @throws NoSuchMethodException
         * @throws InstantiationException
         * @throws IllegalAccessException
         * @throws IllegalArgumentException
         * @throws InvocationTargetException 
         */
        private Object buildReferenceInstance(final CSVRecord record) {

            if (record == null) {
                return null;
            }

            Object referenceInstance = null;

            try {
                final Constructor constructor = referenceClass.getConstructor();
                referenceInstance = constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                SIRS.LOGGER.log(Level.SEVERE, null, ex);
            }

            for (final String header : record.toMap().keySet()) {
                try {
                    final Method getter = referenceClass.getMethod("get" + header.substring(0, 1).toUpperCase() + header.substring(1));
                    final Class type = getter.getReturnType();
                    final Method setter = referenceClass.getMethod("set" + header.substring(0, 1).toUpperCase() + header.substring(1), type);

                    if (String.class.equals(type)) {
                        if ("id".equals(header)) {
                            setter.invoke(referenceInstance, referenceClass.getSimpleName() + ":" + record.get(header));
                        } else {
                            setter.invoke(referenceInstance, record.get(header));
                        }
                    } else if (LocalDateTime.class.equals(type)) {
                        try {
                            setter.invoke(referenceInstance, LocalDateTime.of(LocalDate.parse(record.get(header), DateTimeFormatter.ISO_DATE), LocalTime.MIN));
                        } catch (DateTimeParseException ex) {
                            try {
                                setter.invoke(referenceInstance, LocalDateTime.parse(record.get(header), DateTimeFormatter.ISO_DATE_TIME));
                            } catch (DateTimeParseException ex2) {
                                SIRS.LOGGER.log(Level.INFO, ex2.getMessage());
                            }
                            SIRS.LOGGER.log(Level.INFO, ex.getMessage());
                        }
                    }

                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                    SIRS.LOGGER.log(Level.SEVERE, null, ex);
                }
            }

            return referenceInstance;
        }
        

        private List<Object> readReferenceFile(final File file) throws FileNotFoundException, IOException {
            
            final Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(new FileReader(file));
            final List<Object> fileRefs = new ArrayList<>();
            
            for (final CSVRecord record : records) {
                final Object referenceInstance = buildReferenceInstance(record);
                if(referenceInstance!=null) fileRefs.add(referenceInstance);
            }
            return fileRefs;
        }
        
        private void update(){
            final List<Object> updated = new ArrayList<>();
            if(fileReferences!=null){
                for(final Object fileReference : fileReferences){
                    Object localInstance = null;
                    for(final Object localReference : localReferences){
                        if(fileReference.equals(localReference)) {
                            localInstance = localReference;
                            break;
                        }
                    }

                    if(localInstance==null){
                        final Repository repository = Injector.getSession().getRepositoryForClass(referenceClass);
                        repository.add(fileReference);
                        updated.add(fileReference);
                    }
                    else{
                        if(!localInstance.toString().equals(fileReference.toString())){
                            final Repository repository = Injector.getSession().getRepositoryForClass(referenceClass);
                            try {
                                final Method getRevision = referenceClass.getMethod("getRevision");
                                final Method setRevision = referenceClass.getMethod("setRevision", String.class);
                                final String revision = (String) getRevision.invoke(localInstance);
                                setRevision.invoke(fileReference, revision);
                                repository.update(fileReference);
                                updated.add(fileReference);// On mémorise la référence mise à jour
                            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                SIRS.LOGGER.log(Level.SEVERE, ex.getMessage());
                            }
                        }
                    }
                }
            }
            
            // On élimine les instances de références mises à jour des map correspondantes du ReferenceChecker.
            serverInstancesNotLocal.get(referenceClass).removeAll(updated);
            final Map<Object, Object> incoherentInstances = incoherentReferences.get(referenceClass);
            if(incoherentInstances!=null){
                for(final Object updatedInstance : updated){
                    final Set keySet = incoherentInstances.keySet();
                    for(final Object localInstanceKey : keySet){
                        if(incoherentInstances.get(localInstanceKey)==updatedInstance){
                            incoherentInstances.remove(localInstanceKey);
                            break;
                        }
                    }
                }
            }
        }

        private void registerIncoherentReferences(final Object localReferenceInstance, final Object serverReferenceInstance) {

            if (referenceClass == null || localReferenceInstance == null || serverReferenceInstance == null) {
                return;
            }

            if (incoherentReferences.get(referenceClass) == null) {
                incoherentReferences.put(referenceClass, new HashMap<>());
            }

            final Map<Object, Object> classMap = incoherentReferences.get(referenceClass);

            classMap.put(localReferenceInstance, serverReferenceInstance);
        }

    }

    /**
     * Compare reference instances.
     *
     * @param localReferenceInstance
     * @param serverReferenceInstance
     * @return
     */
    private static boolean sameReferences(final Object localReferenceInstance, final Object serverReferenceInstance) {
        // equals ne vérifie pas l'identité de contenu, mais l'égalité des identifiants !!!!
        if (localReferenceInstance.equals(serverReferenceInstance)) {
            // La méthode equals ne se basant que sur les ID, on vérifie en plus l'égalité des contenus avec "toString"
            if (!localReferenceInstance.toString().equals(serverReferenceInstance.toString())) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
    
    /**
     * Return the file content located at the URL.
     *
     * @param url
     * @return null if the input URL is null
     * @throws IOException
     */
    private static File retriveFileFromURL(final URL url) throws IOException {

        if (url == null) {
            return null;
        }

        final File file = File.createTempFile("tempReference", ".csv");
        final URLConnection connection = url.openConnection();
        try (final InputStream inputStream = connection.getInputStream()) {

            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            final FileOutputStream fos = new FileOutputStream(file);
            final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fos);

            int r = 0;
            while (true) {
                r = inputStreamReader.read();
                if (r != -1) {
                    outputStreamWriter.write(r);
                } else {
                    break;
                }
            }
            outputStreamWriter.flush();
            outputStreamWriter.close();
            fos.close();
            inputStreamReader.close();
        }
        return file;
    }

    /**
     * Reference names located on the server references index file are supposed
     * to be the simple name of the model class related to the given reference.
     *
     * @return
     */
    private List<Class> buildServerClassReferences() throws IOException {
        final List<String> names = retrieveServerClassNameReferences();
        final List<Class> classes = new ArrayList<>();
        serverClassNameReferencesNotLocal = new ArrayList<>();
        for (final String name : names) {
            final String className = MODEL_PACKAGE + "." + name;
            try {
                classes.add(Class.forName(className));
            } catch (ClassNotFoundException ex) {
                serverClassNameReferencesNotLocal.add(className);
            }
        }

        return classes;
    }

    /**
     * TODO Retrive all the server references (parse the index.csv file).
     *
     * @return
     */
    private List<String> retrieveServerClassNameReferences() throws MalformedURLException, IOException {
        final List<String> result;
        final URL indexURL = new URL(referencesDirectoryPath + "index.csv");
        result = readIndexFile(retriveFileFromURL(indexURL));
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // UTILITY STATIC METHODS
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Reads server references index file and retrieve all corresponding class
     * names.
     *
     * @param file
     * @return
     * @throws IOException
     */
    private static List<String> readIndexFile(final File file) throws IOException {
        if (file == null) {
            return null;
        }

        final List<String> result = new ArrayList<>();
        final BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        while (true) {
            final String line = bufferedReader.readLine();

            if (line == null) {
                break;
            }

            result.add(line.replaceAll("\\s", ""));

        }
        return result;
    }

}
