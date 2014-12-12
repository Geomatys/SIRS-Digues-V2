package fr.sirs.core.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
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
                    System.out.println(line);
                    commentIndex = line.indexOf("#");
                    if (commentIndex != -1) {
                        line = line.substring(0, commentIndex);
                    }
                    lineKeyValue = line.split("=");
                    if (lineKeyValue.length == 2) {
                        map.put(lineKeyValue[0], lineKeyValue[1]);
                    } else {
                        System.out.println("Erreur de syntaxe dans le fichier properties.");
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
    
    public String mapPropertyName(final String property){
        if(map.get(property)==null) return property;
        return map.get(property);
    }
    
    
    public static void main(String[] args) {
        LabelMapper lm = new LabelMapper(ArticleJournal.class);
        System.out.println(lm.mapPropertyName("type_rapport"));
    }
}
