package fr.sirs.util;

import static fr.sirs.util.JRColumnParameter.DisplayPolicy.LABEL;

/**
 *
 * @author Samuel Andrés (Geomatys) <samuel.andres at geomatys.com>
 */
public class JRColumnParameter {
    
    public enum DisplayPolicy{LABEL, LABEL_AND_DESIGNATION, LABEL_AND_CODE};
    
    private final String fieldName;
    private final float columnWidthCoeff;
    private final DisplayPolicy displayPolicy;
    
    public JRColumnParameter(final String fieldName, final float columnWidthCoeff, final DisplayPolicy displayPolicy){
        this.fieldName = fieldName;
        this.columnWidthCoeff = columnWidthCoeff;
        this.displayPolicy = displayPolicy;
    }
    
    public JRColumnParameter(final String fieldName, final float columnWidthCoeff){
        this(fieldName, columnWidthCoeff, LABEL);
    }
    
    public JRColumnParameter(final String fieldName){
        this(fieldName, 1.f, LABEL);
    }
    
    public String getFieldName(){return fieldName;}
    public float getColumnWidthCoeff(){return columnWidthCoeff;}
    public DisplayPolicy getDisplayPolicy(){return displayPolicy;}
}
