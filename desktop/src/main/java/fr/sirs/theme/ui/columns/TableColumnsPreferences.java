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
package fr.sirs.theme.ui.columns;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sirs.SIRS;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Classe utilisée pour stocker les préférence de l'utilisateur quant à la
 * présentation des PojoTable : largeur des colonnes, colonnes visibles...
 *
 * -> Objectif : sauvegarder en local les changements apportés par l'utilisateur
 * à la table.
 *
 * @author Matthieu Bastianelli (Geomatys)
 */
//@Component   
public class TableColumnsPreferences {

    private Class pojoClass;

    // Map associant le nom d'une colonne (keys) aux préférences de l'utilisateur (values).
    private Map<String, ColumnState> withPreferencesColumns;

    public TableColumnsPreferences() {
        this(null);

//        this.pojoClass = null;
//        this.withPreferencesColumns = new HashMap<>();
    }

    public TableColumnsPreferences(final Class pojoClass) {
        this(pojoClass, new HashMap<>());
//        this.pojoClass = pojoClass;
    }

    public TableColumnsPreferences(final Class pojoClass, final Map<String, ColumnState> withPreferencesColumns) {
        this.pojoClass = pojoClass;
        this.withPreferencesColumns = withPreferencesColumns;
    }

    //=========
    //Methodes
    //=========
    /**
     * Ajout ou mise à jour de préférences pour une colonne.
     *
     * @param newColumnPreference
     */
    public void addColumnPreference(ColumnState newColumnPreference) {
        this.withPreferencesColumns.put(newColumnPreference.getName(), newColumnPreference);
    }

    /**
     * Récupère les préférences pour un nom de colonne donné.
     *
     * Attention il s'agit des préférences inclues dans la
     * Map<String, ColumnState>
     * withPreferencesColumns ; Pas celles du fichier Json.
     *
     * @param columnName : nom de la colonne dont on cherche les préférences.
     * @return ColumnState indiquant les préférences de la colonne 'columnName'.
     */
    public ColumnState getPreferencesFor(String columnName) {
        return this.withPreferencesColumns.get(columnName);
    }

    /**
     * Méthode permettant de sauvegarder les préférences (ergonomie) des
     * colonnes portées par l'instance de TableColumnsPreferences.
     *
     * Cette méthode repose sur l'usage de Jackson.
     *
     * @return boolean : indiquant si la sauvegarde à réussie (if true).
     */
    public boolean saveInJson() {

        final ObjectMapper objectMapper = new ObjectMapper();

//            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
//            String path = "/src/main/resources/target/" + pojoClass.toString() + "_preferences.json";
//            String path = "/target/" + pojoClass.toString() + "_preferences.json";
        try {

            String path = "preferences.json";
            File filePref = new File(path);
            if (!filePref.exists()) {
                SIRS.LOGGER.log(Level.INFO, "Création du fichier Json lors de la sauvegarde des préférences utilisateur.");
                filePref.createNewFile();
            }

//                final Path targetDir = basedir.toPath().resolve("target");
//                if (!Files.isDirectory(targetDir)) {
//                    Files.createDirectory(targetDir);
//                }
//                final File target = targetDir.resolve(project.getArtifactId() + ".json").toFile();
//                File target = targetDir.resolve(project.getArtifactId() + ".json").toFile();
            objectMapper.writeValue(filePref, this);

            return true;

        } catch (IOException ioe) {
            SIRS.LOGGER.log(Level.WARNING, "Echec lors de l'écriture des préférences de la PojoTable.", ioe);
            return false;
        }

    }

    /**
     * Charge les préférences utilisateur pour les colonnes d'une PojoTable
     * depuis un fichier Json.
     *
     * @param filePref fichier Json comptenant les préférences sauvegardées.
     * @return boolean indiquant si le chargement c'est bien déroulé (true).
     */
    public boolean loadReferencesFromJsonPath(File filePref) {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            TableColumnsPreferences readPref = objectMapper.readValue(filePref, TableColumnsPreferences.class);
            
            //Si on trouve des préférences on met à jour la Map withPreferencesColumns
            if((readPref == null) || (readPref.getWithPreferencesColumns()).isEmpty()){
                SIRS.LOGGER.log(Level.INFO, "Fichier {0} vide.", filePref);
            }else{
                readPref.getWithPreferencesColumns().forEach((name, state) -> {
                    this.withPreferencesColumns.put(name, state);
                }); 
            }
            return true;

        } catch (IOException ioe) {
            SIRS.LOGGER.log(Level.WARNING, "Exception loading columns preferences for a PojoTable.", ioe);
            return false;
        }

    }

    //===================
    //Getter and Setter 
    //===================
    public Class getPojoClass() {
        return pojoClass;
    }

    // Utilisable uniquement pour les TableColumnsPreferences 
    public void setPojoClass(Class pojoClass) {
        this.pojoClass = pojoClass;
    }

    public Map<String, ColumnState> getWithPreferencesColumns() {
        return withPreferencesColumns;
    }

    public void setWithPreferencesColumns(Map<String, ColumnState> withPreferencesColumns) {
        this.withPreferencesColumns = withPreferencesColumns;
    }

}
