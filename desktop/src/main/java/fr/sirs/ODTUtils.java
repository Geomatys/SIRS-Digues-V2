
package fr.sirs;

import java.awt.image.BufferedImage;
import java.net.URI;
import javafx.geometry.Insets;
import javax.imageio.ImageIO;
import org.apache.sis.util.Static;
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

/**
 * Classe utilitaire d'écriture de fichier ODT.
 *
 * @author Johann Sorel (Geomatys)
 */
public final class ODTUtils extends Static{

    private static final int IMAGE_WIDTH = 190;





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
        insertImage(doc, imageUri, image.getWidth(), image.getHeight());
    }

    public static void insertImage(final TextDocument doc, final URI imageUri, int width, int height) throws Exception {
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
        drawFrame.setSvgHeightAttribute((int)(((float)height / (float)width) * IMAGE_WIDTH)+"mm");


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
     * Get master page with same orientation / margin properties as inputs, or
     * create a new one if we cannot find any.
     *
     * TODO : Check footnote settings
     *
     * @param doc Document to search for existing master pages.
     * @param orientation Orientation wanted for the returned page configuration. If null, portrait orientation is used.
     * @param margin Margins to set to the master page. If null, default style margins are used.
     * @return Found master page, or a new one.
     * @throws Exception If we cannot read given document.
     */
    public static MasterPage getOrCreateOrientationMasterPage(Document doc, StyleTypeDefinitions.PrintOrientation orientation, Insets margin) throws Exception {
        if (orientation == null) {
            orientation = StyleTypeDefinitions.PrintOrientation.PORTRAIT;
        }

        final String masterName = orientation.name() + (margin == null? "" : " " + margin.toString());

        final MasterPage masterPage = MasterPage.getOrCreateMasterPage(doc, masterName);
        masterPage.setPrintOrientation(orientation);
        switch (orientation) {
            case LANDSCAPE:
                masterPage.setPageHeight(210);
                masterPage.setPageWidth(297);
                break;
            case PORTRAIT:
                masterPage.setPageWidth(210);
                masterPage.setPageHeight(297);
        }
        if (margin != null) {
            masterPage.setMargins(margin.getTop(), margin.getBottom(), margin.getLeft(), margin.getRight());
        }
        masterPage.setFootnoteMaxHeight(0);
        return masterPage;
    }
}
