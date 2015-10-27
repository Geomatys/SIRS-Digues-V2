package fr.sirs.util.odt;

import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.Element;
import fr.sirs.util.property.Reference;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.common.field.VariableField;
import org.odftoolkit.simple.common.navigation.Selection;
import org.odftoolkit.simple.common.navigation.TextNavigation;
import org.odftoolkit.simple.common.navigation.TextSelection;

/**
 *
 * @author Alexis Manin (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public class ODTUtils {

    private static final String VARIABLE_START = "${";
    private static final String VARIABLE_END = "}";
    private static final Pattern VARIABLE_BUILDER = Pattern.compile("^"+Pattern.quote(VARIABLE_START)+"(.*)"+VARIABLE_END+"$");

    private static final String NOTE_MODELE_FICHE = "Note : Ci-dessous se trouve"
            + " la liste des champs utilisés par SIRS-Digues lors de la création"
            + " d'une fiche. Vous pouvez compléter ce modèle (Ajout de contenu,"
            + " mise en forme) et déplacer les variables (les textes de la forme"
            + " ${propriété}) où vous voulez dans le document. Ils seront"
            + " automatiquement remplacés à la génération du rapport.";

    /**
     * Generate a new template which will put "variables" into it to be easily
     * replaced when creating a report.
     *
     * Note : For the moment, we do not use real {@link VariableField}, because
     * iterating through does not look simple. We just use text with special formatting.
     *
     * @param title Title for the document to create.
     * @param properties A map whose keys are titles for properties, and value are
     * name of the properties to put in the template.
     * @return
     */
    public static TextDocument newSimplePropertyModel(final String title, final Map<String, String> properties) throws Exception {
        final TextDocument result = TextDocument.newTextDocument();
        result.addParagraph(title).applyHeading();
        result.addParagraph(NOTE_MODELE_FICHE);
        for (final Map.Entry<String, String> entry : properties.entrySet()) {
            result.addParagraph(entry.getKey() + " : " + VARIABLE_START+entry.getValue()+VARIABLE_START);
        }

        return result;
    }

    /**
     * Fill given template with data originating from candidate object.
     * @param templateData Source template to fill.
     * @param candidate The object to get data from to fill given template. Most
     * likely an {@link Element}, but can also be a {@link Map} whose keys are
     * names of the variables to replace, and values the data to put in them.
     * @return A Document formatted as input template, but filled with given data.
     */
    public static TextDocument reportFromTemplate(final InputStream templateData, final Object candidate) throws Exception {
        final TextDocument document = TextDocument.loadDocument(templateData);
        final TextNavigation search = new TextNavigation(VARIABLE_START+"*"+VARIABLE_END, document);

        final Map properties;
        if (candidate instanceof Map) {
            properties = (Map) candidate;
        } else {
            // input candidate is of unknown type. We iterate through its property to extract all mappable attributes.
            final BeanInfo info = Introspector.getBeanInfo(candidate.getClass());
            properties = new HashMap<>();
            final Previews previews = InjectorCore.getBean(SessionCore.class).getPreviews();
            for (final PropertyDescriptor desc : info.getPropertyDescriptors()) {
                final Method readMethod = desc.getReadMethod();
                if (readMethod == null) {
                    continue; // Non readble attribute, skip.
                } else {
                    readMethod.setAccessible(true);
                }

                // Check if we've got a real data or a link.
                final Reference ref = readMethod.getAnnotation(Reference.class);
                final Class<?> refClass;
                if (ref != null) {
                    refClass = ref.ref();
                } else {
                    refClass = null;
                }

                Object value = readMethod.invoke(candidate);
                if (refClass != null && (value instanceof String)) {
                    value = previews.get((String) value).getLibelle();
                }
                properties.put(desc.getName(), value);
            }
        }

        while (search.hasNext()) {
            final Selection selection = search.nextSelection();
            if (selection instanceof TextSelection) {
                final TextSelection tSelection = (TextSelection) selection;
                final Matcher matcher = VARIABLE_BUILDER.matcher(tSelection.getText());
                if (matcher.matches()) {
                    final String varName = matcher.group(1);
                    final Object value = properties.get(varName);
                    if (value != null) {
                        tSelection.replaceWith(value.toString()); // TODO : better string conversion ?
                    }
                }
            }
        }

        return document;
    }
}
