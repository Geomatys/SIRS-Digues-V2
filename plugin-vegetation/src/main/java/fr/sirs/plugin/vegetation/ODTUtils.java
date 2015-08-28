
package fr.sirs.plugin.vegetation;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.util.SirsStringConverter;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.awt.image.BufferedImage;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import net.sf.jooreports.templates.DocumentTemplate;
import net.sf.jooreports.templates.DocumentTemplateException;
import net.sf.jooreports.templates.DocumentTemplateFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.ImageIOUtil;
import org.apache.sis.util.Static;
import org.ektorp.DocumentNotFoundException;
import org.odftoolkit.odfdom.dom.OdfContentDom;
import org.odftoolkit.odfdom.dom.attribute.text.TextAnchorTypeAttribute;
import org.odftoolkit.odfdom.dom.element.OdfStyleBase;
import org.odftoolkit.odfdom.dom.element.style.StyleGraphicPropertiesElement;
import org.odftoolkit.odfdom.dom.element.text.TextPElement;
import org.odftoolkit.odfdom.incubator.doc.draw.OdfDrawFrame;
import org.odftoolkit.odfdom.incubator.doc.draw.OdfDrawImage;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextParagraph;
import org.odftoolkit.simple.Document;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.style.MasterPage;
import org.odftoolkit.simple.style.StyleTypeDefinitions;
import org.odftoolkit.simple.text.Paragraph;

/**
 * Classe utilitaire d'écriture de fichier ODT.
 * 
 * @author Johann Sorel (Geomatys)
 */
public final class ODTUtils extends Static{

    private static final int IMAGE_WIDTH = 190;
    private static DocumentTemplate DEFAULT_TEMPLATE;

    public static synchronized DocumentTemplate getDefaultTemplate() throws IOException{
        if(DEFAULT_TEMPLATE!=null) return DEFAULT_TEMPLATE;
        final DocumentTemplateFactory documentTemplateFactory = new DocumentTemplateFactory();
        DEFAULT_TEMPLATE = documentTemplateFactory.getTemplate(ODTUtils.class.getResourceAsStream("/fr/sirs/plugin/reglementaire/defaultTemplate.odt"));
        return DEFAULT_TEMPLATE;
    }

    public static Map toTemplateMap(Object candidate) throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
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
     * @param templateFile fichier ODT template
     * @param candidate bean servant au remplissage
     * @param outputFile fichier ODT de sortie
     * @throws IOException
     * @throws TemplateModelException
     * @throws DocumentTemplateException
     */
    public static void generateReport(File templateFile, Object candidate, File outputFile)
            throws IOException, TemplateModelException, DocumentTemplateException, IllegalAccessException,
            IntrospectionException, InvocationTargetException {
        final DocumentTemplateFactory documentTemplateFactory = new DocumentTemplateFactory();
        final DocumentTemplate template = documentTemplateFactory.getTemplate(templateFile);
        generateReport(template, candidate, outputFile);
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
    public static void generateReport(DocumentTemplate template, Object candidate, File outputFile)
            throws IOException, TemplateModelException, DocumentTemplateException, IntrospectionException,
            InvocationTargetException, IllegalAccessException {
        if(!(candidate instanceof Map || candidate instanceof TemplateModel)){
            final Class pojoClass = candidate.getClass();
            final HashMap<String, PropertyDescriptor> props = SIRS.listSimpleProperties(pojoClass);
            final SirsStringConverter cvt = new SirsStringConverter();
            final Previews previews = Injector.getSession().getPreviews();

            final Map<String,Object> properties = new HashMap<>();
            for(Entry<String,PropertyDescriptor> entry : props.entrySet()){
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

    /**
     * Aggregation dans un seul fichier ODT de tous les fichiers fournis.
     * Fichier supportés :
     * - images
     * - odt
     * - pdf
     *
     * Supporte aussi les objets de type :
     * - File
     * - TextDocument
     * 
     * @param outputFile fichier ODT de sortie
     * @param candidates
     */
    public static void concatenateFiles(File outputFile, Object ... candidates) throws Exception {
        final TextDocument doc = TextDocument.newTextDocument();

        for(Object candidate : candidates){
            concatenateFile(doc, candidate);
        }

        doc.save(outputFile);
    }

    public static void concatenateFile(TextDocument doc, Object candidate) throws Exception {

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
     * Ajouter une image a la suite dans le document.
     *
     * @param doc ODT document
     * @param imageUri
     */
    public static void insertImage(final TextDocument doc, final URI imageUri) throws Exception {
        insertImage(doc, imageUri, null);
    }

    public static void insertImage(final TextDocument doc, final URI imageUri, BufferedImage image) throws Exception {
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
        drawFrame.setSvgHeightAttribute((int)(((float)image.getHeight() / (float)image.getWidth()) * IMAGE_WIDTH)+"mm");


    }

    /**
     * Ajouter une image en pleine page a la suite dans le document.
     *
     * @param doc ODT document
     * @param imageUri image path
     */
    public static void insertImageFullPage(final TextDocument doc, final URI imageUri) throws Exception {
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
    public static MasterPage createMasterPage(Document doc, boolean landscape, int margin) throws Exception{
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

}