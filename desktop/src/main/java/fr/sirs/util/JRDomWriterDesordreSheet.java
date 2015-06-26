
package fr.sirs.util;

import static fr.sirs.SIRS.BUNDLE_KEY_CLASS;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Observation;
import static fr.sirs.util.JRUtils.ATT_BACKCOLOR;
import static fr.sirs.util.JRUtils.ATT_CLASS;
import static fr.sirs.util.JRUtils.ATT_FONT_NAME;
import static fr.sirs.util.JRUtils.ATT_HEIGHT;
import static fr.sirs.util.JRUtils.ATT_IS_BOLD;
import static fr.sirs.util.JRUtils.ATT_IS_STRETCH_WITH_OVERFLOW;
import static fr.sirs.util.JRUtils.ATT_KEY;
import static fr.sirs.util.JRUtils.ATT_LINE_COLOR;
import static fr.sirs.util.JRUtils.ATT_LINE_WIDTH;
import static fr.sirs.util.JRUtils.ATT_MARKUP;
import static fr.sirs.util.JRUtils.ATT_MODE;
import static fr.sirs.util.JRUtils.ATT_NAME;
import static fr.sirs.util.JRUtils.ATT_POSITION_TYPE;
import static fr.sirs.util.JRUtils.ATT_STYLE;
import static fr.sirs.util.JRUtils.ATT_SUB_DATASET;
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
import static fr.sirs.util.JRUtils.TAG_COLUMN;
import static fr.sirs.util.JRUtils.TAG_COLUMN_FOOTER;
import static fr.sirs.util.JRUtils.TAG_COLUMN_HEADER;
import static fr.sirs.util.JRUtils.TAG_COMPONENT_ELEMENT;
import static fr.sirs.util.JRUtils.TAG_DATASET_RUN;
import static fr.sirs.util.JRUtils.TAG_DATA_SOURCE_EXPRESSION;
import static fr.sirs.util.JRUtils.TAG_DETAIL;
import static fr.sirs.util.JRUtils.TAG_DETAIL_CELL;
import static fr.sirs.util.JRUtils.TAG_FIELD;
import static fr.sirs.util.JRUtils.TAG_FIELD_DESCRIPTION;
import static fr.sirs.util.JRUtils.TAG_FONT;
import static fr.sirs.util.JRUtils.TAG_FRAME;
import static fr.sirs.util.JRUtils.TAG_LAST_PAGE_FOOTER;
import static fr.sirs.util.JRUtils.TAG_PAGE_FOOTER;
import static fr.sirs.util.JRUtils.TAG_PAGE_HEADER;
import static fr.sirs.util.JRUtils.TAG_REPORT_ELEMENT;
import static fr.sirs.util.JRUtils.TAG_STATIC_TEXT;
import static fr.sirs.util.JRUtils.TAG_SUB_DATASET;
import static fr.sirs.util.JRUtils.TAG_TABLE;
import static fr.sirs.util.JRUtils.TAG_TABLE_FOOTER;
import static fr.sirs.util.JRUtils.TAG_TABLE_HEADER;
import static fr.sirs.util.JRUtils.TAG_TEXT;
import static fr.sirs.util.JRUtils.TAG_TEXT_ELEMENT;
import static fr.sirs.util.JRUtils.TAG_TEXT_FIELD;
import static fr.sirs.util.JRUtils.TAG_TEXT_FIELD_EXPRESSION;
import static fr.sirs.util.JRUtils.TAG_TITLE;
import fr.sirs.util.JRUtils.TextAlignment;
import static fr.sirs.util.JRUtils.URI_JRXML;
import static fr.sirs.util.JRUtils.URI_JRXML_COMPONENTS;
import static fr.sirs.util.JRUtils.getCanonicalName;
import static fr.sirs.util.PrinterUtilities.getFieldNameFromSetter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
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
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class JRDomWriterDesordreSheet extends AbstractJDomWriter {
    
    // Template elements.
    private final Element subDataset;
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
    
    private static final String OBSERVATIONS_TABLE_DATA_SOURCE = "OBSERVATIONS_TABLE_DATA_SOURCE";
    
    private JRDomWriterDesordreSheet(){
        super();
        subDataset = null;
        this.title = null; 
        this.pageHeader = null;
        this.columnHeader = null;
        this.detail = null;
        this.columnFooter = null;
        this.pageFooter = null;
        this.lastPageFooter = null;
        
        this.fields_interline = 8;
    }
    
    public JRDomWriterDesordreSheet(final InputStream stream) throws ParserConfigurationException, SAXException, IOException {
        super(stream);
        subDataset = (Element) root.getElementsByTagName(TAG_SUB_DATASET).item(0);
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
     * @param desordre
     * @param avoidFields field names to avoid.
     * @throws TransformerException
     * @throws IOException
     */
    public void write(final Desordre desordre, final List<String> avoidFields) throws TransformerException, IOException, Exception {
        
        // Remove elements before inserting fields.-----------------------------
        root.removeChild(this.title);
        root.removeChild(this.pageHeader);
        root.removeChild(this.columnHeader);
        root.removeChild(this.detail);
        
        // Modifies the template, based on the given class.---------------------
        writeObject(desordre, avoidFields);
        
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

    /**
     * <p>This method modifies the body of the DOM.</p>
     * @param classToMap
     * @param avoidFields field names to avoid.
     * @throws Exception 
     */
    private void writeObject(final Desordre desordre, List<String> avoidFields) {
        
        
        
        if(avoidFields==null) avoidFields=new ArrayList<>();
        writeSubDataset(Observation.class, avoidFields);
        
        
        // Sets the initial fields used by the template.------------------------
        final Method[] methods = desordre.getClass().getMethods();
        for (final Method method : methods){
            if(PrinterUtilities.isSetter(method)){
                final String fieldName = getFieldNameFromSetter(method);
                if (avoidFields==null || !avoidFields.contains(fieldName)) {
                    writeField(method);
                }
            }
        }
        
        // Modifies the title block.--------------------------------------------
        writeTitle(desordre.getClass());
        
        // Writes the headers.--------------------------------------------------
        writePageHeader();
        writeColumnHeader();
        
        // Builds the body of the Jasper Reports template.----------------------
        writeDetail(desordre.getClass(), avoidFields);
    }
    
    
    
    private void writeSubDataset(final Class<? extends fr.sirs.core.model.Element> elementClass, final List<String> avoidFields){
        
        
        final Method[] methods = elementClass.getMethods();
        for (final Method method : methods){
            if(PrinterUtilities.isSetter(method)){
                final String fieldName = getFieldNameFromSetter(method);
                if (avoidFields==null || !avoidFields.contains(fieldName)) {
                    writeSubDatasetField(method);
                }
            }
        }
        
    }
        
    /**
     * <p>This method writes the fiels user by the Jasper Reports template.</p>
     * @param propertyType must be a setter method starting by "set"
     */
    private void writeSubDatasetField(final Method method) {
        
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
        subDataset.appendChild(field);
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
        root.appendChild(field);
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
        ((CDATASection) text.getChildNodes().item(0)).setData("Fiche détaillée de " + className);
        
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
        
        /*----------------------------------------------------------------------
        ATTRIBUTS DU DESORDRE
        ----------------------------------------------------------------------*/
        // Loops over the method looking for setters (based on the field names).
        final Method[] methods = classToMap.getMethods();
        int order = 0;
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
                    writeDetailField(fieldName, fieldClass, order, markup, resourceBundle);
                    order++;
                }
            }
        }
        
        /*----------------------------------------------------------------------
        TABLE DES OBSERVATIONS
        ----------------------------------------------------------------------*/
        final Element band = (Element) detail.getElementsByTagName(TAG_BAND).item(0);
        
        final Element componentElement = document.createElement(TAG_COMPONENT_ELEMENT);
        final Element componentElementReportElement = document.createElement(TAG_REPORT_ELEMENT);
        componentElementReportElement.setAttribute(ATT_KEY, "table");
        componentElementReportElement.setAttribute(ATT_STYLE, "table");
        componentElementReportElement.setAttribute(ATT_X, String.valueOf(0));
        componentElementReportElement.setAttribute(ATT_Y, String.valueOf((FIELDS_HEIGHT+fields_interline)*order));
//        componentElementReportElement.setAttribute(ATT_Y, String.valueOf(0));
        componentElementReportElement.setAttribute(ATT_WIDTH, String.valueOf(802));
        componentElementReportElement.setAttribute(ATT_HEIGHT, String.valueOf(64));
        componentElementReportElement.setAttribute(ATT_POSITION_TYPE, PositionType.FLOAT.toString());
//        componentElementReportElement.setAttribute(ATT_IS_STRETCH_WITH_OVERFLOW, String.valueOf(true));
        
        // Set the table element
        final Element table = document.createElementNS(URI_JRXML_COMPONENTS, TAG_TABLE);
        
        final Element datasetRun = document.createElementNS(URI_JRXML, TAG_DATASET_RUN);
        datasetRun.setAttribute(ATT_SUB_DATASET, "Query Dataset");
        final Element datasourceExpression = document.createElementNS(URI_JRXML, TAG_DATA_SOURCE_EXPRESSION);
        
        final CDATASection datasourceExpressionField = document.createCDATASection("(("+ObjectDataSource.class.getCanonicalName()+") $P{"+OBSERVATIONS_TABLE_DATA_SOURCE+"})");//.cloneDataSource()
        
        datasourceExpression.appendChild(datasourceExpressionField);
        datasetRun.appendChild(datasourceExpression);
        
        table.appendChild(datasetRun);
        final int columnWidth = (PAGE_WIDTH - 40);///Observation.class.getMethods().length;
        for(final Method method : Observation.class.getMethods()){
            
            if(PrinterUtilities.isSetter(method)){
                
                // Retrives the field name from the setter name.----------------
                final String fieldName = getFieldNameFromSetter(method);
                final Class fieldClass = method.getParameterTypes()[0];
                if(("id".equals(fieldName) || "designation".equals(fieldName)) 
                        && (avoidFields==null || !avoidFields.contains(fieldName)))
                    writeColumn(method, table, columnWidth);
            }
        }
        
        componentElement.appendChild(componentElementReportElement);
        componentElement.appendChild(table);
        
        band.appendChild(componentElement);
        
        
        
        
        // Sizes the detail element given to the field number.--------------------
        band.setAttribute(ATT_HEIGHT, String.valueOf((FIELDS_HEIGHT+fields_interline)*order+64));
        
        // Builds the DOM tree.-------------------------------------------------
        root.appendChild(detail);
    }
    
    
    
    private void writeColumn(final Method setter, final Element table, final int columnWidth){
        
        
        final Element column = document.createElementNS(URI_JRXML_COMPONENTS, TAG_COLUMN);
        column.setAttribute(ATT_WIDTH, String.valueOf(columnWidth));
        
        // Table header and footer
        final Element tableHeader = document.createElementNS(URI_JRXML_COMPONENTS, TAG_TABLE_HEADER);
        tableHeader.setAttribute(ATT_STYLE, "table_TH");
        tableHeader.setAttribute(ATT_HEIGHT, String.valueOf(5));
        
        final Element tableFooter = document.createElementNS(URI_JRXML_COMPONENTS, TAG_TABLE_FOOTER);
        tableFooter.setAttribute(ATT_STYLE, "table_TH");
        tableFooter.setAttribute(ATT_HEIGHT, String.valueOf(5));
        
        // Column header
        final Element jrColumnHeader = document.createElementNS(URI_JRXML_COMPONENTS, TAG_COLUMN_HEADER);
        jrColumnHeader.setAttribute(ATT_STYLE, "table_CH");
        jrColumnHeader.setAttribute(ATT_HEIGHT, String.valueOf(40));
        
            final Element staticText = document.createElementNS(URI_JRXML, TAG_STATIC_TEXT);
            
                final Element staticTextReportElement = document.createElementNS(URI_JRXML, TAG_REPORT_ELEMENT);
                staticTextReportElement.setAttribute(ATT_X, String.valueOf(INDENT_LABEL/2));
                staticTextReportElement.setAttribute(ATT_Y, String.valueOf(0));
                staticTextReportElement.setAttribute(ATT_WIDTH, String.valueOf(columnWidth-INDENT_LABEL));
                staticTextReportElement.setAttribute(ATT_HEIGHT, String.valueOf(40));
        //        staticTextReportElement.setAttribute(ATT_POSITION_TYPE, PositionType.FLOAT.toString());
                staticText.appendChild(staticTextReportElement);

                final Element text = document.createElementNS(URI_JRXML, TAG_TEXT);
                final CDATASection labelField = document.createCDATASection(getFieldNameFromSetter(setter));
                text.appendChild(labelField);

            staticText.appendChild(text);
        jrColumnHeader.appendChild(staticText);
        
        // Column footer
        final Element jrColumnFooter = document.createElementNS(URI_JRXML_COMPONENTS, TAG_COLUMN_FOOTER);
        jrColumnFooter.setAttribute(ATT_STYLE, "table_CH");
        jrColumnFooter.setAttribute(ATT_HEIGHT, String.valueOf(5));
        
        
        // Detail cell
        final Element detailCell = document.createElementNS(URI_JRXML_COMPONENTS, TAG_DETAIL_CELL);
        detailCell.setAttribute(ATT_STYLE, "table_TD");
        detailCell.setAttribute(ATT_HEIGHT, String.valueOf(40));
        
            final Element textField = document.createElementNS(URI_JRXML, TAG_TEXT_FIELD);
            textField.setAttribute(ATT_IS_STRETCH_WITH_OVERFLOW, "true");

            final Element textFieldReportElement = document.createElement(TAG_REPORT_ELEMENT);
            textFieldReportElement.setAttribute(ATT_X, String.valueOf(INDENT_LABEL/2));
            textFieldReportElement.setAttribute(ATT_Y, String.valueOf(0));
            textFieldReportElement.setAttribute(ATT_WIDTH, String.valueOf(columnWidth-INDENT_LABEL));
            textFieldReportElement.setAttribute(ATT_HEIGHT, String.valueOf(40));
    //        textFieldReportElement.setAttribute(ATT_POSITION_TYPE, PositionType.FLOAT.toString());
            textField.appendChild(textFieldReportElement);

            final Element textFieldExpression = document.createElement(TAG_TEXT_FIELD_EXPRESSION);
            final CDATASection valueField = document.createCDATASection("$F{"+getFieldNameFromSetter(setter)+"}");
            textFieldExpression.appendChild(valueField);

            textField.appendChild(textFieldExpression);
        detailCell.appendChild(textField);
        
        column.appendChild(tableHeader);
        column.appendChild(tableFooter);
        column.appendChild(jrColumnHeader);
        column.appendChild(jrColumnFooter);
        column.appendChild(detailCell);
        
        table.appendChild(column);
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
