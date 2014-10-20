/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer;

import com.healthmarketscience.jackcess.Database;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public abstract class GenericImporter {
    
    protected Database accessDatabase;
    protected final DateTimeFormatter dateTimeFormatter;

    public GenericImporter(Database accessDatabase) {
        this.accessDatabase = accessDatabase;
        this.dateTimeFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
    }
}
