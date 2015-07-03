
package fr.sirs.plugin.reglementaire;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import javax.imageio.ImageIO;
import net.sf.jooreports.templates.DocumentTemplate;
import net.sf.jooreports.templates.DocumentTemplateException;
import net.sf.jooreports.templates.DocumentTemplateFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.ImageIOUtil;
import org.apache.sis.util.Static;
import org.odftoolkit.odfdom.dom.OdfContentDom;
import org.odftoolkit.odfdom.dom.attribute.text.TextAnchorTypeAttribute;
import org.odftoolkit.odfdom.dom.element.OdfStyleBase;
import org.odftoolkit.odfdom.dom.element.style.StyleGraphicPropertiesElement;
import org.odftoolkit.odfdom.dom.element.text.TextPElement;
import org.odftoolkit.odfdom.incubator.doc.draw.OdfDrawFrame;
import org.odftoolkit.odfdom.incubator.doc.draw.OdfDrawImage;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextParagraph;
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
            throws IOException, TemplateModelException, DocumentTemplateException{
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
            throws IOException, TemplateModelException, DocumentTemplateException{
        final TemplateModel wrap = BeansWrapper.getDefaultInstance().wrap(candidate);
        template.createDocument(wrap, new FileOutputStream(outputFile));
    }

    /**
     * Aggregation dans un seul fichier ODT de tous les fichiers fournis.
     * Fichier supportés :
     * - images
     * - odt
     * - pdf
     * 
     * @param outputFile fichier ODT de sortie
     * @param files 
     */
    public static void concatenateFiles(File outputFile, File ... files) throws Exception {
        final TextDocument doc = TextDocument.newTextDocument();

        for(File file : files){
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
                    insertImage(doc, file.toURI());
                }catch(IOException ex){
                    throw new IOException("Unvalid file "+file+". Only PDF, ODT and images are supported.");
                }
            }
        }

        doc.save(outputFile);
    }

    /**
     * Ajouter une image a la suite dans le document.
     *
     * @param doc ODT document
     * @param imageUri
     */
    public static void insertImage(final TextDocument doc, final URI imageUri) throws Exception {
        final OdfContentDom contentDom = doc.getContentDom();
        final OdfDrawFrame drawFrame = contentDom.newOdfElement(OdfDrawFrame.class);
        final TextPElement lastPara = doc.newParagraph();
        lastPara.appendChild(drawFrame);
        drawFrame.setTextAnchorTypeAttribute(TextAnchorTypeAttribute.Value.PARAGRAPH.toString());
        final OdfDrawImage image = (OdfDrawImage) drawFrame.newDrawImageElement();
        image.newImage(imageUri);
    }

    /**
     * Ajouter une image en pleine page a la suite dans le document.
     *
     * @param doc ODT document
     * @param imageUri image path
     */
    public static void insertImageFullPage(final TextDocument doc, final URI imageUri) throws Exception {
        final BufferedImage img = ImageIO.read(imageUri.toURL());
        final MasterPage pdfpageStyle;
        if(img.getWidth()>img.getHeight()){
            pdfpageStyle = MasterPage.getOrCreateMasterPage(doc, "pdfPageLandScape");
            pdfpageStyle.setPrintOrientation(StyleTypeDefinitions.PrintOrientation.LANDSCAPE);
            pdfpageStyle.setPageHeight(210);
            pdfpageStyle.setPageWidth(297);
            pdfpageStyle.setMargins(0, 0, 0, 0);
            pdfpageStyle.setFootnoteMaxHeight(0);
        }else{
            pdfpageStyle = MasterPage.getOrCreateMasterPage(doc, "pdfPagePortrait");
            pdfpageStyle.setPrintOrientation(StyleTypeDefinitions.PrintOrientation.PORTRAIT);
            pdfpageStyle.setPageWidth(210);
            pdfpageStyle.setPageHeight(297);
            pdfpageStyle.setMargins(0, 0, 0, 0);
            pdfpageStyle.setFootnoteMaxHeight(0);
        }
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


}
