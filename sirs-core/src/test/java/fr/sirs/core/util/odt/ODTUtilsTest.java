package fr.sirs.core.util.odt;

import fr.sirs.core.model.Crete;
import fr.sirs.util.odt.ODTUtils;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Insets;
import org.apache.sis.test.DependsOnMethod;
import org.junit.Assert;
import org.junit.Test;
import org.odftoolkit.odfdom.dom.element.text.TextUserFieldDeclElement;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.common.field.VariableField;
import org.odftoolkit.simple.style.MasterPage;
import org.odftoolkit.simple.style.StyleTypeDefinitions;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ODTUtilsTest {

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

        final TextDocument report = ODTUtils.newSimplePropertyModel("Crete", properties);
        ODTUtils.fillTemplate(report, data);

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
}
