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
package fr.sirs.theme.ui.pojotable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    
    // Liste des colonnes 'cachées' par l'utilisateur
    final private List<String> unVisibleColumns =new ArrayList<>();

    // Map associant le nom de colonne à sa position et largeur : remplie lorsque 
    // l'utilisateur a rendu une colonne visible, qu'il en a déplacée une ou 
    // qu'il en a redimensionné une.
    final private Map<String, Double[]> visibleNameAndwidthColumns = new HashMap<>();

    public TableColumnsPreferences() {
        this.pojoClass = null;
    }
    
    public TableColumnsPreferences(final Class pojoClass) {
        this.pojoClass = pojoClass;
    }

    public Class getPojoClass() {
        return pojoClass;
    }

    public void setPojoClass(Class pojoClass) {
        this.pojoClass = pojoClass;
    }
    
    public List<String> getUnVisibleColumns() {
        return unVisibleColumns;
    }
 
    public Map<String, Double[]> getVisibleNameAndwidthColumns() {
        return visibleNameAndwidthColumns;
    }

    
    

}
