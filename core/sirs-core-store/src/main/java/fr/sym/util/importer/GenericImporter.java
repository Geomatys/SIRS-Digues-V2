/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An importer is suposed to retrive data from one and only one table of the given database.
 * @author Samuel Andrés (Geomatys)
 */
public abstract class GenericImporter {
    
    private final String tableName;
    protected Database accessDatabase;
    protected final DateTimeFormatter dateTimeFormatter;
    protected Map<String, Boolean> columnUse;

    public GenericImporter(Database accessDatabase) {
        this.accessDatabase = accessDatabase;
        this.dateTimeFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
        this.columnUse = new HashMap<>();
        this.getColumns().stream().forEach((column) -> {this.columnUse.put(column, Boolean.FALSE);});
        this.tableName = this.getTableName();
        
        try {
            final List<String> emptyFields = this.getEmptyFields();
            if (!emptyFields.isEmpty())
                System.out.println("Empty fields for table "+tableName+" : "+this.getEmptyFields());
            else
                System.out.println("No emty fields for table "+tableName);
        } catch (IOException ex) {
            Logger.getLogger(GenericImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * 
     * @return the list of the column names used by the importer. This method must
     * not return the whole columns from the table, but only those used by the importer.
     */
    public abstract List<String> getColumns();
    
    /**
     * 
     * @return The table name used by the importer. 
     */
    public abstract String getTableName();
    
    /**
     * 
     * @return The list of column names used by the importer which are empty.
     * @throws IOException 
     */
    private List<String> getEmptyFields() throws IOException{
        final List<String> emptyFields = new ArrayList<>();
        final Iterator<Row> it = this.accessDatabase.getTable(tableName).iterator();
        
        while(it.hasNext()){
            final Row row = it.next();
            this.getColumns().stream().forEach((column) -> {
                if(!this.columnUse.get(column) && row.get(column)!=null)
                    this.columnUse.put(column, Boolean.TRUE);
            });
            if(!this.columnUse.containsValue(Boolean.FALSE)) break;
        }
        
        this.getColumns().stream().forEach((column) -> {
                if(!this.columnUse.get(column))
                    emptyFields.add(column);
            });
        return emptyFields;
    }
    
    //TODO
    /**
     * 
     * @return The list of table columns names not ignored by the importer but
     * containing data.
     * @throws IOException 
     */
    private List<String> getRemainingFields() throws IOException{
        return null;
    }
}
