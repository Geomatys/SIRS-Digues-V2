package fr.sirs.util.property;

import static fr.sirs.core.SirsCore.CONFIGURATION_PATH;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Properties;

/**
 * Définit les préférences liées à l'installation locale de l'application.
 * 
 * @author Alexis Manin (Geomatys)
 */
public class SirsPreferences extends Properties {
    
    private static final Path PREFERENCES_PATH = CONFIGURATION_PATH.resolve("preferences.properties");
    private static final String COMMENTS = null;
    
    public static enum PROPERTIES {
        
        DOCUMENT_ROOT("Dossier des documents", "Dossier racine où sont stockés les documents référencés par l'application.", null),
        REFERENCE_URL("Adresse des références", "Url à laquelle se trouvent les différents fichiers centralisés des références de l'application.", "http://sirs-digues.info/wp-content/tablesReferences/"),
        UPDATE_CORE_URL("Mise à jour de l'application", "Url à laquelle se trouve le service de mise à jour de l'application.", "http://sirs-digues.info/wp-content/updates/core.json"),
        UPDATE_PLUGINS_URL("Mise à jour des plugins", "Url à laquelle se trouve le service de mise à jour des plugins.", "http://sirs-digues.info/wp-content/updates/plugins.json"),
        DEFAULT_LOCAL_USER("Administrateur CouchDB local", "Login de l'administrateur pour la base couchDB locale.", null),
        DEFAULT_LOCAL_PASS("Mot de passe de l'administrateur CouchDB", "Le mot de passe de l'utilisateur CouchDB spécifié ici.", null),
        COUCHDB_LOCAL_ADDR("Addresse de la base CouchDB locale", "Addresse d'accès à la base CouchDB locale, pour les réplications sur le poste.", "http://127.0.0.1:5984/");
        public final String title;
        public final String description;
        public final String defaultValue;
        
        private PROPERTIES(final String title, final String description, final String defaultValue) {
            this.title = title;
            this.description = description;
            this.defaultValue = defaultValue;
        }
        
        public String getDefaultValue(){return defaultValue;}
    }

    /**
     * Charge les préférences depuis le système.
     * @throws IOException 
     */
    private SirsPreferences() throws IOException {
        super();

        if (!Files.isRegularFile(PREFERENCES_PATH)) {
            Files.createFile(PREFERENCES_PATH);
        }
        reload();
    }
    
    /**
     * L'instance unique à utiliser pour travailler avec les propriétés.
     */
    public static final SirsPreferences INSTANCE;
    static {
        try {
            INSTANCE = new SirsPreferences();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    
    /**
     * Recharge les propriétés depuis le disque.
     * @throws IOException 
     */
    public void reload() throws IOException {
        try (final InputStream stream = Files.newInputStream(PREFERENCES_PATH, StandardOpenOption.READ)) {
            this.load(stream);
        }
        
        for (final PROPERTIES prop : PROPERTIES.values()) {
            try {
                getProperty(prop);
            } catch (IllegalStateException e) {
                if (prop.getDefaultValue() != null) {
                    setProperty(prop.name(), prop.getDefaultValue());
                }
            }
        }
    }
    
    /**
     * Ecris les propriétés courantes sur le disque.
     * @throws IOException 
     */
    public void store() throws IOException {
        try (OutputStream propertyFile = Files.newOutputStream(PREFERENCES_PATH)) {
            store(propertyFile, COMMENTS);
        }
    }
    
    /**
     * Enregistre les préferences données dans le fichier de propriétés lié.
     * 
     * @param values Une table dont les clés sont les propriétés à mettre à jour,
     * accompagnées de leur valeur.
     * @throws IOException Si une erreur survient lors de la persistence des prpriétés.
     */
    public void store(final Map<PROPERTIES, String> values) throws IOException {
        for (final Map.Entry<PROPERTIES, String> entry : values.entrySet()) {
            setProperty(entry.getKey().name(), entry.getValue());
        }
        
        store();
        reload();
    }
    
    /**
     * Override {@link java.util.Properties#getProperty(String)} method to forbid returning null or empty value.
     * @param key key of the value to retrieve.
     * @return The value stored for queried property. Never null or empty.
     * @throws java.lang.IllegalStateException if queried property has no value.
     */
    @Override
    public String getProperty(String key) throws IllegalStateException {
        final String property = super.getProperty(key);
        if (property == null || property.isEmpty()) {
            throw new IllegalStateException("No valid "+key+ " property defined in "+PREFERENCES_PATH);
        } else {
            return property;
        }
    }

    /**
     * Override {@linkplain java.util.Properties#getProperty(String)} method to forbid returning null or empty value.
     * @param key key for the value to retrieve.
     * @return The value stored for queried property. Never null or empty.
     * @throws java.lang.IllegalStateException if queried property has no value.
     */
    public String getProperty(PROPERTIES key) throws IllegalStateException {
        return getProperty(key.name());
    }
    
    /**
     * @param key key of the value to retrieve.
     * @return The value stored for queried property. Can be null or empty.
     */
    public String getPropertySafe(String key) {
        return super.getProperty(key);
    }

    /**
     * @param key key for the value to retrieve.
     * @return The value stored for queried property. Can be null or empty
     */
    public String getPropertySafe(PROPERTIES key) {
        return getPropertySafe(key.name());
    }
}
