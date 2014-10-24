/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
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
    private Map<String, Boolean> columnDataFlags;

    public GenericImporter(Database accessDatabase) {
        this.accessDatabase = accessDatabase;
        this.dateTimeFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
        this.columnDataFlags = new HashMap<>();
        this.tableName = this.getTableName().toString();
        
        // Set the data flags to false for all the columns used by the importer. 
        this.getUsedColumns().stream().forEach((column) -> {this.columnDataFlags.put(column, Boolean.FALSE);});
        
        System.out.println("=================================================");
        System.out.println("======== IMPORTER CHECK for table : "+tableName+"====");
        try {
            // Detect the empty fields.
            final List<String> emptyFields = this.getEmptyUsedFields();
            if (!emptyFields.isEmpty()) {
                System.out.println("Empty used fields for table " + tableName + " : ");
                emptyFields.stream().forEach((field) -> {
                    System.out.println(field);
                });
            } else {
                //System.out.println("No emty fields for table " + tableName);
            }

            // Detect the coluns used by the importer that do not exist in the table.
            final List<String> erroneousFields = this.getErroneousUsedFields();
            if (!erroneousFields.isEmpty()) {
                System.out.println("Erroneous fields for table " + tableName + " : ");
                erroneousFields.stream().forEach((field) -> {
                    System.out.println(field);
                });
            } else {
                //System.out.println("No erronous fields for table " + tableName);
            }

            // Detect the coluns forgotten by the importer but containing data;
            final List<String> forgottenFields = this.getForgottenFields();
            if (!forgottenFields.isEmpty()) {
                System.out.println("Forgotten fields for table " + tableName + " : ");
                forgottenFields.stream().forEach((field) -> {
                    System.out.println(field);
                });
            } else {
                //System.out.println("No erronous fields for table " + tableName);
            }
        } catch (IOException ex) {
            Logger.getLogger(GenericImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("*************************************************\n");
    }
    
    /**
     * 
     * @return the list of the column names used by the importer. This method must
     * not return the whole columns from the table, but only those used by the importer.
     */
    public abstract List<String> getUsedColumns();
    
    /**
     * 
     * @return The table name used by the importer. 
     */
    public abstract String getTableName();
    
    /**
     * Compute the maps referencing the retrieved objects.
     * @throws java.io.IOException
     * @throws fr.sym.util.importer.AccessDbImporterException
     */
    protected abstract void compute() throws IOException, AccessDbImporterException;
    
    
    public List<String> getTableColumns() throws IOException {
        final List<String> names = new ArrayList<>();
        this.accessDatabase.getTable(tableName).getColumns().stream().forEach((column) -> {
            names.add(column.getName());
        });

        return names;
    }

    /**
     * Check all the columns used by the importer exists in the table.
     * @return 
     */
    private List<String> getErroneousUsedFields() throws IOException{
        final List<String> erroneousUsedColumn = new ArrayList<>();
        final Table table = this.accessDatabase.getTable(tableName);
        
        // Check all used columns
        this.getUsedColumns().stream().forEach((usedColumnName) -> {
            boolean isPresent = false;
            for (final Column column : table.getColumns()) {

                if (column.getName().equals(usedColumnName)) {
                    isPresent = true;
                    break;
                }
            }
            if (!isPresent) {
                erroneousUsedColumn.add(usedColumnName);
            }

        });
        return erroneousUsedColumn;
    }
    
    /**
     * 
     * @return The list of column names used by the importer which are empty.
     * @throws IOException 
     */
    private List<String> getEmptyUsedFields() throws IOException{
        final List<String> emptyFields = new ArrayList<>();
        final Iterator<Row> it = this.accessDatabase.getTable(tableName).iterator();
        
        // For each table row
        while(it.hasNext()){
            final Row row = it.next();
            
            // For eache table column
            this.getUsedColumns().stream().forEach((column) -> {
                
                // Look for data in the cell if the data flag of the column is
                // false. If there is data, set the flag to true.
                if(!this.columnDataFlags.get(column) && row.get(column)!=null)
                    this.columnDataFlags.put(column, Boolean.TRUE);
            });
            
            // If all the columns contains data, do not continue to look for data
            // in the following rows and break the loop.
            if(!this.columnDataFlags.containsValue(Boolean.FALSE)) break;
        }
        
        // List the column names detected to not contain data.
        this.getUsedColumns().stream().forEach((column) -> {
                if(!this.columnDataFlags.get(column))
                    emptyFields.add(column);
            });
        return emptyFields;
    }
    
    /**
     * 
     * @return The list of table columns names ignored by the importer but
     * containing data.
     * @throws IOException 
     */
    private List<String> getForgottenFields() throws IOException{
        final List<String> forgottenFields = new ArrayList<>();
        final Iterator<Row> it = this.accessDatabase.getTable(tableName).iterator();
        final List<String> potentialForgottenFields = this.getTableColumns();
        potentialForgottenFields.removeAll(this.getUsedColumns());
        
        // For each table row
        while(it.hasNext()){
            final Row row = it.next();
            
            // For eache table column
            this.getTableColumns().stream().forEach((column) -> {
                
                
                
                    if(potentialForgottenFields.contains(column)  && row.get(column)!=null){
                        forgottenFields.add(column);
                        potentialForgottenFields.remove(column);
                    }
            });
            
            if(potentialForgottenFields.isEmpty()) break;
        }
        
        return forgottenFields;
    }
}
