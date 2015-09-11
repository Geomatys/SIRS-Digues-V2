/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Describe a relation between two tables.
 *
 * @author Alexis Manin (Geomatys)
 */
public class Relation {

    /** Column in the current table which will be used as filter to find the right row in foreign table*/
    public String localColumn;
    /**
     * Name of the foreign table to work with.
     */
    public String foreignTable;
    /**
     * Column to compare with local one to find the right row to use.
     */
    public String foreignColumn;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String foreignProperty;
}
