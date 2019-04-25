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
import java.util.HashMap;
import java.util.Map;

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
    
    public void setWithPreferencesColumns(Map<String, ColumnState> withPreferencesColumns){
        this.withPreferencesColumns = withPreferencesColumns;
    }

    /**
     * Ajout ou mise à jour de préférences pour une colonne.
     *
     * @param newColumnPreference
     */
    
    public void addColumnPreference(ColumnState newColumnPreference) {
        this.withPreferencesColumns.put(newColumnPreference.getName(), newColumnPreference);
    }

    public ColumnState getPreferencesFor(String columnName) {
        return this.withPreferencesColumns.get(columnName);
    }

}
