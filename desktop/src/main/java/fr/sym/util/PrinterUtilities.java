/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util;

import fr.symadrem.sirs.model.Digue;
import fr.symadrem.sirs.model.TronconGestionDigue;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import net.sf.jasperreports.engine.JasperReport;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStoreUtilities;
import org.geotoolkit.display2d.service.OutputDef;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.FeatureUtilities;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.report.JasperReportService;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class PrinterUtilities {
    
    static public void print(Object objectToPrint) throws Exception {
        
        // Handles the appropriate template ------------------------------------
        final InputStream template = PrinterUtilities.class.getResourceAsStream(
                "/fr/sym/jrxml/" + objectToPrint.getClass().getSimpleName() + ".jrxml");
        System.out.println(objectToPrint.getClass().getSimpleName());
        // Retrives the compiled template and the feature type -----------------
        final Map.Entry<JasperReport, FeatureType> entry = JasperReportService.prepareTemplate(template);
        final JasperReport report = entry.getKey();
        final FeatureType type = entry.getValue();
        
        // Build the feature from the object to print --------------------------
        final Feature feature0 = FeatureUtilities.defaultFeature(type, "id0");
        System.out.println(type);
        
        Method[] methods = objectToPrint.getClass().getMethods();
        for(Method method : methods){
            if(isGetter(method)) {
                String fieldName = method.getName().substring(3, 4).toLowerCase() 
                        + method.getName().substring(4);
                feature0.setPropertyValue(fieldName, method.invoke(objectToPrint));
            }
        }
        
        // Build the feature collection ----------------------------------------
        final FeatureCollection<Feature> featureCollection = FeatureStoreUtilities.collection(feature0);
        //System.out.println(featureCollection);
        
        // Generate the report -------------------------------------------------
        final OutputDef output = new OutputDef(JasperReportService.MIME_PDF, 
                new FileOutputStream("/home/samuel/Bureau/report"
                        + objectToPrint.getClass().getSimpleName() + ".pdf"));
        JasperReportService.generateReport(report, featureCollection, null, output);
    }
    
    
    static public void main(String[] arg) throws Exception {
        
         Digue digue0 = new Digue();
         digue0.setIdDigue(0);
         digue0.setLibelleDigue("Grande Digue");
         digue0.setCommentaireDigue("Cette digue est en mauvais état et présente "
                 + "des signes évidents de vétusté et de délabrement avancé. Des"
                 + "travaux urgents s'imposent faute de quoi d'importants riques"
                 + "de rupture sont à prévoir.");
         digue0.setDateDerniereMaj(Calendar.getInstance());
        
        print(digue0);

         
         
        TronconGestionDigue tronconGestionDigue0 = new TronconGestionDigue();
        tronconGestionDigue0.setIdDigue(Long.valueOf(0));
        tronconGestionDigue0.setCommentaireTroncon("Ceci est un tronçon de la digue.");
        tronconGestionDigue0.setDateDebutValGestionnaireD(Calendar.getInstance());
        tronconGestionDigue0.setDateDebutValTroncon(Calendar.getInstance());
        tronconGestionDigue0.setDateDerniereMaj(Calendar.getInstance());
        tronconGestionDigue0.setDateFinValGestionnaireD(Calendar.getInstance());
        tronconGestionDigue0.setDateFinValTroncon(Calendar.getInstance());
        tronconGestionDigue0.setIdOrgGestionnaire(null);
        tronconGestionDigue0.setIdSystemeRepDefaut(null);
        tronconGestionDigue0.setIdTronconGestion(null);
        tronconGestionDigue0.setIdTypeRive(0);
        tronconGestionDigue0.setLibelleTronconGestion("Tronçon principal de la Grande Digue");
        tronconGestionDigue0.setNomTronconGestion("Tronçon du moulin");
         
        print(tronconGestionDigue0);

    }
    
    
    
    static public boolean isGetter(Method method){
        if(!method.getName().startsWith("get"))      return false;
        if(method.getParameterTypes().length != 0)   return false; 
        if(method.getName().equals("getClass"))      return false; 
        if(void.class.equals(method.getReturnType())) return false;
        return true;
    }

    static public boolean isSetter(Method method){
        if(!method.getName().startsWith("set")) return false;
        if(method.getParameterTypes().length != 1) return false;
        return true;
    }
}
