
package fr.sirs.plugin.document;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import static fr.sirs.SIRS.AUTHOR_FIELD;
import static fr.sirs.SIRS.COMMENTAIRE_FIELD;
import static fr.sirs.SIRS.DATE_MAJ_FIELD;
import static fr.sirs.SIRS.FOREIGN_PARENT_ID_FIELD;
import static fr.sirs.SIRS.LATITUDE_MAX_FIELD;
import static fr.sirs.SIRS.LATITUDE_MIN_FIELD;
import static fr.sirs.SIRS.LONGITUDE_MAX_FIELD;
import static fr.sirs.SIRS.LONGITUDE_MIN_FIELD;
import static fr.sirs.SIRS.VALID_FIELD;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.component.SQLQueryRepository;
import fr.sirs.core.h2.H2Helper;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ObjetPhotographiable;
import fr.sirs.core.model.Photo;
import fr.sirs.core.model.PhotoChoiceDocument;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.RapportModeleDocument;
import fr.sirs.core.model.RapportSectionDocument;
import fr.sirs.core.model.SQLQuery;
import fr.sirs.core.model.TemplateOdt;
import fr.sirs.plugin.document.ui.DocumentsPane;
import fr.sirs.theme.ColumnOrder;
import fr.sirs.util.SirsStringConverter;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.awt.image.BufferedImage;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javax.imageio.ImageIO;
import net.sf.jooreports.templates.DocumentTemplate;
import net.sf.jooreports.templates.DocumentTemplateException;
import net.sf.jooreports.templates.DocumentTemplateFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.ImageIOUtil;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.ektorp.DocumentNotFoundException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.db.JDBCFeatureStore;
import org.geotoolkit.db.h2.H2FeatureStore;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.type.NamesExt;
import org.geotoolkit.util.FileUtilities;
import org.odftoolkit.odfdom.dom.OdfContentDom;
import org.odftoolkit.odfdom.dom.attribute.text.TextAnchorTypeAttribute;
import org.odftoolkit.odfdom.dom.element.OdfStyleBase;
import org.odftoolkit.odfdom.dom.element.style.StyleGraphicPropertiesElement;
import org.odftoolkit.odfdom.dom.element.text.TextPElement;
import org.odftoolkit.odfdom.incubator.doc.draw.OdfDrawFrame;
import org.odftoolkit.odfdom.incubator.doc.draw.OdfDrawImage;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextParagraph;
import org.odftoolkit.odfdom.type.Color;
import org.odftoolkit.simple.Document;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.MasterPage;
import org.odftoolkit.simple.style.StyleTypeDefinitions;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.text.Paragraph;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ODTUtils {
    
    private static DocumentTemplate DEFAULT_TEMPLATE;
    
    public static final String[] COLUMNS_TO_IGNORE = new String[] {
        AUTHOR_FIELD, VALID_FIELD, FOREIGN_PARENT_ID_FIELD, LONGITUDE_MIN_FIELD,
        LONGITUDE_MAX_FIELD, LATITUDE_MIN_FIELD, LATITUDE_MAX_FIELD,
        DATE_MAJ_FIELD, COMMENTAIRE_FIELD,
        "prDebut", "prFin", "valid", "positionDebut", "positionFin", "epaisseur"};
    
    
    private static final int IMAGE_WIDTH = 140;
    
    private static final String TAB = "        ";
    
    private static final Logger LOGGER = Logging.getLogger(ODTUtils.class);
    
    public static void writeSummary(final FileTreeItem item, File file) throws Exception {
        final TextDocument doc = TextDocument.newTextDocument();
        for (FileTreeItem child : item.listChildrenItem()) {
            if (!child.getLibelle().equals(DocumentsPane.SAVE_FOLDER)) {
                write(doc, child, "", false, null);
            }
        }
        doc.save(file);
    }
    
    public static void writeDoSynth(final FileTreeItem item, File file, final Label uiProgressLabel) throws Exception {
        final TextDocument doc = TextDocument.newTextDocument();
        write(doc, (FileTreeItem) item, "", true, uiProgressLabel);
        doc.save(file);
    }
    
    private static void write(final TextDocument doc, final FileTreeItem item, String margin, boolean doSynth, final Label uiProgressLabel) {
        final Paragraph paragraph = doc.addParagraph("");
        
        if (item.isSe()) {
            paragraph.setFont(new Font("Arial", StyleTypeDefinitions.FontStyle.BOLD, 22, Color.BLACK, StyleTypeDefinitions.TextLinePosition.UNDER));
            margin = "";
            paragraph.appendTextContent(margin + item.getLibelle() + "\n");
            
        } else if (item.isDg()) {
            paragraph.setFont(new Font("Arial", StyleTypeDefinitions.FontStyle.BOLD, 18, Color.BLACK, StyleTypeDefinitions.TextLinePosition.UNDER));
            margin = "";
            paragraph.appendTextContent(margin + item.getLibelle() + "\n");
        } else if (item.isTr()) {
            paragraph.setFont(new Font("Arial", StyleTypeDefinitions.FontStyle.BOLD, 14, Color.BLACK, StyleTypeDefinitions.TextLinePosition.UNDER));
            margin = "";
            paragraph.appendTextContent(margin + item.getLibelle() + "\n");
        } else {
            paragraph.setFont(new Font("Arial", StyleTypeDefinitions.FontStyle.BOLD, 12, Color.BLACK, StyleTypeDefinitions.TextLinePosition.REGULAR));
            paragraph.appendTextContent(margin + " - " + item.getLibelle() + "\n");
        }
                
        List<FileTreeItem> directories = item.listChildrenItem(true, doSynth);
        List<FileTreeItem> files       = item.listChildrenItem(false, doSynth);
        
        if (!files.isEmpty()) {
            if (doSynth) {
                final String prefix = item.getLibelle() + " concatenation des fichiers: ";
                final int n = files.size();
                int i = 1;
                for (FileTreeItem child : files) {
                    try {
                        final Paragraph fileNameparagraph = doc.addParagraph("");
                        fileNameparagraph.setFont(new Font("Arial", StyleTypeDefinitions.FontStyle.BOLDITALIC, 12, Color.BLACK, StyleTypeDefinitions.TextLinePosition.REGULAR));
                        fileNameparagraph.appendTextContent(TAB + margin + " -- " + child.getLibelle() + " -- \n");
                        final int I = i;
                        Platform.runLater(()-> uiProgressLabel.setText(prefix + I + "/" + n));
                        concatenateFile(doc, child.getValue());
                        i++;
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                final Table table = Table.newTable(doc, files.size() + 1, 4);
                table.getCellByPosition(0, 0).setStringValue("Nom");
                table.getCellByPosition(0, 0).setCellBackgroundColor(new Color(109,149,182));

                table.getCellByPosition(1, 0).setStringValue("Taille");
                table.getCellByPosition(1, 0).setCellBackgroundColor(new Color(109,149,182));

                table.getCellByPosition(2, 0).setStringValue("N° Inventaire");
                table.getCellByPosition(2, 0).setCellBackgroundColor(new Color(109,149,182));

                table.getCellByPosition(3, 0).setStringValue("Lieu classement");
                table.getCellByPosition(3, 0).setCellBackgroundColor(new Color(109,149,182));

                table.getRowByIndex(0).getDefaultCellStyle();
                int i = 1; 
                for (FileTreeItem child : files) {

                    final String name      = child.getLibelle();
                    final String size      = child.getSize();
                    final String inventory = child.getInventoryNumber();
                    final String place     = child.getClassPlace();

                    table.getCellByPosition(0, i).setStringValue(name);
                    table.getCellByPosition(1, i).setStringValue(size);
                    table.getCellByPosition(2, i).setStringValue(inventory);
                    table.getCellByPosition(3, i).setStringValue(place);

                    i++;
                }
                doc.addParagraph("");
            }
        }
        
        for (FileTreeItem child : directories) {
            write(doc, child, TAB + margin, doSynth, uiProgressLabel);
        }
    }
    
    public static void write(final RapportModeleDocument item, File file, Map<String,Objet> elements, final Label uiProgressLabel, final String prefix) throws Exception {
        final List parts = new ArrayList<>();
        final File tempFolder = new File(file.getParentFile(),"temp_"+file.getName().split("\\.")[0]);
        for (RapportSectionDocument section : item.sections) {
            Platform.runLater(()->uiProgressLabel.setText(prefix + "Génération de la section : "+section.getLibelle()));
            switch (section.getType()) {
                case DOCUMENT : 
                    final TextDocument doc = TextDocument.newTextDocument();
                    doc.addParagraph(section.getLibelle() + "\n");
                    File f = new File(section.getDocumentPath());
                    concatenateFile(doc, f);
                    doc.addParagraph("\n");
                    parts.add(doc);
                    break;
                case TABLE :
                    parts.addAll(generateTable(section, elements));
                    break;
                case FICHE :    
                    final AtomicInteger inc = new AtomicInteger();
                    tempFolder.mkdir();
                    parts.addAll(generateFiches(section, elements, tempFolder, inc));
                    break;
            }
        }
        
        Platform.runLater(()->uiProgressLabel.setText(prefix + "Aggrégation des sections"));
        final TextDocument doc = TextDocument.newTextDocument();
        for(int i=0,n=parts.size();i<n;i++){
            final int I = i;
            Platform.runLater(()->uiProgressLabel.setText(prefix + "Aggrégation des sections "+I+"/"+n));
            ODTUtils.concatenateFile(doc, parts.get(i));
        }
        FileUtilities.deleteDirectory(tempFolder);
        doc.save(file);
        Platform.runLater(()->uiProgressLabel.setText(prefix + "Génération terminée"));
    }
    
    private static List generateFiches(RapportSectionDocument section,
            Map<String,Objet> elements, File tempFolder, AtomicInteger inc) throws Exception{

        final PhotoChoiceDocument photoChoice = section.getPhotoChoice();

        final List parts = new ArrayList();

        //titre
        final TextDocument doc = TextDocument.newTextDocument();
        final String titre = section.getLibelle();
        final Paragraph paragraph = doc.addParagraph(titre);
        paragraph.setFont(new Font("Serial", StyleTypeDefinitions.FontStyle.BOLD, 16));
        parts.add(doc);

        //on recupere le template
        final boolean isDefaultTemplate;
        final DocumentTemplate templateDoc;
        final String templateId = section.getTemplateId();
        if(templateId==null || templateId.isEmpty()){
            isDefaultTemplate = true;
            templateDoc = ODTUtils.getDefaultTemplate();
        }else{
            isDefaultTemplate = false;
            final AbstractSIRSRepository<TemplateOdt> repo = Injector.getSession().getRepositoryForClass(TemplateOdt.class);
            final TemplateOdt template = repo.get(templateId);
            if(template==null){
                throw new Exception("Template manquant pour l'identifiant : "+templateId);
            }
            final DocumentTemplateFactory documentTemplateFactory = new DocumentTemplateFactory();
            templateDoc = documentTemplateFactory.getTemplate(new ByteArrayInputStream(template.getOdt()));
        }


        //on recupere les elements qui correspondent a la requete
        final List<Element> validElements = listValidElements(section, elements);
        if(validElements.isEmpty()){
            return Collections.EMPTY_LIST;
        }

        //on genere une fiche pour chaque objet
        for(Element ele : validElements){
            final File f = new File(tempFolder, section.getId()+"_"+inc.incrementAndGet()+".odt");
            if(isDefaultTemplate){
                generateReport(templateDoc, ODTUtils.toTemplateMap(ele), f);
            }else{
                generateReport(templateDoc, ele, f);
            }

            parts.add(f);

            if(ele instanceof ObjetPhotographiable){
                final ObjetPhotographiable photographiable = (ObjetPhotographiable) ele;
                final List<Photo> photos = photographiable.getPhotos();
                if(!photos.isEmpty()){
                    if(PhotoChoiceDocument.DERNIERE.equals(photoChoice)){
                        Photo last = null;
                        for(Photo p : photos){
                            final String str = p.getChemin();
                            if(str!=null && !str.isEmpty()){
                                if(last==null || last.getDate()==null || (p.getDate()!=null && last.getDate().isBefore(p.getDate()))){
                                    final Path path = SIRS.getDocumentAbsolutePath(p.getChemin());
                                    if(Files.exists(path, LinkOption.NOFOLLOW_LINKS)){
                                        last = p;
                                    }
                                }
                            }
                        }
                        if(last!=null){
                            parts.add(SIRS.getDocumentAbsolutePath(last.getChemin()));
                        }
                    }else if(PhotoChoiceDocument.TOUTE.equals(photoChoice)){
                        for(Photo p : photos){
                            final String str = p.getChemin();
                            if(str!=null && !str.isEmpty()){
                                final Path path = SIRS.getDocumentAbsolutePath(p.getChemin());
                                if(Files.exists(path, LinkOption.NOFOLLOW_LINKS)){
                                    parts.add(SIRS.getDocumentAbsolutePath(str));
                                }
                            }
                        }
                    }
                }
            }
        }

        return parts;
    }
    
    private static List generateTable(RapportSectionDocument section, Map<String,Objet> elements) throws Exception{

        //on recupere les elements qui correspondent a la requete
        final List<Element> validElements = listValidElements(section, elements);
        if(validElements.isEmpty()) {return Collections.EMPTY_LIST;}
        
        final TextDocument doc = TextDocument.newTextDocument();
        
        //liste des champs
        final Class pojoClass = validElements.get(0).getClass();
        final LabelMapper labelMapper = LabelMapper.get(pojoClass);
        final Map<String,Printer> cols = new TreeMap<>(ColumnOrder.createComparator(pojoClass.getSimpleName()));

        try {
            //contruction des colonnes editable
            final HashMap<String, PropertyDescriptor> properties = SIRS.listSimpleProperties(pojoClass);
            for(Map.Entry<String,PropertyDescriptor> entry : properties.entrySet()){
                cols.put(entry.getKey(),entry.getValue().getReadMethod()::invoke);
            }

            // On donne toutes les informations de position.
            if (Positionable.class.isAssignableFrom(pojoClass)) {
                final HashMap<String, PropertyDescriptor> positionable = SIRS.listSimpleProperties(Positionable.class);
                positionable.remove("systemeRepId");
                for(String key : positionable.keySet()){
                    cols.remove(key);
                }

            }

            // On enlève les propriétés inutiles pour l'utilisateur
            for (final String key : COLUMNS_TO_IGNORE) {
                cols.remove(key);
            }
        } catch (IntrospectionException ex) {
            SIRS.LOGGER.log(Level.WARNING, "property columns cannot be created.", ex);
        }

        //titre
        final String titre = section.getLibelle();
        final Paragraph paragraph = doc.addParagraph(titre);
        paragraph.setFont(new Font("Serial", StyleTypeDefinitions.FontStyle.BOLD, 16));

        //table
        final Table table = doc.addTable(validElements.size(), cols.size());
        //header
         int colIndex = 0;
        for(Map.Entry<String,Printer> entry : cols.entrySet()){
            final Cell cell = table.getCellByPosition(colIndex, 0);
            cell.setStringValue(labelMapper.mapPropertyName(entry.getKey()));
            colIndex++;
        }
        //cells
        final Previews previews = Injector.getSession().getPreviews();
        final SirsStringConverter cvt = new SirsStringConverter();
        for(int i=0,n=validElements.size();i<n;i++){
            final Element element = validElements.get(i);
            colIndex = 0;
            for(Map.Entry<String,Printer> entry : cols.entrySet()){
                Object obj = entry.getValue().print(element);
                if(obj!=null){
                    final Cell cell = table.getCellByPosition(colIndex, i+1);
                    if(obj instanceof String){
                        try{
                            obj = cvt.toString(previews.get((String)obj));
                        }catch(DocumentNotFoundException ex){/**pas important*/}
                    }
                    cell.setStringValue(String.valueOf(obj));
                }
                colIndex++;
            }
        }
        return Collections.singletonList(doc);
    }
    
    private static List<Element> listValidElements(RapportSectionDocument section, Map<String,Objet> elements) throws SQLException, DataStoreException{
        final List<Element> validElements = new ArrayList<>();
        final String requeteId = section.getRequeteId();

        if(requeteId==null || requeteId.isEmpty()){
            //pas de filtre
            validElements.addAll(elements.values());
            return validElements;
        }

        //creation de la requete
        final SQLQueryRepository queryRepo = (SQLQueryRepository)Injector.getSession().getRepositoryForClass(SQLQuery.class);
        final SQLQuery sqlQuery = queryRepo.get(requeteId);
        final Query fsquery = org.geotoolkit.data.query.QueryBuilder.language(
                JDBCFeatureStore.CUSTOM_SQL, sqlQuery.getSql(), NamesExt.create("requete"));

        //recupération de la base H2
        final FeatureStore h2Store = (H2FeatureStore) H2Helper.getStore(Injector.getSession().getConnector());
        final FeatureCollection col = h2Store.createSession(false).getFeatureCollection(fsquery);
        final String firstProperty = col.getFeatureType().getDescriptors().iterator().next().getName().tip().toString();

        //on filtre les elements
        try (FeatureIterator iterator = col.iterator()) {
            while(iterator.hasNext()){
                final Feature feature = iterator.next();
                final Object val = feature.getPropertyValue(firstProperty);
                final Objet ele = elements.get(val);
                if(ele!=null) validElements.add(ele);
            }
        }

        return validElements;
    }
    
    private static void concatenateFile(TextDocument doc, Object candidate) throws Exception {

        if(candidate instanceof File || candidate instanceof Path){
            final File file;
            if(candidate instanceof Path){
                file = ((Path)candidate).toFile();
            }else{
                file = (File) candidate;
            }
            final String fileName = file.getName().toLowerCase();
            if(fileName.endsWith(".odt")){
                //append content at the end
                final TextDocument childDoc = TextDocument.loadDocument(file);
                final Paragraph paragraph = doc.addParagraph("");
                doc.insertContentFromDocumentAfter(childDoc, paragraph, true);

            }else if(fileName.endsWith(".pdf")){
                //transform it to image
                try (PDDocument document = PDDocument.loadNonSeq(file, null)) {
                    final List<PDPage> pages = document.getDocumentCatalog().getAllPages();
                    for(int i=0,n=pages.size();i<n;i++) {
                        final PDPage page = pages.get(i);
                        final BufferedImage bim = page.convertToImage(BufferedImage.TYPE_INT_RGB, 300);
                        final File imgFile = File.createTempFile("pdf_"+page+"_", ".png");
                        imgFile.deleteOnExit();
                        try(final FileOutputStream imgStream = new FileOutputStream(imgFile)){
                            ImageIOUtil.writeImage(bim, "png", imgStream, 300);
                            insertImageFullPage(doc, imgFile.toURI());
                        }finally{
                            file.delete();
                        }
                    }
                }

            }else{
                //try image
                try{
                    final BufferedImage img = ImageIO.read(file);
                    insertImage(doc, file.toURI(), img);
                }catch(IOException ex){
                    throw new IOException("Unvalid file "+candidate+". Only PDF, ODT and images are supported.");
                }
            }
        }else if(candidate instanceof TextDocument){
            final TextDocument textDoc = (TextDocument) candidate;
            final Paragraph paragraph = doc.addParagraph("");
            doc.insertContentFromDocumentAfter(textDoc, paragraph, true);
        }
    }
    
    /**
     * Ajouter une image en pleine page a la suite dans le document.
     *
     * @param doc ODT document
     * @param imageUri image path
     */
    private static void insertImageFullPage(final TextDocument doc, final URI imageUri) throws Exception {
        final BufferedImage img = ImageIO.read(imageUri.toURL());
        final MasterPage pdfpageStyle = createMasterPage(doc, img.getWidth()>img.getHeight(), 0);
        doc.addPageBreak(null, pdfpageStyle);

        final OdfContentDom contentDom = doc.getContentDom();
        final OdfDrawFrame drawFrame = contentDom.newOdfElement(OdfDrawFrame.class);
        OdfTextParagraph para = doc.newParagraph();
        para.appendChild(drawFrame);
        drawFrame.setTextAnchorTypeAttribute(TextAnchorTypeAttribute.Value.PAGE.toString());
        final OdfDrawImage image = (OdfDrawImage) drawFrame.newDrawImageElement();

        //style run through
        final OdfStyleBase sb = drawFrame.getOrCreateUnqiueAutomaticStyle();
        sb.setAttributeNS("urn:oasis:names:tc:opendocument:xmlns:style:1.0","style:parent-style-name", "Graphics");
        sb.setProperty(StyleGraphicPropertiesElement.Wrap, "run-through");
        sb.setProperty(StyleGraphicPropertiesElement.NumberWrappedParagraphs, "no-limit");
        sb.setProperty(StyleGraphicPropertiesElement.VerticalPos, "top");
        sb.setProperty(StyleGraphicPropertiesElement.VerticalRel, "page");
        sb.setProperty(StyleGraphicPropertiesElement.HorizontalPos, "center");
        sb.setProperty(StyleGraphicPropertiesElement.HorizontalRel, "page");
        sb.setProperty(StyleGraphicPropertiesElement.Mirror, "none");
        sb.setProperty(StyleGraphicPropertiesElement.Clip, "rect(0mm, 0mm, 0mm, 0mm)");
        sb.setProperty(StyleGraphicPropertiesElement.Luminance, "0%");
        sb.setProperty(StyleGraphicPropertiesElement.Contrast, "0%");
        sb.setProperty(StyleGraphicPropertiesElement.Red, "0%");
        sb.setProperty(StyleGraphicPropertiesElement.Green, "0%");
        sb.setProperty(StyleGraphicPropertiesElement.Blue, "0%");
        sb.setProperty(StyleGraphicPropertiesElement.Gamma, "100%");
        sb.setProperty(StyleGraphicPropertiesElement.ColorInversion, "false");
        sb.setProperty(StyleGraphicPropertiesElement.ImageOpacity, "100%");
        sb.setProperty(StyleGraphicPropertiesElement.ColorMode, "standard");

        image.newImage(imageUri);
    }
    
    /**
     * Create a page configuration.
     * 
     * @param doc
     * @param landscape
     * @param margin ine millimeter
     * @return
     * @throws Exception
     */
    private static MasterPage createMasterPage(Document doc, boolean landscape, int margin) throws Exception{
        final MasterPage masterPage;
        if(landscape){
            masterPage = MasterPage.getOrCreateMasterPage(doc, "pageLandScape");
            masterPage.setPrintOrientation(StyleTypeDefinitions.PrintOrientation.LANDSCAPE);
            masterPage.setPageHeight(210);
            masterPage.setPageWidth(297);
            masterPage.setMargins(margin, margin, margin, margin);
            masterPage.setFootnoteMaxHeight(0);
        }else{
            masterPage = MasterPage.getOrCreateMasterPage(doc, "pagePortrait");
            masterPage.setPrintOrientation(StyleTypeDefinitions.PrintOrientation.PORTRAIT);
            masterPage.setPageWidth(210);
            masterPage.setPageHeight(297);
            masterPage.setMargins(margin, margin, margin, margin);
            masterPage.setFootnoteMaxHeight(0);
        }
        return masterPage;
    }
    
    private static void insertImage(final TextDocument doc, final URI imageUri, final BufferedImage image) throws Exception {
        final OdfContentDom contentDom = doc.getContentDom();
        final TextPElement lastPara = doc.newParagraph("");
        final OdfDrawFrame drawFrame = contentDom.newOdfElement(OdfDrawFrame.class);
        lastPara.appendChild(drawFrame);
        final OdfDrawImage drawImage = (OdfDrawImage) drawFrame.newDrawImageElement();
        drawImage.newImage(imageUri);

        //style none
        final OdfStyleBase sb = drawFrame.getOrCreateUnqiueAutomaticStyle();
        sb.setAttributeNS("urn:oasis:names:tc:opendocument:xmlns:style:1.0","style:parent-style-name", "Graphics");
        sb.setProperty(StyleGraphicPropertiesElement.Wrap, "none");
        sb.setProperty(StyleGraphicPropertiesElement.HorizontalPos, "center");
        sb.setProperty(StyleGraphicPropertiesElement.HorizontalRel, "paragraph");
        sb.setProperty(StyleGraphicPropertiesElement.Mirror, "none");
        sb.setProperty(StyleGraphicPropertiesElement.Clip, "rect(0mm, 0mm, 0mm, 0mm)");
        sb.setProperty(StyleGraphicPropertiesElement.VerticalPos, "top");
        sb.setProperty(StyleGraphicPropertiesElement.VerticalRel, "baseline");
        sb.setProperty(StyleGraphicPropertiesElement.Luminance, "0%");
        sb.setProperty(StyleGraphicPropertiesElement.Contrast, "0%");
        sb.setProperty(StyleGraphicPropertiesElement.Red, "0%");
        sb.setProperty(StyleGraphicPropertiesElement.Green, "0%");
        sb.setProperty(StyleGraphicPropertiesElement.Blue, "0%");
        sb.setProperty(StyleGraphicPropertiesElement.Gamma, "100%");
        sb.setProperty(StyleGraphicPropertiesElement.ColorInversion, "false");
        sb.setProperty(StyleGraphicPropertiesElement.ImageOpacity, "100%");
        sb.setProperty(StyleGraphicPropertiesElement.ColorMode, "standard");


        drawFrame.setTextAnchorTypeAttribute(TextAnchorTypeAttribute.Value.PARAGRAPH.toString());
        drawFrame.setSvgWidthAttribute(IMAGE_WIDTH+"mm");
        //Calculate relative height
        if (image != null) {
            drawFrame.setSvgHeightAttribute((int)(((float)image.getHeight() / (float)image.getWidth()) * IMAGE_WIDTH)+"mm");
        }
    }
    
    private static interface Printer{

        public Object print(Object candidate) throws Exception;
    }
    
    private static synchronized DocumentTemplate getDefaultTemplate() throws IOException{
        if(DEFAULT_TEMPLATE!=null) return DEFAULT_TEMPLATE;
        final DocumentTemplateFactory documentTemplateFactory = new DocumentTemplateFactory();
        DEFAULT_TEMPLATE = documentTemplateFactory.getTemplate(ODTUtils.class.getResourceAsStream("/fr/sirs/plugin/document/defaultTemplate.odt"));
        return DEFAULT_TEMPLATE;
    }
    
    private static Map toTemplateMap(Object candidate) throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        final Class pojoClass = candidate.getClass();
        final HashMap<String, PropertyDescriptor> props = SIRS.listSimpleProperties(pojoClass);
        final LabelMapper labelMapper = LabelMapper.get(pojoClass);
        final SirsStringConverter cvt = new SirsStringConverter();
        final Previews previews = Injector.getSession().getPreviews();

        final List<Map<String,Object>> properties = new ArrayList<>();
        for(Entry<String,PropertyDescriptor> entry : props.entrySet()){
            if(entry.getValue().getReadMethod()!=null){
                Object val = entry.getValue().getReadMethod().invoke(candidate);
                final HashMap map = new HashMap(2);
                map.put("key", labelMapper.mapPropertyName(entry.getKey()));
                if(val instanceof String){
                    try{
                        val = cvt.toString(previews.get((String)val));
                    }catch(DocumentNotFoundException ex){/**pas important*/}
                }
                map.put("value", val==null ? "":val.toString() );
                properties.add(map);
            }
        }

        final Map objectMap = new HashMap();
        objectMap.put("properties", properties);
        objectMap.put("class", candidate.getClass().getSimpleName());

        return objectMap;
    }
    
    /**
     * Remplissage d'un modèle de rapport pour un objet donné.
     *
     * Template au format JODReport : http://jodreports.sourceforge.net/?q=node/23
     *
     * @param template template JOOReport
     * @param candidate bean servant au remplissage
     * @param outputFile fichier ODT de sortie
     * @throws IOException
     * @throws TemplateModelException
     * @throws DocumentTemplateException
     */
    private static void generateReport(DocumentTemplate template, Object candidate, File outputFile)
            throws IOException, TemplateModelException, DocumentTemplateException, IntrospectionException,
            InvocationTargetException, IllegalAccessException {
        if(!(candidate instanceof Map || candidate instanceof TemplateModel)){
            final Class pojoClass = candidate.getClass();
            final HashMap<String, PropertyDescriptor> props = SIRS.listSimpleProperties(pojoClass);
            final SirsStringConverter cvt = new SirsStringConverter();
            final Previews previews = Injector.getSession().getPreviews();

            final Map<String,Object> properties = new HashMap<>();
            for(Map.Entry<String,PropertyDescriptor> entry : props.entrySet()){
                if(entry.getValue().getReadMethod()!=null){
                    Object val = entry.getValue().getReadMethod().invoke(candidate);
                    if(val instanceof String){
                        try{
                            val = cvt.toString(previews.get((String)val));
                        }catch(DocumentNotFoundException ex){/**pas important*/}
                    }
                    properties.put(entry.getKey(), val == null ? "" : val.toString());
                }
            }
            candidate = properties;
            //candidate = BeansWrapper.getDefaultInstance().wrap(candidate);
        }
        template.createDocument(candidate, new FileOutputStream(outputFile));
    }
}
