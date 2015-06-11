
package fr.sirs.util;

import static fr.sirs.SIRS.BUNDLE_KEY_CLASS;
import static fr.sirs.util.JRUtils.ATT_BACKCOLOR;
import static fr.sirs.util.JRUtils.ATT_CLASS;
import static fr.sirs.util.JRUtils.ATT_FONT_NAME;
import static fr.sirs.util.JRUtils.ATT_HEIGHT;
import static fr.sirs.util.JRUtils.ATT_IS_BOLD;
import static fr.sirs.util.JRUtils.ATT_IS_STRETCH_WITH_OVERFLOW;
import static fr.sirs.util.JRUtils.ATT_LINE_COLOR;
import static fr.sirs.util.JRUtils.ATT_LINE_WIDTH;
import static fr.sirs.util.JRUtils.ATT_MARKUP;
import static fr.sirs.util.JRUtils.ATT_MODE;
import static fr.sirs.util.JRUtils.ATT_NAME;
import static fr.sirs.util.JRUtils.ATT_POSITION_TYPE;
import static fr.sirs.util.JRUtils.ATT_TEXT_ALIGNMENT;
import static fr.sirs.util.JRUtils.ATT_VERTICAL_ALIGNMENT;
import static fr.sirs.util.JRUtils.ATT_WIDTH;
import static fr.sirs.util.JRUtils.ATT_X;
import static fr.sirs.util.JRUtils.ATT_Y;
import static fr.sirs.util.JRUtils.BOOLEAN_PRIMITIVE_NAME;
import fr.sirs.util.JRUtils.Markup;
import fr.sirs.util.JRUtils.Mode;
import fr.sirs.util.JRUtils.PositionType;
import static fr.sirs.util.JRUtils.TAG_BAND;
import static fr.sirs.util.JRUtils.TAG_BOTTOM_PEN;
import static fr.sirs.util.JRUtils.TAG_BOX;
import static fr.sirs.util.JRUtils.TAG_COLUMN_FOOTER;
import static fr.sirs.util.JRUtils.TAG_COLUMN_HEADER;
import static fr.sirs.util.JRUtils.TAG_DETAIL;
import static fr.sirs.util.JRUtils.TAG_FIELD;
import static fr.sirs.util.JRUtils.TAG_FIELD_DESCRIPTION;
import static fr.sirs.util.JRUtils.TAG_FONT;
import static fr.sirs.util.JRUtils.TAG_FRAME;
import static fr.sirs.util.JRUtils.TAG_LAST_PAGE_FOOTER;
import static fr.sirs.util.JRUtils.TAG_PAGE_FOOTER;
import static fr.sirs.util.JRUtils.TAG_PAGE_HEADER;
import static fr.sirs.util.JRUtils.TAG_REPORT_ELEMENT;
import static fr.sirs.util.JRUtils.TAG_STATIC_TEXT;
import static fr.sirs.util.JRUtils.TAG_TEXT;
import static fr.sirs.util.JRUtils.TAG_TEXT_ELEMENT;
import static fr.sirs.util.JRUtils.TAG_TEXT_FIELD;
import static fr.sirs.util.JRUtils.TAG_TEXT_FIELD_EXPRESSION;
import static fr.sirs.util.JRUtils.TAG_TITLE;
import fr.sirs.util.JRUtils.TextAlignment;
import static fr.sirs.util.JRUtils.getCanonicalName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class JRDomWriterElementSheet {
    
    // Template elements.
    private final Document document;
    private final Element root;
    private final Element title;
    private final Element pageHeader;
    private final Element columnHeader;
    private final Element detail;
    private final Element columnFooter;
    private final Element pageFooter;
    private final Element lastPageFooter;
    private File output;
    
    // Dynamic template parameters.
    private int fields_interline;
    
    // Static template parameters.
    private static final String FIELDS_VERTICAL_ALIGNMENT = "Middle";
    private static final String FIELDS_FONT_NAME = "Serif";
    private static final int FIELDS_HEIGHT = 16;
    //private static final String DATE_PATTERN = "dd/MM/yyyy à hh:mm:ss";
    private static final int INDENT_LABEL = 10;
    private static final int LABEL_WIDTH = 140;
    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int COLUMN_WIDTH = 555;
    private static final int LEFT_MARGIN = 20;
    private static final int RIGHT_MARGIN = 20;
    private static final int TOP_MARGIN = 20;
    private static final int BOTTOM_MARGIN = 20;
    
    private static final String NULL_REPLACEMENT = "Non renseigné";
    private static final String TRUE_REPLACEMENT = "Oui";
    private static final String FALSE_REPLACEMENT = "Non";
    
    private JRDomWriterElementSheet(){
        this.document = null;
        this.root = null; 
        this.title = null; 
        this.pageHeader = null;
        this.columnHeader = null;
        this.detail = null;
        this.columnFooter = null;
        this.pageFooter = null;
        this.lastPageFooter = null;
        
        this.fields_interline = 8;
    }
    
    public JRDomWriterElementSheet(final InputStream stream) throws ParserConfigurationException, SAXException, IOException {
        
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder constructeur = factory.newDocumentBuilder();
        this.document = constructeur.parse(stream);
        stream.close();
        
        root = document.getDocumentElement();
        title = (Element) root.getElementsByTagName(TAG_TITLE).item(0);
        pageHeader = (Element) root.getElementsByTagName(TAG_PAGE_HEADER).item(0);
        columnHeader = (Element) root.getElementsByTagName(TAG_COLUMN_HEADER).item(0);
        detail = (Element) this.root.getElementsByTagName(TAG_DETAIL).item(0);
        columnFooter = (Element) root.getElementsByTagName(TAG_COLUMN_FOOTER).item(0);
        pageFooter = (Element) root.getElementsByTagName(TAG_PAGE_FOOTER).item(0);
        lastPageFooter = (Element) root.getElementsByTagName(TAG_LAST_PAGE_FOOTER).item(0);
        
        fields_interline = 8;
    }
    
    /**
     * This setter changes the default fields interline.
     * @param fieldsInterline 
     */
    public void setFieldsInterline(int fieldsInterline){
        fields_interline = fieldsInterline;
    }
    
    /**
     * <p>This method sets the output to write the modified DOM in.</p>
     * @param output 
     */
    public void setOutput(final File output) {
        this.output = output;
    } 
    
    /**
     * <p>This method writes a Jasper Reports template mapping the parameter class.</p>
     * @param classToMap
     * @param avoidFields field names to avoid.
     * @throws TransformerException
     * @throws IOException
     */
    public void write(final Class classToMap, final List<String> avoidFields) throws TransformerException, IOException, Exception {
        
        // Remove elements before inserting fields.-----------------------------
        this.root.removeChild(this.title);
        this.root.removeChild(this.pageHeader);
        this.root.removeChild(this.columnHeader);
        this.root.removeChild(this.detail);
        
        // Modifies the template, based on the given class.---------------------
        this.writeObject(classToMap, avoidFields);
        
        // Serializes the document.---------------------------------------------
        //DomUtilities.write(this.document, this.output);
        final Source source = new DOMSource(this.document);
        final Result result = new StreamResult(this.output);
        final TransformerFactory factory = TransformerFactory.newInstance();
        final Transformer trs = factory.newTransformer();
        trs.setOutputProperty(OutputKeys.INDENT, "yes");
        trs.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        trs.transform(source, result);
    }
    
    private String getFieldNameFromSetter(final Method setter){
        return setter.getName().substring(3, 4).toLowerCase()
                            + setter.getName().substring(4);
    }

    /**
     * <p>This method modifies the body of the DOM.</p>
     * @param classToMap
     * @param avoidFields field names to avoid.
     * @throws Exception 
     */
    private void writeObject(final Class classToMap, List<String> avoidFields) {
        
        // Sets the initial fields used by the template.------------------------
        final Method[] methods = classToMap.getMethods();
        for (final Method method : methods){
            if(PrinterUtilities.isSetter(method)){
                final String fieldName = getFieldNameFromSetter(method);
                if (avoidFields==null || !avoidFields.contains(fieldName)) {
                    this.writeField(method);
                }
            }
        }
        
        // Modifies the title block.--------------------------------------------
        writeTitle(classToMap);
        
        // Writes the headers.--------------------------------------------------
        writePageHeader();
        writeColumnHeader();
        
        // Builds the body of the Jasper Reports template.----------------------
        writeDetail(classToMap, avoidFields);
    }
        
    /**
     * <p>This method writes the fiels user by the Jasper Reports template.</p>
     * @param method must be a setter method starting by "set"
     */
    private void writeField(final Method method) {
        
        // Builds the name of the field.----------------------------------------
        final String fieldName = method.getName().substring(3, 4).toLowerCase() 
                        + method.getName().substring(4);
        
        // Creates the field element.-------------------------------------------
        final Element field = document.createElement(TAG_FIELD);
        field.setAttribute(ATT_NAME, fieldName);
        
        final Optional<String> canonicalName = getCanonicalName(method.getParameterTypes()[0]);
        if(canonicalName.isPresent()) field.setAttribute(ATT_CLASS, canonicalName.get());
        
        final Element fieldDescription = document.createElement(TAG_FIELD_DESCRIPTION);
        final CDATASection description = document.createCDATASection("Mettre ici une description du champ.");
        
        // Builds the DOM tree.-------------------------------------------------
        fieldDescription.appendChild(description);
        field.appendChild(fieldDescription);
        this.root.appendChild(field);
    }
    
    /**
     * <p>This method writes the title of the template.</p>
     * @param classToMap 
     */
    private void writeTitle(final Class classToMap) {
        
        // Looks for the title content.-----------------------------------------
        final Element band = (Element) this.title.getElementsByTagName(TAG_BAND).item(0);
        final Element staticText = (Element) band.getElementsByTagName(TAG_STATIC_TEXT).item(0);
        final Element text = (Element) staticText.getElementsByTagName(TAG_TEXT).item(0);
        
        // Sets the title.------------------------------------------------------
        final String className;
        final ResourceBundle resourceBundle = ResourceBundle.getBundle(classToMap.getName(), Locale.getDefault(),
                Thread.currentThread().getContextClassLoader());
        if(resourceBundle!=null){
            className = (resourceBundle.containsKey(BUNDLE_KEY_CLASS)) ?
                    resourceBundle.getString(BUNDLE_KEY_CLASS) : classToMap.getSimpleName();
        }
        else{
            className = classToMap.getSimpleName();
        }
        ((CDATASection) text.getChildNodes().item(0)).setData(
                "Fiche synoptique de " + className);
        
        // Builds the DOM tree.-------------------------------------------------
        this.root.appendChild(this.title);
    }
    
    private void writePageHeader(){
        this.root.appendChild(this.pageHeader);
    }
    
    private void writeColumnHeader(){
        this.root.appendChild(this.columnHeader);
    }
    
    /**
     * <p>This method writes the content of the detail element.</p>
     * @param classToMap
     * @throws Exception 
     */
    private void writeDetail(final Class classToMap, List<String> avoidFields) {
        
        final ResourceBundle resourceBundle = ResourceBundle.getBundle(classToMap.getName(), Locale.getDefault(),
                Thread.currentThread().getContextClassLoader());
        
        // Loops over the method looking for setters (based on the field names).
        final Method[] methods = classToMap.getMethods();
        int i = 0;
        for (final Method method : methods){
            if(PrinterUtilities.isSetter(method)){
                
                // Retrives the field name from the setter name.----------------
                final String fieldName = getFieldNameFromSetter(method);
                final Class fieldClass = method.getParameterTypes()[0];
                
                // Provides a multiplied height for comment and description fields.
                final Markup markup;
                if (fieldName.contains("escript") || fieldName.contains("omment")){
                    markup = Markup.HTML;
                } else {
                    markup = Markup.NONE;
                }
                
                // Writes the field.--------------------------------------------
                if(avoidFields==null || !avoidFields.contains(fieldName)){
                    writeDetailField(fieldName, fieldClass, i, markup, resourceBundle);
                    i++;
                }
            }
        }
        
        // Sizes the detail element givent the field number.--------------------
        ((Element) this.detail.getElementsByTagName(TAG_BAND).item(0))
                .setAttribute(ATT_HEIGHT, String.valueOf((FIELDS_HEIGHT+fields_interline)*i));
        
        // Builds the DOM tree.-------------------------------------------------
        this.root.appendChild(this.detail);
    }
    
    /**
     * <p>This method writes the variable of a given field.</p>
     * @param field
     * @param order
     * @param heightMultiplicator 
     */
    private void writeDetailField(final String field, final Class fieldClass, final int order, final Markup style, final ResourceBundle resourceBundle){
        
        // Looks for the band element.------------------------------------------
        final Element band = (Element) this.detail.getElementsByTagName(TAG_BAND).item(0);
        
        
        /*
        Sets the frame, that will contain the field label and the corresponding field value.
        
        ------------------------------------------------------------------------
        |                                                                      |   
        |                               FRAME                                  |  
        |                                                                      | 
        ------------------------------------------------------------------------
        */
        final Element frame = document.createElement(TAG_FRAME);
        
        final Element frameReportElement = document.createElement(TAG_REPORT_ELEMENT);
        frameReportElement.setAttribute(ATT_X, String.valueOf(0));
        frameReportElement.setAttribute(ATT_Y, String.valueOf((FIELDS_HEIGHT+fields_interline)*order));
        frameReportElement.setAttribute(ATT_WIDTH, String.valueOf(COLUMN_WIDTH));
        frameReportElement.setAttribute(ATT_HEIGHT, String.valueOf(FIELDS_HEIGHT));
        frameReportElement.setAttribute(ATT_POSITION_TYPE, PositionType.FLOAT.toString());
        frameReportElement.setAttribute(ATT_MODE, Mode.OPAQUE.toString());
        if(order%2==0)
            frameReportElement.setAttribute(ATT_BACKCOLOR, "#F0F0F0");
        else
            frameReportElement.setAttribute(ATT_BACKCOLOR, "#F5F5F5");
        
        final Element box = document.createElement(TAG_BOX);
        
        final Element bottomPen = document.createElement(TAG_BOTTOM_PEN);
        bottomPen.setAttribute(ATT_LINE_WIDTH, "0.25");
        bottomPen.setAttribute(ATT_LINE_COLOR, "#CCCCCC");
        
        // Builds the DOM tree.-------------------------------------------------
        box.appendChild(bottomPen);
        frame.appendChild(frameReportElement);
        frame.appendChild(box);
        
        
        /*
        Sets the label, that will contain the field label.
        
        ------------------------------------------------------------------------
        |    --------------------  FRAME                                       |   
        |    |       LABEL      |                                              |  
        |    --------------------                                              | 
        ------------------------------------------------------------------------
        */
        
        // Sets the field's label.----------------------------------------------
        final Element staticText = this.document.createElement(TAG_STATIC_TEXT);
        
        final Element staticTextReportElement = this.document.createElement(TAG_REPORT_ELEMENT);
        staticTextReportElement.setAttribute(ATT_X, String.valueOf(INDENT_LABEL));
        staticTextReportElement.setAttribute(ATT_Y, String.valueOf(0));
        staticTextReportElement.setAttribute(ATT_WIDTH, String.valueOf(LABEL_WIDTH));
        staticTextReportElement.setAttribute(ATT_HEIGHT, String.valueOf(FIELDS_HEIGHT));
        staticTextReportElement.setAttribute(ATT_POSITION_TYPE, PositionType.FLOAT.toString());
        
        final Element staticTextTextElement = this.document.createElement(TAG_TEXT_ELEMENT);
        staticTextTextElement.setAttribute(ATT_VERTICAL_ALIGNMENT, FIELDS_VERTICAL_ALIGNMENT);
        staticTextTextElement.setAttribute(ATT_TEXT_ALIGNMENT, TextAlignment.LEFT.toString());
        
        final Element staticTextFont = this.document.createElement(TAG_FONT);
        staticTextFont.setAttribute(ATT_IS_BOLD, "true");
        staticTextFont.setAttribute(ATT_FONT_NAME, FIELDS_FONT_NAME);
        
        final Element text = this.document.createElement(TAG_TEXT);
        
        final CDATASection labelField;
        if(resourceBundle!=null && resourceBundle.containsKey(field)){
            labelField = this.document.createCDATASection(resourceBundle.getString(field));
        } else{
            labelField = this.document.createCDATASection(field);
        }
        
        // Builds the DOM tree.-------------------------------------------------
        text.appendChild(labelField);
        staticText.appendChild(staticTextReportElement);
        staticTextTextElement.appendChild(staticTextFont);
        staticText.appendChild(staticTextTextElement);
        staticText.appendChild(text);
        frame.appendChild(staticText);
        
        
        
        /*
        Sets the field, that will contain the field value.
        
        ------------------------------------------------------------------------
        |    --------------------  FRAME  -------------------------------------|   
        |    |       LABEL      |         |               FIELD               ||  
        |    --------------------         -------------------------------------| 
        ------------------------------------------------------------------------
        */
        // Sets the field.------------------------------------------------------
        final Element textField = this.document.createElement(TAG_TEXT_FIELD);
        //if (c==Instant.class)
        //    textField.setAttribute(TAG_PATTERN, DATE_PATTERN);
        textField.setAttribute(ATT_IS_STRETCH_WITH_OVERFLOW, "true");
//        if(fieldClass!=LocalDateTime.class)
//            textField.setAttribute(ATT_IS_BLANK_WHEN_NULL, "true");
        
        final Element textFieldReportElement = document.createElement(TAG_REPORT_ELEMENT);
        textFieldReportElement.setAttribute(ATT_X, String.valueOf(INDENT_LABEL+LABEL_WIDTH));
        textFieldReportElement.setAttribute(ATT_Y, String.valueOf(0));
        textFieldReportElement.setAttribute(ATT_WIDTH, String.valueOf(COLUMN_WIDTH-(INDENT_LABEL+LABEL_WIDTH)));
        textFieldReportElement.setAttribute(ATT_HEIGHT, String.valueOf(FIELDS_HEIGHT));
        textFieldReportElement.setAttribute(ATT_POSITION_TYPE, PositionType.FLOAT.toString());
        
        final Element textFieldTextElement = document.createElement(TAG_TEXT_ELEMENT);
        textFieldTextElement.setAttribute(ATT_VERTICAL_ALIGNMENT, FIELDS_VERTICAL_ALIGNMENT);
        textFieldTextElement.setAttribute(ATT_TEXT_ALIGNMENT, TextAlignment.JUSTIFIED.toString());
        if(style!=null && style!=Markup.NONE) 
            textFieldTextElement.setAttribute(ATT_MARKUP, style.toString());
        
        final Element textFieldFont = document.createElement(TAG_FONT);
        textFieldFont.setAttribute(ATT_FONT_NAME, FIELDS_FONT_NAME);
        
        final Element textFieldExpression = document.createElement(TAG_TEXT_FIELD_EXPRESSION);
        
        // The content of the field is specific in case of Calendar field.------
        final CDATASection valueField;
        //if (c==Instant.class) 
        //    valueField = this.document.createCDATASection("$F{"+field+"}");
        //else $F{permit_quantity}.equals(null) ? $F{fst_insp_qpqlml_quantity} : $F{permit_quantity}
        
        if(fieldClass==Boolean.class || (fieldClass!=null && BOOLEAN_PRIMITIVE_NAME.equals(fieldClass.getName()))){
            valueField = document.createCDATASection("$F{"+field+"}==null ? \""+NULL_REPLACEMENT+"\" : ($F{"+field+"} ? \""+TRUE_REPLACEMENT+"\" : \""+FALSE_REPLACEMENT+"\")");
        }
        else{
            valueField = document.createCDATASection("$F{"+field+"}==null ? \""+NULL_REPLACEMENT+"\" : $F{"+field+"}");
        }
        
        // Builds the DOM tree.-------------------------------------------------
        textFieldExpression.appendChild(valueField);
        textField.appendChild(textFieldReportElement);
        textFieldTextElement.appendChild(textFieldFont);
        textField.appendChild(textFieldTextElement);
        textField.appendChild(textFieldExpression);
        frame.appendChild(textField);
        
        band.appendChild(frame);
    }
}
