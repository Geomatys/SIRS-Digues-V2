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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sirs.SIRS;
import fr.sirs.core.model.Element;
import fr.sirs.theme.ui.PojoTable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javafx.scene.control.TableColumn;

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

    @JsonIgnore
    private Class pojoClass;

    @JsonIgnore
    final private ObjectMapper objectMapper = new ObjectMapper();
    @JsonIgnore
    final private File filePref;

    // Map associant le nom d'une colonne (keys) aux préférences de l'utilisateur (values).
    final private Map<Integer, ColumnState> withPreferencesColumns = new HashMap<>();

    public TableColumnsPreferences() {
        this(null);
    }

    public TableColumnsPreferences(final Class pojoClass) {
        this.pojoClass = pojoClass;

        String path = "preferences.json";
        this.filePref = new File(path);
        this.loadReferencesFromJsonPath();
    }

//    public TableColumnsPreferences(final Class pojoClass, final Map<String, ColumnState> withPreferencesColumns) {
//        this.pojoClass = pojoClass;
//        this.withPreferencesColumns = withPreferencesColumns;
//    }
    //=========
    //Methodes
    //=========
    
    /**
     * Application des préférences utilisateurs aux colonnes d'une TableView de 
     * en attribut d'une PojoTable
     * 
     * - Ne marche pas s'il y a suppression de colonnes mais à priori pas possible.
     * 
     * @param columns 
     */
    public void applyPreferencesToTableColumns(List<TableColumn<Element, ?>> columns) {

        List<TableColumn<Element, ?>> oldColumns = new ArrayList<>();
        Map<String, TableColumn<Element, ?>> changedColumns = new HashMap<>();

        oldColumns = columns;

        for (int i = 0; i < columns.size(); i++) {
            ColumnState preference = withPreferencesColumns.get(i);
            if ( !(preference == null) ){
                if(! (preference.getName() == null) ) {
                        
                        TableColumn<Element, ?> updatedColumn = columns.get(i);
                        
//                        if (preference.getName().equals(((PojoTable.PropertyColumn) oldColumns.get(i)).getName())) {

                        if (!(preference.getName().equals(getColumnRef(oldColumns.get(i))))) {
                            //nom de colonne différent on alimente 'changedColumns' et on remplace
                            TableColumn<Element, ?> extractedColumn = changedColumns.get(preference.getName());

                            if (extractedColumn == null) {
                                //Si la colonne associée à cette position dans les 
                                //préférences ne fait pas partie des colonnes extraites, 
                                //on l'y ajoute puis on la mofifie.
                                TableColumn<Element, ?> oldColumn=oldColumns.get(i);
                                changedColumns.put(getColumnRef(oldColumn), oldColumn);
                                updatedColumn = oldColumns.stream()
                                        .filter( col-> ((getColumnRef(col)!=null)&&(getColumnRef(col).equals(preference.getName()))) )
                                        .findFirst()
                                        .orElseThrow(() -> new RuntimeException("Problème de référencement des colonnes.\n"
                                                + "Aucune référence de colonne ne correspond au nom de la préférence."));
                                
                                
                            } else {
                                //Si la colonne fait partie des colonnes extraites,
                                //On la remplace par celle extraite.
                                updatedColumn = extractedColumn;
                                changedColumns.remove(preference.getName());

                            }
                            
                            // mise à jour de l'épaisseur et de la visibilité
                            updatedColumn.setVisible(preference.isVisible());
                            updatedColumn.setVisible(preference.isVisible());
                            
                        }
                }
            }

        }
    }

    /**
     * Méthode static permettant d'identifier une colonne par sont Id ou par le
     * nom de sa classe.
     * 
     * En effet la plupart des colonnes d'une PojoTable ont un nom permettant de 
     * les identifier. Lorsque ce n'est pas le cas (classe spécifique de colonnes)
     * on les identifie par leur nom de classe.
     * 
     * @param column
     * @return 
     */
    public static String getColumnRef(TableColumn<Element, ?> column){
        try{
            return ((PojoTable.PropertyColumn) column).getName();
        }catch(ClassCastException cce){
            return column.getClass().toString();
        }
    }
    
    /**
     * Ajout ou mise à jour de préférences pour une colonne.
     *
     * @param newColumnPreference
     */
    public void addColumnPreference(ColumnState newColumnPreference) {
        this.withPreferencesColumns.put(newColumnPreference.getPosition(), newColumnPreference);
    }

    /**
     * Récupère les préférences pour un nom de colonne donné.
     *
     * Attention il s'agit des préférences inclues dans la
     * Map<String, ColumnState> withPreferencesColumns ; Pas celles du fichier
     * Json.
     *
     * @param columnPosition : Position de la colonne dont on cherche les
     * préférences.
     * @return ColumnState indiquant les préférences de la colonne 'columnName'.
     */
    public ColumnState getPreferencesFor(Integer columnPosition) {
        return this.withPreferencesColumns.get(columnPosition);
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

//            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
//            String path = "/src/main/resources/target/" + pojoClass.toString() + "_preferences.json";
//            String path = "/target/" + pojoClass.toString() + "_preferences.json";
        try {

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
     * @return boolean indiquant si le chargement c'est bien déroulé (true).
     */
    final public boolean loadReferencesFromJsonPath() {
        try {
            TableColumnsPreferences readPref = objectMapper.readValue(filePref, TableColumnsPreferences.class);

            //Si on trouve des préférences on met à jour la Map withPreferencesColumns
            if ((readPref == null) || (readPref.getWithPreferencesColumns()).isEmpty()) {
                SIRS.LOGGER.log(Level.INFO, "Fichier {0} vide.", filePref);
            } else {
                readPref.getWithPreferencesColumns().forEach((colPosition, state) -> {
                    this.withPreferencesColumns.put(colPosition, state);
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

    public Map<Integer, ColumnState> getWithPreferencesColumns() {
        return withPreferencesColumns;
    }

//    public void setWithPreferencesColumns(Map<Integer, ColumnState> withPreferencesColumns) {
//        this.withPreferencesColumns = withPreferencesColumns;
//    }
}
