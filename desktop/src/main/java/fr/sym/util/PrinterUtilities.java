package fr.sym.util;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
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
    
    /**
     * Print an object from the model. This method needs the object to have a 
     * corresponding jasper report template ClassNameToPrint.jrxml preexisting
     * at the path : /fr/sym/jrxml/
     * @param objectToPrint
     * @throws Exception 
     */
    static public void print(Object objectToPrint) throws Exception {
        
        // Handles the appropriate template ------------------------------------
        final InputStream template = PrinterUtilities.class.getResourceAsStream(
                "/fr/sym/jrxml/" + objectToPrint.getClass().getSimpleName() + ".jrxml");
        
        // Retrives the compiled template and the feature type -----------------
        final Map.Entry<JasperReport, FeatureType> entry = JasperReportService.prepareTemplate(template);
        final JasperReport report = entry.getKey();
        final FeatureType type = entry.getValue();
        
        // Build the feature from the object to print --------------------------
        final Feature feature0 = FeatureUtilities.defaultFeature(type, "id0");
        
        Method[] methods = objectToPrint.getClass().getMethods();
        for(Method method : methods) {
            if(isGetter(method)) {
                final String fieldName;
                if (method.getName().startsWith("is")) {
                    fieldName = method.getName().substring(2, 3).toLowerCase() 
                        + method.getName().substring(3);
                } else if (method.getName().startsWith("get")) {
                    fieldName = method.getName().substring(3, 4).toLowerCase() 
                        + method.getName().substring(4);
                } else {
                    throw new Exception("This is an original getter.");
                }
                
                feature0.setPropertyValue(fieldName, method.invoke(objectToPrint));
            }
        }
        
        // Build the feature collection ----------------------------------------
        final FeatureCollection<Feature> featureCollection = FeatureStoreUtilities.collection(feature0);
        
        // Generate the report -------------------------------------------------
        final OutputDef output = new OutputDef(JasperReportService.MIME_PDF, 
                new FileOutputStream("src/test/resources/report"
                        + objectToPrint.getClass().getSimpleName() + ".pdf"));
        JasperReportService.generateReport(report, featureCollection, null, output);
    }
    
    /**
     * Detect if a method is a getter.
     * @param method
     * @return true if the method is a getter.
     */
    static public boolean isGetter(Method method){
        if (method == null) return false; 
        else if ((method.getName().startsWith("get") 
                || method.getName().startsWith("is"))
                && method.getParameterTypes().length == 0
                && !method.getName().equals("getClass")
                && !void.class.equals(method.getReturnType()))
            return true;
        else return false;
    }

    /**
     * Detect if a method is a setter.
     * @param method
     * @return true if the method is a setter. 
     */
    static public boolean isSetter(Method method){
        if (method == null) return false;
        else if (method.getName().startsWith("set")
                && method.getParameterTypes().length == 1
                && void.class.equals(method.getReturnType())) 
            return true;
        else return false;
    }
    
    static public void main(String[] arg) throws Exception {      
    }
}
