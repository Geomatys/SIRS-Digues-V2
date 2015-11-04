package fr.sirs.core.util.odt;

import fr.sirs.core.CouchDBTestCase;
import fr.sirs.core.model.Crete;
import fr.sirs.util.odt.ODTUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javax.imageio.ImageIO;
import org.apache.sis.test.DependsOnMethod;
import org.junit.Assert;
import org.junit.Test;
import org.odftoolkit.odfdom.dom.element.text.TextUserFieldDeclElement;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.common.field.VariableField;
import org.odftoolkit.simple.style.MasterPage;
import org.odftoolkit.simple.style.StyleTypeDefinitions;
import org.odftoolkit.simple.text.Paragraph;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ODTUtilsTest extends CouchDBTestCase {

    @Test
    public void testTemplateCreation() throws Exception {
        final HashMap<String, String> properties = new HashMap<>(3);
        properties.put("var1", "First variable");
        properties.put("var2", "Second variable");
        properties.put("var3", "Third variable");
        final TextDocument result = ODTUtils.newSimplePropertyModel("Document test", properties);
        Assert.assertNotNull("Generated template", result);

        // Now, check that variables have been set correctly
        Map<String, VariableField> vars = ODTUtils.findAllVariables(result, null);
        Assert.assertNotNull("Variable map", vars);

        for (final String key : properties.keySet()) {
            Assert.assertNotNull("Variable cannot be found :"+key, vars.get(key));
        }
    }

    @Test
    @DependsOnMethod(value="testTemplateCreation")
    public void testSimpleReport() throws Exception {

        final Crete data = new Crete();
        data.setEpaisseur(10);
        data.setCommentaire("BOUH !");
        data.setDate_debut(LocalDate.now());

        final HashMap<String, String> properties = new HashMap<>(3);
        properties.put("epaisseur", "Epaisseur");
        properties.put("commentaire", "Commentaire");
        properties.put("date_debut", "Date de début");

        final TextDocument template = ODTUtils.newSimplePropertyModel("Crete", properties);
        TextDocument report = ODTUtils.reportFromTemplate(template, data);

        Assert.assertNotNull("Generated report", report);
        // Now, check that variables have been set correctly
        Map<String, VariableField> vars = ODTUtils.findAllVariables(report, null);
        Assert.assertNotNull("Variable map", vars);

        Field valueField = VariableField.class.getDeclaredField("userVariableElement");
        valueField.setAccessible(true);
        VariableField var = vars.get("epaisseur");
        Assert.assertNotNull("Variable cannot be found : epaisseur", var);
        Assert.assertEquals("Variable epaisseur", String.valueOf(data.getEpaisseur()), ((TextUserFieldDeclElement)valueField.get(var)).getOfficeStringValueAttribute());

        var = vars.get("commentaire");
        Assert.assertNotNull("Variable cannot be found : commentaire", var);
        Assert.assertEquals("Variable commentaire", String.valueOf(data.getCommentaire()), ((TextUserFieldDeclElement)valueField.get(var)).getOfficeStringValueAttribute());

        var = vars.get("date_debut");
        Assert.assertNotNull("Variable cannot be found : date_debut", var);
        Assert.assertEquals("Variable date_debut", String.valueOf(data.getDate_debut().toString()), ((TextUserFieldDeclElement)valueField.get(var)).getOfficeStringValueAttribute());
    }

    @Test
    public void testGetOrCreateMasterPage() throws Exception {
        final TextDocument doc = TextDocument.newTextDocument();
        final Insets margin = new Insets(5, 4, 3, 2);

        MasterPage mp = ODTUtils.getOrCreateOrientationMasterPage(doc, StyleTypeDefinitions.PrintOrientation.LANDSCAPE, margin);
        Assert.assertEquals("Page orientation", StyleTypeDefinitions.PrintOrientation.LANDSCAPE.name().toLowerCase(), mp.getPrintOrientation().toLowerCase());
        Assert.assertEquals("Page margins", margin.getTop(), mp.getMarginTop(), 0.01);
        Assert.assertEquals("Page margins", margin.getRight(), mp.getMarginRight(), 0.01);
        Assert.assertEquals("Page margins", margin.getBottom(), mp.getMarginBottom(), 0.01);
        Assert.assertEquals("Page margins", margin.getLeft(), mp.getMarginLeft(), 0.01);

        final MasterPage copy = ODTUtils.getOrCreateOrientationMasterPage(doc, StyleTypeDefinitions.PrintOrientation.LANDSCAPE, margin);
        Assert.assertEquals("Master page copy", mp.getPrintOrientation(), copy.getPrintOrientation());
        Assert.assertEquals("Master page copy", mp.getMarginTop(), copy.getMarginTop(), 0.0001);
        Assert.assertEquals("Master page copy", mp.getMarginRight(), copy.getMarginRight(), 0.0001);
        Assert.assertEquals("Master page copy", mp.getMarginBottom(), copy.getMarginBottom(), 0.0001);
        Assert.assertEquals("Master page copy", mp.getMarginLeft(), copy.getMarginLeft(), 0.0001);
    }


    public static void concatenateFile(TextDocument holder, Object candidate) throws Exception {
        if (candidate instanceof TextDocument) {
            holder.insertContentFromDocumentAfter((TextDocument) candidate, holder.addParagraph(""), true);
        }

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
}
