package fr.sirs.util;

import fr.sirs.core.component.DatabaseRegistry;
import fr.sirs.core.component.DesordreRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Element;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.StringConverter;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.ektorp.CouchDbConnector;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.display2d.service.OutputDef;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.report.FeatureCollectionDataSource;
import org.geotoolkit.report.JasperReportService;
import org.springframework.context.ConfigurableApplicationContext;

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
    
    private static final List<String> falseGetter = new ArrayList<>();
    static{
        falseGetter.add("getClass");
        falseGetter.add("isNew");
        falseGetter.add("getAttachments");
        falseGetter.add("getRevisions");
        falseGetter.add("getConflicts");
        falseGetter.add("getDocumentId");
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // FICHES DE DESORDRES
    ////////////////////////////////////////////////////////////////////////////
    private static final String META_TEMPLATE_DESORDRE = "/fr/sirs/jrxml/metaTemplateDesordre.jrxml";
    
    public static File print(List<String> avoidFields, final Previews previewLabelRepository, final StringConverter stringConverter, final Desordre desordre) throws Exception {
        
        // Creates the Jasper Reports specific template from the generic template.
//        final File templateFile = File.createTempFile(elements.get(0).getClass().getSimpleName(), JRXML_EXTENSION);
//        templateFile.deleteOnExit();
        final File templateFile = new File("/home/samuel/Bureau/desordreObservation.jrxml");
        
//        final JRDomWriterElementSheet templateWriter = new JRDomWriterElementSheet(PrinterUtilities.class.getResourceAsStream(META_TEMPLATE_ELEMENT));
//        templateWriter.setFieldsInterline(2);
//        templateWriter.setOutput(templateFile);
//        templateWriter.write(elements.get(0).getClass(), avoidFields);
        
        final JasperReport jasperReport = JasperCompileManager.compileReport(JRXmlLoader.load(templateFile));
                
        JasperPrint finalPrint = null;
//        for(final Element element : elements){
            final JRDataSource source = new ObjectDataSource(Collections.singletonList(desordre), previewLabelRepository, stringConverter);

            final Map<String, Object> parameters = new HashMap<>();
            parameters.put("logo", PrinterUtilities.class.getResourceAsStream("/fr/sirs/images/icon-sirs.png"));
            parameters.put("OBSERVATIONS_TABLE_DATA_SOURCE", new ObjectDataSource<>(desordre.observations, previewLabelRepository, stringConverter));
            parameters.put("OBSERVATIONS_2_TABLE_DATA_SOURCE", new ObjectDataSource<>(desordre.observations, previewLabelRepository, stringConverter));
            
            final JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, source);
            if(finalPrint==null) finalPrint=print;
            else{
                for(final JRPrintPage page : print.getPages()){
                    finalPrint.addPage(page);
                }
            }
//        }
        
        // Generate the report -------------------------------------------------
        final File fout = File.createTempFile("DESORDRE_OBSERVATION", PDF_EXTENSION);
        try (final FileOutputStream outStream = new FileOutputStream(fout)) {
            final OutputDef output = new OutputDef(JasperReportService.MIME_PDF, outStream);
            JasperReportService.generate(finalPrint, output);
        }
        return fout;
//        return null;
    }
    
    public static void main(String[] args) throws IOException {
        
//        StdHttpClient.Builder builder = new StdHttpClient.Builder().host("127.0.0.1").port(5984);
//
//        builder.username("geouser");
//        builder.password("geopw");
//
//        HttpClient httpClient = builder.build();
//        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
//        CouchDbConnector db = new StdCouchDbConnector("rhone22juin", dbInstance);
//
//        Previews previews = new Previews(db);
//        Desordre desordre = new DesordreRepository(db).getOne();
        
        DatabaseRegistry registry = new DatabaseRegistry();
        ConfigurableApplicationContext cac = registry.connectToSirsDatabase("rhone22juin", false, false, false);
        CouchDbConnector db = cac.getBean(CouchDbConnector.class);
        
        Previews previews = new Previews(db);
        Desordre desordre = new DesordreRepository(db).getOne();
        
        try {
            File pdfFile = PrinterUtilities.print(null, previews, new SirsStringConverter(), desordre);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            }
        } catch (Exception ex) {
            Logger.getLogger(PrinterUtilities.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // FICHES DE RESULTATS DE REQUETES
    ////////////////////////////////////////////////////////////////////////////
    private static final String META_TEMPLATE_QUERY = "/fr/sirs/jrxml/metaTemplateQuery.jrxml";
    
    public static File print(List<String> avoidFields, final FeatureCollection featureCollection) throws Exception {
        
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
        
        try (final FileOutputStream outStream = new FileOutputStream(fout)) {
            final OutputDef output = new OutputDef(JasperReportService.MIME_PDF, outStream);
            final Map<String, Object> parameters = new HashMap<>();
            parameters.put("logo", PrinterUtilities.class.getResourceAsStream("/fr/sirs/images/icon-sirs.png"));
            parameters.put("TABLE_DATA_SOURCE", new FeatureCollectionDataSource(featureCollection));

            final JasperPrint print = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());
            JasperReportService.generate(print, output);
//        JasperReportService.generateReport(report, featureCollection, parameters, output);
        }
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
     * <p>This specific template is used to print objects of the model.</p>
     * 
     * @param elements Pojos to print. The list must contain at least one element. 
     * If it contains more than one, they must be all of the same class.
     * @param avoidFields Names of the fields to avoid printing.
     * @param previewLabelRepository
     * @param stringConverter
     * @return 
     * @throws Exception 
     */
    public static File print(final List<String> avoidFields, 
            final Previews previewLabelRepository, final StringConverter stringConverter, final List<? extends Element> elements) throws Exception {
        
        // Creates the Jasper Reports specific template from the generic template.
        final File templateFile = File.createTempFile(elements.get(0).getClass().getSimpleName(), JRXML_EXTENSION);
        templateFile.deleteOnExit();
        
        final JRDomWriterElementSheet templateWriter = new JRDomWriterElementSheet(PrinterUtilities.class.getResourceAsStream(META_TEMPLATE_ELEMENT));
        templateWriter.setFieldsInterline(2);
        templateWriter.setOutput(templateFile);
        templateWriter.write(elements.get(0).getClass(), avoidFields);
        
        final JasperReport jasperReport = JasperCompileManager.compileReport(JRXmlLoader.load(templateFile));
                
        JasperPrint finalPrint = null;
        for(final Element element : elements){
            final JRDataSource source = new ObjectDataSource(Collections.singletonList(element), previewLabelRepository, stringConverter);

            final Map<String, Object> parameters = new HashMap<>();
            parameters.put("logo", PrinterUtilities.class.getResourceAsStream("/fr/sirs/images/icon-sirs.png"));
            final JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, source);
            if(finalPrint==null) finalPrint=print;
            else{
                for(final JRPrintPage page : print.getPages()){
                    finalPrint.addPage(page);
                }
            }
        }
        
        // Generate the report -------------------------------------------------
        final File fout = File.createTempFile(elements.get(0).getClass().getSimpleName(), PDF_EXTENSION);
        try (final FileOutputStream outStream = new FileOutputStream(fout)) {
            final OutputDef output = new OutputDef(JasperReportService.MIME_PDF, outStream);
            JasperReportService.generate(finalPrint, output);
        }
        return fout;
    }
    
    /*
    
    Pour l'impression de plusieurs documents, plusieurs solutions (http://stackoverflow.com/questions/24115885/combining-two-jasper-reports)
    
    
    solution 1
    
    List<JasperPrint> jasperPrints = new ArrayList<JasperPrint>();
// Your code to get Jasperreport objects
JasperReport jasperReportReport1 = JasperCompileManager.compileReport(jasperDesignReport1);
jasperPrints.add(jasperReportReport1);
JasperReport jasperReportReport2 = JasperCompileManager.compileReport(jasperDesignReport2);
jasperPrints.add(jasperReportReport2);
JasperReport jasperReportReport3 = JasperCompileManager.compileReport(jasperDesignReport3);
jasperPrints.add(jasperReportReport3);

JRPdfExporter exporter = new JRPdfExporter();
//Create new FileOutputStream or you can use Http Servlet Response.getOutputStream() to get Servlet output stream
// Or if you want bytes create ByteArrayOutputStream
ByteArrayOutputStream out = new ByteArrayOutputStream();
exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrints);
exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
exporter.exportReport();
byte[] bytes = out.toByteArray();
    
    
    solution 2 (adoptée pour le moment) :
    
    JasperPrint jp1 = JasperFillManager.fillReport(url.openStream(), parameters,
                    new JRBeanCollectionDataSource(inspBean));
JasperPrint jp2 = JasperFillManager.fillReport(url.openStream(), parameters,
                    new JRBeanCollectionDataSource(inspBean));

List pages = jp2 .getPages();
        for (int j = 0; j < pages.size(); j++) {
        JRPrintPage object = (JRPrintPage)pages.get(j);
        jp1.addPage(object);

}
JasperViewer.viewReport(jp1,false);
    
    */
    
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
    
    public static String getFieldNameFromSetter(final Method setter){
        return setter.getName().substring(3, 4).toLowerCase()
                            + setter.getName().substring(4);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    private PrinterUtilities(){}
}
