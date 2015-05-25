package fr.sirs.util;

import fr.sirs.core.component.Previews;
import fr.sirs.core.model.Element;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.util.StringConverter;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.display2d.service.OutputDef;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.report.FeatureCollectionDataSource;
import org.geotoolkit.report.JasperReportService;

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
    
    private static final String JRXML_EXTENSION = ".jrxml";
    private static final String PDF_EXTENSION = ".pdf";
    
    ////////////////////////////////////////////////////////////////////////////
    // FICHES DE RESULTATS DE REQUETES
    ////////////////////////////////////////////////////////////////////////////
    private static final String META_TEMPLATE_QUERY = "/fr/sirs/jrxml/metaTemplateQuery.jrxml";
    private static final List<String> falseGetter = new ArrayList<>();
    
    static{
        falseGetter.add("getClass");
        falseGetter.add("isNew");
        falseGetter.add("getAttachments");
        falseGetter.add("getRevisions");
        falseGetter.add("getConflicts");
        falseGetter.add("getDocumentId");
    }
    
    static public File print(final FeatureCollection featureCollection, List<String> avoidFields) throws Exception {
        
        if(avoidFields==null) avoidFields=new ArrayList<>();
        
        // Creates the Jasper Reports specific template from the generic template.
        final JRDomWriterQueryResultSheet writer = new JRDomWriterQueryResultSheet(PrinterUtilities.class.getResourceAsStream(META_TEMPLATE_QUERY));
        writer.setFieldsInterline(2);
        final File template = File.createTempFile(featureCollection.getFeatureType().getName().getLocalPart(), JRXML_EXTENSION);
        template.deleteOnExit();
        writer.setOutput(template);
        writer.write(featureCollection.getFeatureType(), avoidFields);
        
        // Retrives the compiled template and the feature type -----------------
        final Map.Entry<JasperReport, FeatureType> entry = JasperReportService.prepareTemplate(template);
        final JasperReport report = entry.getKey();
        final FeatureType type = entry.getValue();
        
        // Generate the report -------------------------------------------------
        final File fout = File.createTempFile(featureCollection.getFeatureType().getName().getLocalPart(), PDF_EXTENSION);
        fout.deleteOnExit();
        
        final OutputDef output = new OutputDef(JasperReportService.MIME_PDF, new FileOutputStream(fout));
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("logo", PrinterUtilities.class.getResourceAsStream("/fr/sirs/images/icon-sirs.png"));
        parameters.put("TABLE_DATA_SOURCE", new FeatureCollectionDataSource(featureCollection));
        
        
        final JasperPrint print = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());
        JasperReportService.generate(print, output);
//        JasperReportService.generateReport(report, featureCollection, parameters, output);
        return fout;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // FICHES SYNOPTIQUES D'ELEMENTS DU MODELE
    ////////////////////////////////////////////////////////////////////////////
    private static final String META_TEMPLATE_ELEMENT = "/fr/sirs/jrxml/metaTemplateElement.jrxml";
    
    /**
     * <p>Generate the specific Jasper Reports template for a given class.
     * This method is based on a meta-template defined in 
     * src/main/resources/fr/sirs/jrxml/metaTemplate.jrxml
     * and produce a specific template : ClassName.jrxml".</p>
     * 
     * <p>Then, this specific template is used to print an object of the model.</p>
     * @param element Pojo to print.
     * @param avoidFields Names of the fields to avoid printing.
     * @param previewLabelRepository
     * @param stringConverter
     * @return 
     * @throws Exception 
     */
    static public File print(final Element element, final List<String> avoidFields, 
            final Previews previewLabelRepository, final StringConverter stringConverter) throws Exception {
        
        // Creates the Jasper Reports specific template from the generic template.
        final File templateFile = File.createTempFile(element.getClass().getSimpleName(), JRXML_EXTENSION);
        templateFile.deleteOnExit();
        
        final JRDomWriterElementSheet templateWriter = new JRDomWriterElementSheet(PrinterUtilities.class.getResourceAsStream(META_TEMPLATE_ELEMENT));
        templateWriter.setFieldsInterline(2);
        templateWriter.setOutput(templateFile);
        templateWriter.write(element.getClass(), avoidFields);
        
        final JasperReport jasperReport = JasperCompileManager.compileReport(JRXmlLoader.load(templateFile));
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("logo", PrinterUtilities.class.getResourceAsStream("/fr/sirs/images/icon-sirs.png"));
        final JRDataSource source = new ObjectDataSource(Collections.singletonList(element), previewLabelRepository, stringConverter);
        
        final JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, source);
        
        // Generate the report -------------------------------------------------
        final File fout = File.createTempFile(element.getClass().getSimpleName(), PDF_EXTENSION);
        fout.deleteOnExit();
        
        final OutputDef output = new OutputDef(JasperReportService.MIME_PDF, new FileOutputStream(fout));
        JasperReportService.generate(print, output);
        return fout;
    }
    
    static public File print(final Element objectToPrint, final List<String> avoidFields) throws Exception {
        return print(objectToPrint, avoidFields, null, null);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // METHODES UTILITAIRES
    ////////////////////////////////////////////////////////////////////////////
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
    
    ////////////////////////////////////////////////////////////////////////////
    private PrinterUtilities(){}
}
