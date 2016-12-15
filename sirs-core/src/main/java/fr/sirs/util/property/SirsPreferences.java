/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 *
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
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
        REFERENCE_URL("Adresse des références", "Url à laquelle se trouvent les différents fichiers centralisés des références de l'application.", "http://sirs-digues.info/wp-content/tablesReferences/"),
        UPDATE_CORE_URL("Mise à jour de l'application", "Url à laquelle se trouve le service de mise à jour de l'application.", "http://sirs-digues.info/wp-content/updates/core.json"),
        UPDATE_PLUGINS_URL("Mise à jour des plugins", "Url à laquelle se trouve le service de mise à jour des plugins.", "http://sirs-digues.info/wp-content/updates/plugins.json"),
        COUCHDB_LOCAL_ADDR("Addresse de la base CouchDB locale", "Addresse d'accès à la base CouchDB locale, pour les réplications sur le poste.", "http://127.0.0.1:5984/"),
        DESIGNATION_AUTO_INCREMENT("Auto-incrément des désignations", "Lorsqu'un nouvel élément sera créé, sa désignation sera automatiquement remplie avec une valeur numérique"
                + " déterminée à partir de l'objet du même type ayant une déisgnation de forme numérique la plus haute trouvée dans la base de données, + 1.", Boolean.FALSE.toString());

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
     * @throws IOException Si on échoue à lire ou créer le fichier contenant les propriétés.
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
     * @throws IOException Si on échoue à lire le fichier contenant les propriétés.
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
     * @throws IOException Si on échoue à écrire le fichier contenant les propriétés.
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
