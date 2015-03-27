
package fr.sirs.util;

import fr.sirs.SIRS;
import static fr.sirs.SIRS.BUNDLE_KEY_CLASS;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
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
public class JRDomWriter {
    
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
    private int height_multiplicator;
    
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
    
    // Jasper Reports attributes.
    private static final String URI_JRXML = "http://jasperreports.sourceforge.net/jasperreports";
    private static final String URI_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String PREFIX_XSI = "xsi";
    private static final String ATT_XSI_SCHEMA_LOCATION = "schemaLocation";
    
    // Jasper Reports tags.
    private static final String TAG_JASPER_REPORT = "jasperReport";
    private static final String TAG_PAGE_WIDTH = "pageWidth";
    private static final String TAG_PAGE_HEIGHT = "pageHeight";
    private static final String TAG_COLUMN_WIDTH = "columnWidth";
    private static final String TAG_LEFT_MARGIN = "leftMargin";
    private static final String TAG_RIGHT_MARGIN = "rightMargin";
    private static final String TAG_TOP_MARGIN = "topMargin";
    private static final String TAG_BOTTOM_MARGIN = "bottomMargin";
    private static final String TAG_FIELD = "field";
    private static final String TAG_FIELD_DESCRIPTION = "fieldDescription";
    private static final String TAG_FRAME = "frame";
    private static final String TAG_TITLE = "title";
    private static final String TAG_PAGE_HEADER = "pageHeader";
    private static final String TAG_COLUMN_HEADER = "columnHeader";
    private static final String TAG_DETAIL = "detail";
    private static final String TAG_COLUMN_FOOTER = "colulmnFooter";
    private static final String TAG_PAGE_FOOTER = "pageFooter";
    private static final String TAG_LAST_PAGE_FOOTER = "lastPageFooter";
    private static final String TAG_BAND = "band";
    private static final String TAG_STATIC_TEXT = "staticText";
    private static final String TAG_TEXT_ELEMENT = "textElement";
    private static final String TAG_REPORT_ELEMENT = "reportElement";
    private static final String TAG_FONT = "font";
    private static final String TAG_TEXT_FIELD = "textField";
    private static final String TAG_TEXT_FIELD_EXPRESSION = "textFieldExpression";
    private static final String TAG_TEXT = "text";
    private static final String TAG_PATTERN = "pattern";
    private static final String TAG_BOX = "box";
    private static final String TAG_BOTTOM_PEN = "bottomPen";
    
    
    private static final String BOOLEAN_PRIMITIVE_NAME = "boolean";
    private static final String FLOAT_PRIMITIVE_NAME = "float";
    private static final String DOUBLE_PRIMITIVE_NAME = "double";
    private static final String INTEGER_PRIMITIVE_NAME = "int";
    private static final String LONG_PRIMITIVE_NAME = "long";
    
    private static final String BOOLEAN_CANONICAL_NAME = "java.lang.Boolean";
    private static final String FLOAT_CANONICAL_NAME = "java.lang.Float";
    private static final String DOUBLE_CANONICAL_NAME = "java.lang.Double";
    private static final String INTEGER_CANONICAL_NAME = "java.lang.Integer";
    private static final String LONG_CANONICAL_NAME = "java.lang.Long";
    
    private static final String NULL_REPLACEMENT = "Non renseigné";
    private static final String TRUE_REPLACEMENT = "Oui";
    private static final String FALSE_REPLACEMENT = "Non";
    
    // Jasper Reports attributes.
    private static final String ATT_MODE = "mode";
    private static enum Mode {
        OPAQUE("Opaque"), TRANSPARENT("Transparent");
        private final String mode;
        private Mode(final String mode){this.mode=mode;}
        
        @Override
        public String toString(){return this.mode;}
    }; 
    private static final String ATT_LINE_WIDTH = "lineWidth";
    private static final String ATT_LINE_COLOR = "lineColor";
    private static final String ATT_BACKCOLOR = "backcolor";
    private static final String ATT_NAME = "name";
    private static final String ATT_TEXT_ALIGNMENT = "textAlignment";
    private static enum TextAlignment {
        LEFT("Left"), CENTER("Center"), RIGHT("Right"), JUSTIFIED("Justified");
        private final String textAlignment;
        private TextAlignment(final String textAlignment){this.textAlignment=textAlignment;}
        
        @Override
        public String toString(){return this.textAlignment;}
    }; 
    private static final String ATT_VERTICAL_ALIGNMENT = "verticalAlignment";
    private static final String ATT_FONT_NAME = "fontName";
    private static final String ATT_IS_BOLD = "isBold";
    private static final String ATT_LANGUAGE = "language";
    private static final String ATT_PAGE_WIDTH = "pageWidth";
    private static final String ATT_PAGE_HEIGHT = "pageHeight";
    private static final String ATT_COLUMN_WIDTH = "columnWidth";
    private static final String ATT_LEFT_MARGIN = "leftMargin";
    private static final String ATT_RIGHT_MARGIN = "rightMargin";
    private static final String ATT_TOP_MARGIN = "topMargin";
    private static final String ATT_BOTTOM_MARGIN = "bottomMargin";
    private static final String ATT_UUID = "uuid";
    private static final String ATT_CLASS = "class";
    private static final String ATT_HEIGHT = "height";
    private static final String ATT_X = "x";
    private static final String ATT_Y = "y";
    private static final String ATT_WIDTH = "width";
    private static final String ATT_IS_STRETCH_WITH_OVERFLOW = "isStretchWithOverflow";
    private static final String ATT_IS_BLANK_WHEN_NULL = "isBlankWhenNull";
    private static final String ATT_POSITION_TYPE = "positionType";
    private static enum PositionType {
        FLOAT("Float"), FIX_RELATIVE_TO_TOP("FixRelativeToTop"), FIX_RELATIVE_TO_BOTTOM("FixRelativeToBottom");
        private final String positionType;
        private PositionType(final String positionType){this.positionType=positionType;}
        
        @Override
        public String toString(){return this.positionType;}
    }; 
    private static final String ATT_STRETCH_TYPE = "stretchType";
    private static enum StretchType {
        NO_STRETCH("NoStretch"), RELATIVE_TO_TALLEST_OBJECT("RelativeToTallestObject"), RELATIVE_TO_BAND_HEIGHT("RelativeToBandHeight");
        private final String stretchType;
        private StretchType(final String stretchType){this.stretchType=stretchType;}
        
        @Override
        public String toString(){return this.stretchType;}
    }; 
    private static final String ATT_MARKUP = "markup";
    private static enum Markup {
        NONE("none"), STYLED("styled"), HTML("html"), RTF("rtf");
        private final String markup;
        private Markup(final String markup){this.markup=markup;}
        
        @Override
        public String toString(){return this.markup;}
    }; 
    
    private JRDomWriter(){
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
        this.height_multiplicator = 1;
    }
    
    public JRDomWriter(final InputStream stream) throws ParserConfigurationException, SAXException, IOException {
        
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder constructeur = factory.newDocumentBuilder();
        this.document = constructeur.parse(stream);
        stream.close();
        
        this.root = this.document.getDocumentElement();
        this.title = (Element) this.root.getElementsByTagName(TAG_TITLE).item(0);
        this.pageHeader = (Element) this.root.getElementsByTagName(TAG_PAGE_HEADER).item(0);
        this.columnHeader = (Element) this.root.getElementsByTagName(TAG_COLUMN_HEADER).item(0);
        this.detail = (Element) this.root.getElementsByTagName(TAG_DETAIL).item(0);
        this.columnFooter = (Element) this.root.getElementsByTagName(TAG_COLUMN_FOOTER).item(0);
        this.pageFooter = (Element) this.root.getElementsByTagName(TAG_PAGE_FOOTER).item(0);
        this.lastPageFooter = (Element) this.root.getElementsByTagName(TAG_LAST_PAGE_FOOTER).item(0);
        
        this.fields_interline = 8;
        this.height_multiplicator = 1;
    }
    
    /**
     * This setter changes the default fields interline.
     * @param fieldsInterline 
     */
    public void setFieldsInterline(int fieldsInterline){
        this.fields_interline = fieldsInterline;
    }
    
    /**
     * This setter changes the default height multiplicator for comments or 
     * description fields.
     * @param heightMultiplicator 
     */
    public void setHeightMultiplicator(int heightMultiplicator){
        this.height_multiplicator = heightMultiplicator;
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
    private void writeObject(final Class classToMap, List<String> avoidFields) throws Exception {
        
        // Sets the initial fields used by the template.------------------------
        final Method[] methods = classToMap.getMethods();
        for (final Method method : methods){
            if(PrinterUtilities.isSetter(method)){
                final String fieldName = getFieldNameFromSetter(method);
                if (avoidFields==null || !avoidFields.contains(fieldName)) {
                    SIRS.LOGGER.log(Level.FINE, fieldName);
                    this.writeField(method);
                }
            }
        }
        
        // Modifies the title block.--------------------------------------------
        this.writeTitle(classToMap);
        
        // Writes the headers.--------------------------------------------------
        this.writePageHeader();
        this.writeColumnHeader();
        
        // Builds the body of the Jasper Reports template.----------------------
        this.writeDetail(classToMap, avoidFields);
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
        if(!method.getParameterTypes()[0].isPrimitive()){
            field.setAttribute(ATT_CLASS, method.getParameterTypes()[0].getCanonicalName());
        } else {
            switch(method.getParameterTypes()[0].getCanonicalName()){
                case BOOLEAN_PRIMITIVE_NAME: field.setAttribute(ATT_CLASS, BOOLEAN_CANONICAL_NAME);break;
                case FLOAT_PRIMITIVE_NAME: field.setAttribute(ATT_CLASS, FLOAT_CANONICAL_NAME);break;
                case DOUBLE_PRIMITIVE_NAME: field.setAttribute(ATT_CLASS, DOUBLE_CANONICAL_NAME);break;
                case INTEGER_PRIMITIVE_NAME: field.setAttribute(ATT_CLASS, INTEGER_CANONICAL_NAME);break;
                case LONG_PRIMITIVE_NAME: field.setAttribute(ATT_CLASS, LONG_CANONICAL_NAME);break;
            }
        }
        
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
        final ResourceBundle resourceBundle = ResourceBundle.getBundle(classToMap.getName());
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
    private void writeDetail(final Class classToMap, List<String> avoidFields) throws Exception{
        
        final ResourceBundle resourceBundle = ResourceBundle.getBundle(classToMap.getName());
        
        // Loops over the method looking for setters (based on the field names).
        final Method[] methods = classToMap.getMethods();
        int i = 0;
        for (final Method method : methods){
            if(PrinterUtilities.isSetter(method)){
                
                // Retrives the field name from the setter name.----------------
                final String fieldName = getFieldNameFromSetter(method);
                final Class fieldClass = method.getParameterTypes()[0];
                
                // Provides a multiplied height for comment and description fields.
                final int heightMultiplicator;
                final Markup markup;
                if (fieldName.contains("escript") || fieldName.contains("omment")){
                    heightMultiplicator=this.height_multiplicator;
                    markup = Markup.HTML;
                } else {
                    heightMultiplicator=1;
                    markup = Markup.NONE;
                }
                
                // Writes the field.--------------------------------------------
                if(avoidFields==null || !avoidFields.contains(fieldName)){
                    this.writeDetailField(fieldName, fieldClass, i, heightMultiplicator, markup, resourceBundle);
                    i+=heightMultiplicator;
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
    private void writeDetailField(final String field, final Class fieldClass, final int order, final int heightMultiplicator, final Markup style, final ResourceBundle resourceBundle){
        
        // Looks for the band element.------------------------------------------
        final Element band = (Element) this.detail.getElementsByTagName(TAG_BAND).item(0);
        
        // Sets the frame.------------------------------------------------------
        final Element frame = this.document.createElement(TAG_FRAME);
        
        final Element frameReportElement = this.document.createElement(TAG_REPORT_ELEMENT);
        frameReportElement.setAttribute(ATT_X, String.valueOf(INDENT_LABEL-INDENT_LABEL));
        frameReportElement.setAttribute(ATT_Y, String.valueOf((FIELDS_HEIGHT+fields_interline)*order));
        frameReportElement.setAttribute(ATT_WIDTH, String.valueOf(COLUMN_WIDTH));
        frameReportElement.setAttribute(ATT_HEIGHT, String.valueOf(FIELDS_HEIGHT*heightMultiplicator+fields_interline*(heightMultiplicator-1)));
        frameReportElement.setAttribute(ATT_POSITION_TYPE, PositionType.FLOAT.toString());
        frameReportElement.setAttribute(ATT_MODE, Mode.OPAQUE.toString());
        if(order%2==0)
            frameReportElement.setAttribute(ATT_BACKCOLOR, "#F0F0F0");
        else
            frameReportElement.setAttribute(ATT_BACKCOLOR, "#F5F5F5");
        
        final Element box = this.document.createElement(TAG_BOX);
        
        final Element bottomPen = this.document.createElement(TAG_BOTTOM_PEN);
        bottomPen.setAttribute(ATT_LINE_WIDTH, "0.25");
        bottomPen.setAttribute(ATT_LINE_COLOR, "#CCCCCC");
        
        // Builds the DOM tree.-------------------------------------------------
        box.appendChild(bottomPen);
        frame.appendChild(frameReportElement);
        frame.appendChild(box);
        
        // Sets the field's label.----------------------------------------------
        final Element staticText = this.document.createElement(TAG_STATIC_TEXT);
        
        final Element staticTextReportElement = this.document.createElement(TAG_REPORT_ELEMENT);
        staticTextReportElement.setAttribute(ATT_X, String.valueOf(INDENT_LABEL-INDENT_LABEL));
        staticTextReportElement.setAttribute(ATT_Y, String.valueOf(0));
        staticTextReportElement.setAttribute(ATT_WIDTH, String.valueOf(LABEL_WIDTH));
        staticTextReportElement.setAttribute(ATT_HEIGHT, String.valueOf(FIELDS_HEIGHT*heightMultiplicator+fields_interline*(heightMultiplicator-1)));
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
        textFieldReportElement.setAttribute(ATT_HEIGHT, String.valueOf(FIELDS_HEIGHT*heightMultiplicator+fields_interline*(heightMultiplicator-1)));
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
