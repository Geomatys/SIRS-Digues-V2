package fr.sirs.util.odt;

import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCore;
import fr.sirs.core.SirsCoreRuntimeException;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.Element;
import fr.sirs.util.property.Reference;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.BadSecurityHandlerException;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.gui.javafx.util.TaskManager;
import org.geotoolkit.image.io.XImageIO;
import org.odftoolkit.odfdom.dom.element.text.TextUserFieldDeclElement;
import org.odftoolkit.odfdom.dom.element.text.TextUserFieldDeclsElement;
import org.odftoolkit.odfdom.dom.element.text.TextVariableDeclElement;
import org.odftoolkit.odfdom.dom.element.text.TextVariableDeclsElement;
import org.odftoolkit.odfdom.pkg.OdfElement;
import org.odftoolkit.simple.Document;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.common.field.AbstractVariableContainer;
import org.odftoolkit.simple.common.field.Field.FieldType;
import org.odftoolkit.simple.common.field.Fields;
import org.odftoolkit.simple.common.field.VariableField;
import org.odftoolkit.simple.draw.Image;
import org.odftoolkit.simple.style.MasterPage;
import org.odftoolkit.simple.style.StyleTypeDefinitions;

/**
 * Utility methods used to create ODT templates, or fill ODT templates.
 *
 * Note : For now, we do not use real ODT varaibles (see {@link VariableField})
 * in our templates, because iterating through does not look simple. We just use
 * text with special formatting.
 *
 * @author Alexis Manin (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public class ODTUtils {

    /**
     * Width of an ODT documentt page in portrait mode. Unit is millimeter.
     */
    private static final int PORTRAIT_WIDTH = 210;

    /**
     * Height of an ODT documentt page in portrait mode. Unit is millimeter.
     */
    private static final int PORTRAIT_HEIGHT = 297;

    public static final String CLASS_KEY = "The.Sirs.Class";

    private static final String ASK_PASSWORD = "Le document suivant est protégé par mot de passe. Veuillez insérer le mot de passe pour continuer.";

    private static final String ELEMENT_MODEL_NOTE = "Note : Ci-dessous se trouve"
            + " la liste des champs utilisés par SIRS-Digues lors de la création"
            + " d'une fiche. Vous pouvez compléter ce modèle (Ajout de contenu,"
            + " mise en forme) et déplacer / copier les variables (les textes"
            + " surlignés de gris) où vous voulez dans le document. Elles seront"
            + " automatiquement remplacés à la génération du rapport.";

    private static Field USER_FIELD;
    private static Field SIMPLE_FIELD;

    /**
     * Generate a new template which will put "variables" into it to be easily
     * replaced when creating a report.
     *
     * Note : For the moment, we do not use real {@link VariableField}, because
     * iterating through does not look simple. We just use text with special
     * formatting.
     *
     * @param title Title for the document to create.
     * @param properties A map whose keys are properties to put in template, and values are titles for them.
     * @return A new template document with prepared variables.
     */
    public static TextDocument newSimplePropertyModel(final String title, final Map<String, String> properties) throws Exception {
        final TextDocument result = TextDocument.newTextDocument();
        result.addParagraph(title).applyHeading();
        result.addParagraph(ELEMENT_MODEL_NOTE);
        for (final Map.Entry<String, String> entry : properties.entrySet()) {
            appendUserVariable(result, entry.getKey(), entry.getValue(), entry.getValue());
        }

        return result;
    }

    /**
     * Save given class reference in input document, setting it as the class to
     * use when generating a report.
     * @param input The document to modify.
     * @param targetClass Class to put, or null to remove information from document.
     */
    public static void setTargetClass(final TextDocument input, final Class targetClass) {
        ArgumentChecks.ensureNonNull("Input document", input);
        if (targetClass == null) {
            removeVariable(input.getVariableFieldByName(CLASS_KEY));
        } else {
            Fields.createUserVariableField(input, CLASS_KEY, targetClass.getCanonicalName());
        }
    }

    /**
     * Analyze input document to find which type of object it is planned for.
     * @param source Source document to analyze.
     * @return The class which must be used for report creation, or null if we
     * cannot find information in given document.
     * @throws ReflectiveOperationException If we fail analyzing document.
     */
    public static Class getTargetClass(final TextDocument source) throws ReflectiveOperationException {
        final VariableField var = source.getVariableFieldByName(CLASS_KEY);
        if (var != null && FieldType.USER_VARIABLE_FIELD.equals(var.getFieldType())) {
            Object value = getVariableValue(var);
            if (value instanceof String) {
                return Thread.currentThread().getContextClassLoader().loadClass((String) value);
            }
        }

        return null;
    }

    /**
     * Replace all variables defined in input document template with the one given
     * in parameter. For variables in document also present in given property mapping,
     * they're left as is. New properties to be put are added in paragraphs at the end
     * of the document.
     *
     * @param source Document template to modify.
     * @param properties Properties to set in given document. Keys are property names,
     * and values are associated title. If null, all user variables will be deleted
     * from input document.
     */
    public static void setVariables(final TextDocument source, final Map<String, String> properties) {
        Map<String, VariableField> vars = findAllVariables(source, VariableField.VariableType.USER);

        if (properties != null) {
            for (final Map.Entry<String, String> entry : properties.entrySet()) {
                if (vars.remove(entry.getKey()) == null) {
                    appendUserVariable(source, entry.getKey(), entry.getValue(), entry.getValue());
                }
            }
        }

        for (final VariableField var : vars.values()) {
            removeVariable(var);
        }
    }

    /**
     * Remove given variable from its holding document.
     * @param var the variable field to get rid of.
     * @return true if we succeeded, false otherwise.
     */
    public static boolean removeVariable(final VariableField var) {
        if (var == null)
            return false;
        final OdfElement varElement = var.getOdfElement();
        if (varElement == null)
            return false;
        return varElement.getParentNode().removeChild(varElement) != null;
    }

    /**
     * Fill given template with data originating from candidate object.
     *
     * @param template Source template to fill.
     * @param candidate The object to get data from to fill given template.
     * 
     * @throws java.beans.IntrospectionException If input candidate cannot be analyzed.
     * @throws java.lang.ReflectiveOperationException If we fail reading candidate properties.
     */
    public static void fillTemplate(final TextDocument template, final Element candidate) throws IntrospectionException, ReflectiveOperationException {
        //final TextNavigation search = new TextNavigation(VARIABLE_SEARCH, document);

        // We iterate through input properties to extract all mappable attributes.
        final PropertyDescriptor[] descriptors = Introspector.getBeanInfo(candidate.getClass()).getPropertyDescriptors();
        final HashMap<String, Object> properties = new HashMap<>(descriptors.length);
        final Previews previews = InjectorCore.getBean(SessionCore.class).getPreviews();
        for (final PropertyDescriptor desc : descriptors) {
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

        Object value;
        for (final Map.Entry<String, Object> entry : properties.entrySet()) {
            value = entry.getValue();
            VariableField var = template.getVariableFieldByName(entry.getKey());
            if (var != null) {
                var.updateField(value == null ? "N/A" : value.toString(), null);
            } else {
                SirsCore.LOGGER.fine("No variable found for name "+entry.getKey());
            }
        }
    }

    /**
     * Find variable with given name into input document, and update its value with specified one.
     * If we cannot find a matching variable into given document, this method just returns.
     *
     * @param doc The document to search into.
     * @param varName Name of the variable to find.
     * @param newValue Value to put into found variable.
     */
    public static void findAndReplaceVariable(final TextDocument doc, final String varName, final Object newValue) {
        try {
            VariableField var = doc.getVariableFieldByName(varName);
            if (var != null) {
                var.updateField(newValue == null ? "N/A" : newValue.toString(), null);
            }
        } catch (IllegalArgumentException e) {
            // No variable found for given name.
        }
    }

    /**
     * Add a new variable in given document, and put a new paragraph containing
     * it at the end of the document.
     *
     * @param doc The document to add variable into.
     * @param varName Name of the variable to create. Should not be null.
     * @param value Default value to set to the variable. Null accepted.
     * @param text Text to put in the paragraph to create. If null, no paragraph
     * will be created, so variabl will not be displayed in document body.
     * @return The created variable.
     */
    public static VariableField appendUserVariable(final TextDocument doc, final String varName, final String value, final String text) {
        final VariableField field = Fields.createUserVariableField(doc, varName, value);
        if (text != null) {
            field.displayField(doc.addParagraph(text + " : ").getOdfElement());
        }
        return field;
    }

    /**
     * Search in input document for all declared variables of a given type.
     *
     * For algorithm, see {@link AbstractVariableContainer#getVariableFieldByName(java.lang.String) }
     *
     * @param source Document to search into.
     * @param type Type of variable to retrieve. If null, all type of variables
     * will be returned.
     * @return A map of all found variables. Keys are variable names, values are concrete variables. Never null, but can be empty.
     */
    public static Map<String, VariableField> findAllVariables(final TextDocument source, final VariableField.VariableType type) {
        final OdfElement variableContainer = source.getVariableContainerElement();
        final HashMap<String, VariableField> result = new HashMap<>();

        VariableField tmpField;
        // First, find all user variable methods.
        if (type == null || VariableField.VariableType.USER.equals(type)) {
            TextUserFieldDeclsElement userVariableElements = OdfElement.findFirstChildNode(TextUserFieldDeclsElement.class, variableContainer);
            if (userVariableElements != null) {
                TextUserFieldDeclElement userVariable = OdfElement.findFirstChildNode(TextUserFieldDeclElement.class, userVariableElements);
                Object value;
                while (userVariable != null) {
                    // really crappy...
                    value = getValue(userVariable);

                    // even crappier ...
                    tmpField = Fields.createUserVariableField(source, userVariable.getTextNameAttribute(), value.toString());
                    result.put(tmpField.getVariableName(), tmpField);

                    userVariable = OdfElement.findNextChildNode(TextUserFieldDeclElement.class, userVariable);
                }
            }
        }

        // then look for simple variables.
        if (type == null || VariableField.VariableType.SIMPLE.equals(type)) {
            TextVariableDeclsElement userVariableElements = OdfElement.findFirstChildNode(TextVariableDeclsElement.class, variableContainer);
            if (userVariableElements != null) {
                TextVariableDeclElement variable = OdfElement.findFirstChildNode(TextVariableDeclElement.class, userVariableElements);
                while (variable != null) {
                    tmpField = Fields.createSimpleVariableField(source, variable.getTextNameAttribute());
                    result.put(tmpField.getVariableName(), tmpField);
                    variable = OdfElement.findNextChildNode(TextVariableDeclElement.class, variable);
                }
            }
        }

        return result;
    }

    /**
     * Try to extract value from given field.
     *
     * IMPORTANT ! For now, only fields of {@link VariableField.VariableType#USER}
     * type are supported.
     *
     * @param field Object to extract value from.
     * @return Found value, or null if we cannot find any.
     * @throws ReflectiveOperationException If an error occurred while analyzing
     * input variable.
     * @throws UnsupportedOperationException If input variable type is not {@link VariableField.VariableType#USER}
     */
    public static Object getVariableValue(final VariableField field) throws ReflectiveOperationException {
        ArgumentChecks.ensureNonNull("input variable field", field);
        if (FieldType.USER_VARIABLE_FIELD.equals(field.getFieldType())) {
            Field userVariableField = getUserVariableField();
            return getValue((TextUserFieldDeclElement) userVariableField.get(field));
        } else {
            throw new UnsupportedOperationException("Not done yet.");
        }
    }

    private static Field getUserVariableField() {
        if (USER_FIELD == null) {
            try {
                USER_FIELD = VariableField.class.getDeclaredField("userVariableElement");
            } catch (NoSuchFieldException ex) {
                throw new IllegalStateException("Cannot access user field.", ex);
            }
            USER_FIELD.setAccessible(true);
        }
        return USER_FIELD;
    }

    private static Field getSimpleVariableField() {
        if (SIMPLE_FIELD == null) {
            try {
                SIMPLE_FIELD = VariableField.class.getDeclaredField("simpleVariableElement");
            } catch (NoSuchFieldException ex) {
                throw new IllegalStateException("Cannot access user field.", ex);
            }
            SIMPLE_FIELD.setAccessible(true);
        }
        return SIMPLE_FIELD;
    }

    /**
     * Analyze input element tto find contained value.
     * @param input
     * @return value hold by given object, or null.
     */
    private static Object getValue(TextUserFieldDeclElement userVariable) {
        // really crappy...
        Object value = userVariable.getOfficeStringValueAttribute();
        if (value == null) {
            value = userVariable.getOfficeTimeValueAttribute();
            if (value == null) {
                value = userVariable.getOfficeDateValueAttribute();
                if (value == null) {
                    value = userVariable.getOfficeBooleanValueAttribute();
                    if (value == null) {
                        value = userVariable.getOfficeValueAttribute();
                    }
                }
            }
        }

        return value;
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
                masterPage.setPageHeight(PORTRAIT_WIDTH);
                masterPage.setPageWidth(PORTRAIT_HEIGHT);
                break;
            case PORTRAIT:
                masterPage.setPageWidth(PORTRAIT_WIDTH);
                masterPage.setPageHeight(PORTRAIT_HEIGHT);
        }
        if (margin != null) {
            masterPage.setMargins(margin.getTop(), margin.getBottom(), margin.getLeft(), margin.getRight());
        }
        masterPage.setFootnoteMaxHeight(0);
        return masterPage;
    }

    /**
     * Aggregate all provided data into one ODT document.
     *
     * Recognized data :
     * - {@link TextDocument}
     * - {@link RenderedImage}
     * - {@link PDDocument}
     * - {@link PDPage}
     * - {@link CharSequence}
     *
     * Also, if a data reference is given as :
     * - {@link URI}
     * - {@link URL}
     * - {@link File}
     * - {@link Path}
     * - {@link InputStream}
     *
     * We'll try to read it and convert it into one of the managed objects.
     *
     * @param output output ODT file. If it already exists, we try to open it and
     * add content at the end of the document. If it does not exists, we create
     * a new file.
     * @param candidates Objects to concatenate.
     * @throws java.lang.Exception If output
     */
    public static void concatenate(Path output, Object ... candidates) throws Exception {
        try (final TextDocument doc = Files.exists(output)?
                TextDocument.loadDocument(output.toFile()) : TextDocument.newTextDocument()) {

            for (Object candidate : candidates) {
                append(doc, candidate);
            }

            try (final OutputStream stream = Files.newOutputStream(output, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
                doc.save(stream);
            } catch (Exception e) {
                try {
                    Files.deleteIfExists(output);
                } catch (Exception ignored) {
                    e.addSuppressed(ignored);
                }
            }
        }
    }

    /**
     * Try to read input object and put it into given document.
     *
     * Recognized data :
     * - {@link TextDocument}
     * - {@link RenderedImage}
     * - {@link PDDocument}
     * - {@link PDPage}
     * - {@link CharSequence}
     *
     * Also, if a data reference is given as :
     * - {@link URI}
     * - {@link URL}
     * - {@link File}
     * - {@link Path}
     * - {@link InputStream}
     *
     * We'll try to read it and convert it into one of the managed objects.
     *
     * @param holder Document to append data into.
     * @param candidate Object to read and put at the end of given document.
     * @throws Exception If we cannot read object or it is not a supported format.
     */
    public static void append(TextDocument holder, Object candidate) throws Exception {
        if (candidate == null) {
            return;
        }

        boolean deletePath = false;

        // Unify all reference APIs to Path.
        if (candidate instanceof File) {
            candidate = ((File)candidate).toPath();
        } else if (candidate instanceof URI) {
            candidate = Paths.get((URI)candidate);
        } else if (candidate instanceof URL) {
            candidate = Paths.get(((URL)candidate).toURI());
        } else if (candidate instanceof InputStream) {
            final Path tmpFile = Files.createTempFile("candidate", ".tmp");
            Files.copy((InputStream)candidate, tmpFile);
            candidate = tmpFile;
            deletePath = true;
        }

        // If we've got a reference to an external data, we try to read it.
        if (candidate instanceof Path) {
            final Path tmpPath = (Path) candidate;
            if (!Files.isReadable(tmpPath)) {
                throw new IllegalArgumentException("Path given for document concatenation is not readable : "+tmpPath);
            }

            try (final TextDocument tmpDoc = TextDocument.loadDocument(tmpPath.toFile())) {

                holder.insertContentFromDocumentAfter(tmpDoc, holder.addParagraph(""), true);

            } catch (Exception e) {
                try {

                    appendPDF(holder, tmpPath);

                } catch (Exception e1) {
                    try {

                        appendImage(holder, tmpPath);

                    } catch (Exception e2) {
                        try {

                            appendTextFile(holder, tmpPath);

                        } catch (Exception e3) {
                            e.addSuppressed(e1);
                            e.addSuppressed(e2);
                            e.addSuppressed(e3);
                            throw e;
                        }
                    }
                }
            } finally {
                if (deletePath) {
                    Files.deleteIfExists(tmpPath);
                }
            }

        } else if (candidate instanceof TextDocument) {
            holder.insertContentFromDocumentAfter((TextDocument) candidate, holder.addParagraph(""), true);
        } else if (candidate instanceof PDDocument) {
            appendPDF(holder, (PDDocument) candidate);
        } else if (candidate instanceof PDPage) {
            appendPage(holder, (PDPage) candidate);
        } else if (candidate instanceof RenderedImage) {
            appendImage(holder, (RenderedImage) candidate);
        } else if (candidate instanceof CharSequence) {
            holder.addParagraph(((CharSequence)candidate).toString());
        } else {
            throw new UnsupportedOperationException("Object type not supported for insertion in ODT : "+ candidate.getClass().getCanonicalName());
        }
    }

    /**
     * Put content of a text file into given document. We assume that input text file
     * paragraphs are separated by a blank line.
     *
     * Note : all lines are trimmed and all blank line are suppressed.
     *
     * @param holder Document to append data into.
     * @param txtFile Text file to read and concatenate.
     * @throws IOException If an error occurs while reading input file.
     */
    public static void appendTextFile(final TextDocument holder, final Path txtFile) throws IOException {
        try (final BufferedReader reader = Files.newBufferedReader(txtFile)) {
            String line = reader.readLine();
            if (line != null) {
                StringBuilder txtBuilder = new StringBuilder(line);
                final String sep = System.lineSeparator();
                while ((line = reader.readLine().trim()) != null) {
                    if (line.isEmpty()) {
                        // skip all blank lines
                        if (txtBuilder.length() <= 0)
                            continue;
                        holder.addParagraph(txtBuilder.toString());
                        line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        txtBuilder = new StringBuilder(line);
                    } else {
                        txtBuilder.append(sep).append(line);
                    }
                }
            }
        }
    }

    /**
     * Read a PDF document and put its content into given document.
     *
     * Note : Each PDF page is transformed into an image.
     *
     * @param target Document to put pdf content into.
     * @param input Location of the PDF document to read.
     * @throws IOException If we fail reading input pdf, or writing image generated from it.
     */
    public static void appendPDF(final TextDocument target, final Path input) throws IOException {
        ArgumentChecks.ensureNonNull("Output document", target);
        ArgumentChecks.ensureNonNull("Input document", input);
        try (final InputStream in = Files.newInputStream(input, StandardOpenOption.READ);
                final PDDocument loaded = PDDocument.load(in)) {
            appendPDF(target, loaded);
        }
    }

    /**
     * Read a PDF document and put its content into given document.
     *
     * Note : Each PDF page is transformed into an image.
     *
     * @param target Document to put pdf content into.
     * @param input PDF document to read.
     * @throws IOException If we fail reading input pdf, or writing image generated from it.
     */
    public static void appendPDF(final TextDocument target, final PDDocument input) throws IOException {
        ArgumentChecks.ensureNonNull("Output document", target);
        ArgumentChecks.ensureNonNull("Input document", input);
        if (input.isEncrypted()) {
            Optional<String> pwd = askPassword(ASK_PASSWORD, input);
            while (pwd.isPresent()) {
                try {
                    input.openProtection(new StandardDecryptionMaterial(pwd.get()));
                    break;
                } catch (CryptographyException | BadSecurityHandlerException e) {
                    pwd = askPassword("Le mot de passe est invalide. Veuillez recommencer.", input);
                }
            }

            if (!pwd.isPresent()) {
                throw new IOException("No password available to decode PDF file.");
            }
        }
        final List<PDPage> pages = input.getDocumentCatalog().getAllPages();
        for (final PDPage page : pages) {
            appendPage(target, page);
        }
    }

    /**
     * Pops a JavaFX alert to query document password.
     * @param headerText A quick sentence about what is asked to user.
     * @param input Document to get password for.
     * @return An optional containing user password, or an empty optional if user cancelled the alert.
     */
    private static Optional<String> askPassword(final String headerText, final PDDocument input) {
        final String sep = System.lineSeparator();
        final StringBuilder builder = new StringBuilder(headerText).append(sep).append("Informations sur le document : ");

        final PDDocumentInformation docInfo = input.getDocumentInformation();
        builder.append("Titre : ").append(valueOrUnknown(docInfo.getTitle())).append(sep);
        builder.append("Auteur : ").append(valueOrUnknown(docInfo.getAuthor())).append(sep);
        builder.append("Editeur : ").append(valueOrUnknown(docInfo.getProducer())).append(sep);
        builder.append("Sujet : ").append(valueOrUnknown(docInfo.getSubject()));

        final Callable<Optional<String>> query = () -> {
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, null, ButtonType.CANCEL, ButtonType.OK);
            alert.setHeaderText(builder.toString());

            final PasswordField field = new PasswordField();
            HBox.setHgrow(field, Priority.ALWAYS);
            alert.getDialogPane().setContent(new HBox(5, new Label("Mot de passe : "), field));
            alert.setResizable(true);
            if (ButtonType.OK.equals(alert.showAndWait().orElse(ButtonType.CANCEL))) {
                final String pwd = field.getText();
                return Optional.of(pwd == null ? "" : pwd);
            } else {
                return Optional.empty();
            }
        };

        if (Platform.isFxApplicationThread()) {
            try {
                return query.call();
            } catch (Exception ex) {
                throw new SirsCoreRuntimeException(ex);
            }
        } else {
            final TaskManager.MockTask<Optional<String>> mockTask = new TaskManager.MockTask("Demande de mot de passe", query);
            Platform.runLater(mockTask);
            try {
                return mockTask.get();
            } catch (InterruptedException ex) {
                // No response for too long
                return Optional.empty();
            } catch (ExecutionException ex) {
                throw new SirsCoreRuntimeException(ex);
            }
        }
    }

    /**
     * Adapt input string value.
     * @param input The string to test.
     * @return input string if not null nor empty, or "Inconnu".
     */
    private static String valueOrUnknown(final String input) {
        if (input == null || input.isEmpty()) {
            return "Inconnu";
        } else {
            return input;
        }
    }

    /**
     * Put content of the given PDF page at the end of input ODT document.
     *
     * Note: As processing pdf text is hell (no, really), we just convert it
     * into an image and put it into given document.
     *
     * @param holder Document to append content to.
     * @param page Page to convert and write into ODT document.
     * @throws java.io.IOException If we fail reading input page, or writing generated image.
     */
    public static void appendPage(final TextDocument holder, final PDPage page) throws IOException {
        final BufferedImage img = page.convertToImage();
        holder.addPageBreak();
        ODTUtils.appendImage(holder, img, true);
    }

    /**
     * Insert given image into input document. Image is inserted in a new empty
     * paragraph, to avoid overlapping with another content.
     *
     * @param holder Document to insert image into.
     * @param image Image to put.
     */
    public static void appendImage(final TextDocument holder, final RenderedImage image) throws IOException {
        ODTUtils.appendImage(holder, image, false);
    }

    public static void appendImage(final TextDocument holder, final RenderedImage image, final boolean fullPage) throws IOException {
        final Path tmpImage = Files.createTempFile("img", ".png");
        if (!ImageIO.write(image, "png", tmpImage.toFile())) {
            throw new IllegalStateException("No valid writer found for image format png !");
        }

        try {
            final StyleTypeDefinitions.PrintOrientation orientation = (image.getWidth() < image.getHeight())
                    ? StyleTypeDefinitions.PrintOrientation.PORTRAIT : StyleTypeDefinitions.PrintOrientation.LANDSCAPE;
            appendImage(holder, tmpImage, orientation, fullPage);
        } finally {
            Files.delete(tmpImage);
        }
    }

    /**
     * Insert given image into input document. Image is inserted in a new empty
     * paragraph, to avoid overlapping with another content.
     *
     * @param holder Document to insert into
     * @param imagePath Location off the image to insert.
     */
    public static void appendImage(final TextDocument holder, final Path imagePath) {
        appendImage(holder, imagePath, null, false);
    }

    /**
     * Insert given image into input document. Image is inserted in a new empty
     * paragraph, to avoid overlapping with another content.
     *
     * @param holder Document to insert into
     * @param imagePath Location off the image to insert.
     * @param fullPage If true, we will create a new page (no margin, orientation set according to image dimension) in which the image will be rendered.
     */
    public static void appendImage(final TextDocument holder, final Path imagePath, final boolean fullPage) {
        appendImage(holder, imagePath, null, fullPage);
    }

    /**
     * Insert given image into input document. Image is inserted in a new empty
     * paragraph, to avoid overlapping with another content.
     *
     * @param holder Document to insert into
     * @param imagePath Location off the image to insert.
     * @param orientation Orientation of the image to insert. Only used if a full page rendering is queried.
     * If null, we will try to determine the best orientation by analyzing image dimension.
     * @param fullPage If true, we will create a new page (no margin, orientation set by previous parameter) in which the image will be rendered.
     */
    public static void appendImage(
            final TextDocument holder,
            final Path imagePath,
            StyleTypeDefinitions.PrintOrientation orientation,
            final boolean fullPage) {

        if (fullPage) {
            if (orientation == null) {
                try {
                    ImageReader reader = XImageIO.getReader(imagePath.toFile(), false, false);
                    orientation = (reader.getWidth(0) < reader.getHeight(0))
                            ? StyleTypeDefinitions.PrintOrientation.PORTRAIT : StyleTypeDefinitions.PrintOrientation.LANDSCAPE;
                } catch (IOException e) {
                    SirsCore.LOGGER.log(Level.WARNING, "Cannot read image attributes : " + imagePath, e);
                    orientation = StyleTypeDefinitions.PrintOrientation.PORTRAIT;
                }
            }

            try {
                holder.addPageBreak(
                        holder.getParagraphByReverseIndex(0, false),
                        getOrCreateOrientationMasterPage(holder, orientation, Insets.EMPTY)
                );
            } catch (Exception ex) {
                SirsCore.LOGGER.log(Level.WARNING, "Cannot add a page break in a text document", ex);
            }
        }

        final Image newImage = Image.newImage(holder.addParagraph(""), imagePath.toUri());

        if (fullPage) {
            newImage.getStyleHandler().setAchorType(StyleTypeDefinitions.AnchorType.TO_PAGE);
        }
    }
}
