package fr.sym.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import net.sf.jasperreports.engine.JasperReport;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStoreUtilities;
import org.geotoolkit.display2d.service.OutputDef;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.FeatureUtilities;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.report.JasperReportService;
import org.xml.sax.SAXException;

/**
 * <p>This class provides utilities for two purposes:</p>
 * <ul>
 * <li>generating Jasper Reports templates mapping the classes of the model.</li>
 * <li>generating portable documents (.pdf) based on the templates on the one 
 * hand and the instances on the other hand.</li>
 * </ul>
 * <p>These are tools for printing functionnalities.</p>
 * @author Samuel Andrés (Geomatys)
 */
public class PrinterUtilities {
    
    private static final List<String> falseGetter = new ArrayList<>();
    
    static{
        falseGetter.add("getClass");
        falseGetter.add("isNew");
        falseGetter.add("getAttachments");
        falseGetter.add("getRevisions");
        falseGetter.add("getConflicts");
        falseGetter.add("getDocumentId");
    }
    
    /**
     * <p>Generate the specific Jasper Reports template for a given class.
     * This method is based on a meta-template defined in 
     * src/main/resources/fr/sym/jrxml/metaTemplate.jrxml
     * and produce a specific template :
     * "src/main/resources/fr/sym/jrxml/ClassName.jrxml"</p>
     * @param classToMap
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    static public void generateJasperReportsTemplate(final Class classToMap) 
            throws ParserConfigurationException, IOException, SAXException, Exception {
        
        final JRDomWriter writer = new JRDomWriter(new FileInputStream(
                "src/main/resources/fr/sym/jrxml/metaTemplate.jrxml"));
        writer.setFieldsInterline(2);
        writer.setHeightMultiplicator(3);
        writer.setOutput(new File("src/main/resources/fr/sym/jrxml/"
                + classToMap.getSimpleName()+".jrxml"));
        writer.write(classToMap, null);
    }
    
    /**
     * <p>Print an object from the model. This method needs the object to have a 
     * corresponding jasper report template ClassNameToPrint.jrxml preexisting
     * at the path : /fr/sym/jrxml/</p>
     * @param objectToPrint Pojo to print.
     * @param avoidFields Names of the fields to avoid printing.
     * @return 
     * @throws Exception 
     */
    static public File print(final Object objectToPrint, List<String> avoidFields) throws Exception {
        
        if(avoidFields==null) avoidFields=new ArrayList<>();
        
        // Creates the Jasper Reports specific template from the generic template.
        final JRDomWriter writer = new JRDomWriter(new FileInputStream(
                "src/main/resources/fr/sym/jrxml/metaTemplate.jrxml"));
        writer.setFieldsInterline(2);
        final File template = File.createTempFile(objectToPrint.getClass().getSimpleName(), 
                ".jrxml", new File("src/main/resources/fr/sym/jrxml"));
        template.deleteOnExit();
        writer.setOutput(template);
        writer.write(objectToPrint.getClass(), avoidFields);
        
        // Retrives the compiled template and the feature type -----------------
        final Map.Entry<JasperReport, FeatureType> entry = JasperReportService.prepareTemplate(template);
        final JasperReport report = entry.getKey();
        final FeatureType type = entry.getValue();
        
        // Build the feature from the object to print --------------------------
        final Feature feature0 = FeatureUtilities.defaultFeature(type, "id0");
        
        final Method[] methods = objectToPrint.getClass().getMethods();
        for(final Method method : methods) {
            if (isGetter(method)) {
                final String fieldName;
                
                if (method.getName().startsWith("is")) {
                    fieldName = method.getName().substring(2, 3).toLowerCase()
                            + method.getName().substring(3);
                } else if (method.getName().startsWith("get")) {
                    fieldName = method.getName().substring(3, 4).toLowerCase()
                            + method.getName().substring(4);
                } else {
                    throw new Exception("This is an \"original\" getter.");
                }
                
                if(!avoidFields.contains(fieldName)){
                    feature0.setPropertyValue(fieldName, method.invoke(objectToPrint));
                }
            }
        }
        
        // Build the feature collection ----------------------------------------
        final FeatureCollection<Feature> featureCollection = FeatureStoreUtilities.collection(feature0);
        
        // Generate the report -------------------------------------------------
        final File fout = new File("src/test/resources/report"
                        + objectToPrint.getClass().getSimpleName() + ".pdf");
        OutputStream out = new FileOutputStream(fout);
        final OutputDef output = new OutputDef(JasperReportService.MIME_PDF, 
                out);
        JasperReportService.generateReport(report, featureCollection, null, output);
        return fout;
    }
    
    /**
     * <p>This method detects if a method is a getter.</p>
     * @param method
     * @return true if the method is a getter.
     */
    static public boolean isGetter(final Method method){
        if (method == null) 
            return false; 
        else 
            return (method.getName().startsWith("get") 
                || method.getName().startsWith("is"))
                && method.getParameterTypes().length == 0
                && !falseGetter.contains(method.getName());
    }

    /**
     * <p>This method detects if a method is a setter.</p>
     * @param method
     * @return true if the method is a setter. 
     */
    static public boolean isSetter(final Method method){
        if (method == null) 
            return false;
        else 
            return method.getName().startsWith("set")
                && method.getParameterTypes().length == 1
                && void.class.equals(method.getReturnType());
    }
    
    static public void main(String[] arg) throws Exception {
        
        final File rep = new File("../core/sirs-core-store/target/generated-sources/pojos/fr/symadrem/sirs/model");
        
        final Pattern pattern = Pattern.compile("(.*)\\.java"); 
        for (final String s : rep.list()) {  
            final Matcher matcher = pattern.matcher(s);
            while(matcher.find()){
                final String className = matcher.group(1);
                final Class classe = Class.forName("fr.symadrem.sirs.model."+className);
                PrinterUtilities.generateJasperReportsTemplate(classe);
            }
        }
    }
    
    private PrinterUtilities(){}
}
