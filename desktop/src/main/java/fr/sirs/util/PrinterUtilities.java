package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.EchelleLimnimetrique;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.Observation;
import fr.sirs.core.model.ObservationReseauHydrauliqueFerme;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.core.model.Photo;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.VoieDigue;
import static fr.sirs.util.JRDomWriterDesordreSheet.OBSERVATION_TABLE_DATA_SOURCE;
import static fr.sirs.util.JRDomWriterDesordreSheet.PHOTOS_SUBREPORT;
import static fr.sirs.util.JRDomWriterDesordreSheet.PHOTO_DATA_SOURCE;
import static fr.sirs.util.JRDomWriterDesordreSheet.PRESTATION_TABLE_DATA_SOURCE;
import static fr.sirs.util.JRDomWriterDesordreSheet.RESEAU_OUVRAGE_TABLE_DATA_SOURCE;
import static fr.sirs.util.JRDomWriterDesordreSheet.VOIRIE_TABLE_DATA_SOURCE;
import fr.sirs.util.property.SirsPreferences;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javafx.util.StringConverter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.display2d.service.OutputDef;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.report.CollectionDataSource;
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
    
    private static final String TEMPLATE_PHOTOS = "/fr/sirs/jrxml/photoTemplate.jrxml";

    ////////////////////////////////////////////////////////////////////////////
    // FICHES DÉTAILLÉES DE DESORDRES
    ////////////////////////////////////////////////////////////////////////////
    private static final String META_TEMPLATE_RESEAU_FERME = "/fr/sirs/jrxml/metaTemplateReseauFerme.jrxml";

    public static File printReseauFerme(final List<String> avoidDesordreFields,
            final List<String> avoidObservationFields,
            final List<String> reseauFields,
            final Previews previewLabelRepository,
            final StringConverter stringConverter,
            final List<ReseauHydrauliqueFerme> reseaux,
            final boolean printPhoto, final boolean printReseauOuvrage) throws Exception {

        JasperPrint firstPrint = null;
        final List<JasperPrint> followingPrints = new ArrayList<>();
        for(final ReseauHydrauliqueFerme reseau : reseaux){

            // Creates the Jasper Reports specific template from the generic template.
            final File templateFile = File.createTempFile(Desordre.class.getName(), JRXML_EXTENSION);
            templateFile.deleteOnExit();

            final JRDomWriterReseauFermeSheet templateWriter = new JRDomWriterReseauFermeSheet(
                    PrinterUtilities.class.getResourceAsStream(META_TEMPLATE_RESEAU_FERME),
                    avoidDesordreFields, avoidObservationFields,
                    reseauFields, printPhoto, printReseauOuvrage);
            templateWriter.setOutput(templateFile);
            templateWriter.write(reseau);

            final JasperReport jasperReport = JasperCompileManager.compileReport(JRXmlLoader.load(templateFile));

            final JRDataSource source = new ObjectDataSource(Collections.singletonList(reseau), previewLabelRepository, stringConverter);

            final Map<String, Object> parameters = new HashMap<>();
            parameters.put("logo", PrinterUtilities.class.getResourceAsStream("/fr/sirs/images/icon-sirs.png"));

            parameters.put(OBSERVATION_TABLE_DATA_SOURCE, new ObjectDataSource<>(reseau.getObservations(), previewLabelRepository, stringConverter));

            final List<Photo> photos = new ArrayList<>();
            for(final ObservationReseauHydrauliqueFerme observation : reseau.getObservations()){
                if(observation.getPhotos()!=null && !observation.getPhotos().isEmpty()){
                    photos.addAll(observation.getPhotos());
                }
            }
            if(reseau.getPhotos()!=null && !reseau.getPhotos().isEmpty()){
                photos.addAll(reseau.getPhotos());
            }

            if(printPhoto) {
                parameters.put(PHOTO_DATA_SOURCE, new ObjectDataSource<>(photos, previewLabelRepository, stringConverter));
            }

            if(printReseauOuvrage) {
                final List<ObjetReseau> reseauOuvrageList = new ArrayList<>();
                final List<List<? extends ObjetReseau>> retrievedLists = new ArrayList();
                retrievedLists.add(Injector.getSession().getRepositoryForClass(OuvrageHydrauliqueAssocie.class).get(reseau.getOuvrageHydrauliqueAssocieIds()));
                retrievedLists.add(Injector.getSession().getRepositoryForClass(ReseauHydrauliqueCielOuvert.class).get(reseau.getReseauHydrauliqueCielOuvertIds()));
                retrievedLists.add(Injector.getSession().getRepositoryForClass(ReseauHydrauliqueFerme.class).get(reseau.getStationPompageIds()));

                for(final List candidate : retrievedLists){
                    if(candidate!=null && !candidate.isEmpty()){
                        reseauOuvrageList.addAll(candidate);
                    }
                }

                parameters.put(RESEAU_OUVRAGE_TABLE_DATA_SOURCE, new ObjectDataSource<>(reseauOuvrageList, previewLabelRepository, stringConverter));
            }

            final JasperReport photosReport = net.sf.jasperreports.engine.JasperCompileManager.compileReport(PrinterUtilities.class.getResourceAsStream(TEMPLATE_PHOTOS));
            parameters.put(PHOTOS_SUBREPORT, photosReport);

            final JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, source);
            if(firstPrint==null) firstPrint=print;
            else followingPrints.add(print);
        }

        for(final JasperPrint print : followingPrints){
            for(final JRPrintPage page : print.getPages()){
                if(firstPrint!=null) firstPrint.addPage(page);
            }
        }

        // Generate the report -------------------------------------------------
        final File fout = File.createTempFile("RESEAU_HYDRAULIQUE_FERME_OBSERVATION", PDF_EXTENSION);
        try (final FileOutputStream outStream = new FileOutputStream(fout)) {
            final OutputDef output = new OutputDef(JasperReportService.MIME_PDF, outStream);
            JasperReportService.generate(firstPrint, output);
        }
        return fout;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // FICHES DÉTAILLÉES DE DESORDRES
    ////////////////////////////////////////////////////////////////////////////
    private static final String META_TEMPLATE_DESORDRE = "/fr/sirs/jrxml/metaTemplateDesordre.jrxml";
    
    public static File printDisorders(final List<String> avoidDesordreFields, 
            final List<String> avoidObservationFields, 
            final List<String> avoidPrestationFields, 
            final List<String> reseauFields,
            final Previews previewLabelRepository, 
            final StringConverter stringConverter, 
            final List<Desordre> desordres, 
            final boolean printPhoto, final boolean printReseauOuvrage, final boolean printVoirie)
        throws ParserConfigurationException, SAXException, JRException, TransformerException, IOException {
        
        JasperPrint firstPrint = null;
        final List<JasperPrint> followingPrints = new ArrayList<>();
        for(final Desordre desordre : desordres){
            
            // Creates the Jasper Reports specific template from the generic template.
            final File templateFile = File.createTempFile(Desordre.class.getName(), JRXML_EXTENSION);
            templateFile.deleteOnExit();

            final JRDomWriterDesordreSheet templateWriter = new JRDomWriterDesordreSheet(
                    PrinterUtilities.class.getResourceAsStream(META_TEMPLATE_DESORDRE), 
                    avoidDesordreFields, avoidObservationFields, avoidPrestationFields, 
                    reseauFields, printPhoto, printReseauOuvrage, printVoirie);
            templateWriter.setOutput(templateFile);
            templateWriter.write(desordre);

            final JasperReport jasperReport = JasperCompileManager.compileReport(JRXmlLoader.load(templateFile));
                
            final JRDataSource source = new ObjectDataSource(Collections.singletonList(desordre), previewLabelRepository, stringConverter);
            
            final Map<String, Object> parameters = new HashMap<>();
            parameters.put("logo", PrinterUtilities.class.getResourceAsStream("/fr/sirs/images/icon-sirs.png"));
            
            parameters.put(OBSERVATION_TABLE_DATA_SOURCE, new ObjectDataSource<>(desordre.observations, previewLabelRepository, stringConverter));
            
            parameters.put(PRESTATION_TABLE_DATA_SOURCE, new ObjectDataSource<>(Injector.getSession().getRepositoryForClass(Prestation.class).get(desordre.getPrestationIds()), previewLabelRepository, stringConverter));
            
            final List<Photo> photos = new ArrayList<>();
            for(final Observation observation : desordre.observations){
                if(observation.photos!=null && !observation.photos.isEmpty()){
                    photos.addAll(observation.photos);
                }
            }
            
            if(printPhoto) {
                parameters.put(PHOTO_DATA_SOURCE, new ObjectDataSource<>(photos, previewLabelRepository, stringConverter));
            }
            
            if(printReseauOuvrage) {
                final List<ObjetReseau> reseauOuvrageList = new ArrayList<>();
                final List<List<? extends ObjetReseau>> retrievedLists = new ArrayList();
                retrievedLists.add(Injector.getSession().getRepositoryForClass(EchelleLimnimetrique.class).get(desordre.getEchelleLimnimetriqueIds()));
                retrievedLists.add(Injector.getSession().getRepositoryForClass(OuvrageParticulier.class).get(desordre.getOuvrageParticulierIds()));
                retrievedLists.add(Injector.getSession().getRepositoryForClass(ReseauTelecomEnergie.class).get(desordre.getReseauTelecomEnergieIds()));
                retrievedLists.add(Injector.getSession().getRepositoryForClass(OuvrageTelecomEnergie.class).get(desordre.getOuvrageTelecomEnergieIds()));
                retrievedLists.add(Injector.getSession().getRepositoryForClass(OuvrageHydrauliqueAssocie.class).get(desordre.getOuvrageHydrauliqueAssocieIds()));
                retrievedLists.add(Injector.getSession().getRepositoryForClass(ReseauHydrauliqueCielOuvert.class).get(desordre.getReseauHydrauliqueCielOuvertIds()));
                retrievedLists.add(Injector.getSession().getRepositoryForClass(ReseauHydrauliqueFerme.class).get(desordre.getReseauHydrauliqueFermeIds()));
                
                for(final List candidate : retrievedLists){
                    if(candidate!=null && !candidate.isEmpty()){
                        reseauOuvrageList.addAll(candidate);
                    }
                }
                
                parameters.put(RESEAU_OUVRAGE_TABLE_DATA_SOURCE, new ObjectDataSource<>(reseauOuvrageList, previewLabelRepository, stringConverter));
            }
            
            if(printVoirie) {
                final List<ObjetReseau> voirieList = new ArrayList<>();
                final List<List<? extends ObjetReseau>> retrievedLists = new ArrayList();
                retrievedLists.add(Injector.getSession().getRepositoryForClass(OuvrageVoirie.class).get(desordre.getOuvrageVoirieIds()));
                retrievedLists.add(Injector.getSession().getRepositoryForClass(VoieDigue.class).get(desordre.getVoieDigueIds()));
                
                for(final List candidate : retrievedLists){
                    if(candidate!=null && !candidate.isEmpty()){
                        voirieList.addAll(candidate);
                    }
                }
                
                parameters.put(VOIRIE_TABLE_DATA_SOURCE, new ObjectDataSource<>(voirieList, previewLabelRepository, stringConverter));
            }
            
            final JasperReport photosReport = net.sf.jasperreports.engine.JasperCompileManager.compileReport(PrinterUtilities.class.getResourceAsStream(TEMPLATE_PHOTOS));
            parameters.put(PHOTOS_SUBREPORT, photosReport);
            
            final JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, source);
            if(firstPrint==null) firstPrint=print;
            else followingPrints.add(print);
        }
        
        for(final JasperPrint print : followingPrints){
            for(final JRPrintPage page : print.getPages()){
                if(firstPrint!=null) firstPrint.addPage(page);
            }
        }
        
        // Generate the report -------------------------------------------------
        final File fout = File.createTempFile("DESORDRE_OBSERVATION", PDF_EXTENSION);
        try (final FileOutputStream outStream = new FileOutputStream(fout)) {
            final OutputDef output = new OutputDef(JasperReportService.MIME_PDF, outStream);
            JasperReportService.generate(firstPrint, output);
        }
        return fout;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // FICHES DE RESULTATS DE REQUETES
    ////////////////////////////////////////////////////////////////////////////
    private static final String META_TEMPLATE_QUERY = "/fr/sirs/jrxml/metaTemplateQuery.jrxml";

    /**
     *
     * @param avoidFields
     * @param featureCollection
     * @return
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws JRException
     * @throws TransformerException
     */
    public static File print(List<String> avoidFields, final FeatureCollection featureCollection)
        throws IOException, ParserConfigurationException, SAXException, JRException, TransformerException {
        
        if(avoidFields==null) avoidFields=new ArrayList<>();
        
        // Creates the Jasper Reports specific template from the generic template.
        final JRDomWriterQueryResultSheet writer = new JRDomWriterQueryResultSheet(PrinterUtilities.class.getResourceAsStream(META_TEMPLATE_QUERY));
        writer.setFieldsInterline(2);
        final File template = File.createTempFile(featureCollection.getFeatureType().getName().tip().toString(), JRXML_EXTENSION);
        template.deleteOnExit();
        writer.setOutput(template);
        writer.write(featureCollection.getFeatureType(), avoidFields);
        
        // Retrives the compiled template and the feature type -----------------
        final Map.Entry<JasperReport, FeatureType> entry = JasperReportService.prepareTemplate(template);
        final JasperReport report = entry.getKey();
        final FeatureType type = entry.getValue();
        
        // Generate the report -------------------------------------------------
        final File fout = File.createTempFile(featureCollection.getFeatureType().getName().tip().toString(), PDF_EXTENSION);
        
        try (final FileOutputStream outStream = new FileOutputStream(fout)) {
            final OutputDef output = new OutputDef(JasperReportService.MIME_PDF, outStream);
            final Map<String, Object> parameters = new HashMap<>();
            parameters.put("logo", PrinterUtilities.class.getResourceAsStream("/fr/sirs/images/icon-sirs.png"));
            parameters.put(JRDomWriterQueryResultSheet.TABLE_DATA_SOURCE, new CollectionDataSource(featureCollection));

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
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws net.sf.jasperreports.engine.JRException
     * @throws javax.xml.transform.TransformerException
     */
    public static File print(final List<String> avoidFields, 
            final Previews previewLabelRepository, final StringConverter stringConverter, final List<? extends Element> elements)
            throws IOException, ParserConfigurationException, SAXException, JRException, TransformerException {
        
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
    
    /**
     * Utility method used to build complete path into jasper templates from 
     * local path only.
     * 
     * @param inputText
     * @return
     * @throws Exception 
     */
    public static InputStream streamFromText(final String inputText) throws Exception {
        final URI resultURI = imageUriFromText(inputText);
        try {
            return new FileInputStream(new File(resultURI));
        } catch(Exception e){
            SIRS.LOGGER.log(Level.INFO, "No image found at URI "+resultURI);
            return FXFileTextField.class.getResourceAsStream("/fr/sirs/images/imgNotFound.png");
        } 
    }

    public static URI imageUriFromText(final String inputText) throws Exception {
        final String rootPath = SirsPreferences.INSTANCE.getPropertySafe(SirsPreferences.PROPERTIES.DOCUMENT_ROOT);
        final URI resultURI;
        if (rootPath == null) {
            resultURI = inputText.matches("[A-Za-z]+://.+")? new URI(inputText) : Paths.get(inputText).toUri();
        } else {
            resultURI = SIRS.getDocumentAbsolutePath(inputText == null? "" : inputText).toUri();
        }
        return resultURI;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    private PrinterUtilities(){}
}
