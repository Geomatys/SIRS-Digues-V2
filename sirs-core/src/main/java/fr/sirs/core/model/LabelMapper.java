package fr.sirs.core.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class LabelMapper {
    
    private Class modelClass;
    private Map<String, String> map;
    
    public LabelMapper(final Class modelClass){
        this.modelClass = modelClass;
        loadConfigFile(modelClass.getSimpleName());
    }

    private void loadConfigFile(final String className) {
        map = new HashMap<>();
        try {
            
            BufferedReader buff = new BufferedReader(new InputStreamReader(
                    LabelMapper.class.getResourceAsStream("/fr/sirs/core/model/" + className + ".properties")));
       
            try {
                String line;
                int commentIndex = 0;
                String[] lineKeyValue;
                while ((line = buff.readLine()) != null) {
                    commentIndex = line.indexOf("#");
                    if (commentIndex != -1) {
                        line = line.substring(0, commentIndex);
                    }
                    lineKeyValue = line.split("=");
                    if (lineKeyValue.length == 2) {
                        map.put(lineKeyValue[0], lineKeyValue[1]);
                    }
                }
            } finally {
                // dans tous les cas, on ferme nos flux
                buff.close();
            }
        } catch (IOException ioe) {
            // erreur de fermeture des flux
            System.out.println("Erreur --" + ioe.toString());
        }
    }
    
    public void setModelClass(final Class modelClass){
        this.modelClass = modelClass;
    }
    public Class getModelClass(){return this.modelClass;}
    
    /**
     * 
     * @param plural
     * @return 
     */
    public String mapClassName(final boolean plural){
        if(plural){
            if(map.get("class_name_plural")==null) return mapClassName();
            return map.get("class_name_plural");
        } else{
            return mapClassName();
        }
    }
    
    public String mapClassName(){
        if(map.get("class_name")==null) return this.modelClass.getSimpleName();
        return map.get("class_name");
    }
    
    public String mapPropertyName(final String property){
        if(map.get(property)==null) return property;
        return map.get(property);
    }
    
    
    public static String mapClassName(final Class modelClass, final boolean plural){
        final LabelMapper labelMapper = new LabelMapper(modelClass);
        return labelMapper.mapClassName(plural);
    }
    
    public static String mapClassName(final Class modelClass){
        final LabelMapper labelMapper = new LabelMapper(modelClass);
        return labelMapper.mapClassName();
    }
    
    public static String mapPropertyName(final Class modelClass, final String property){
        final LabelMapper labelMapper = new LabelMapper(modelClass);
        return labelMapper.mapPropertyName(property);
    }
}
