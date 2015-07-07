
package fr.sirs.util;

import fr.sirs.SIRS;
import static fr.sirs.util.JRUtils.ATT_CLASS;
import static fr.sirs.util.JRUtils.ATT_HEIGHT;
import static fr.sirs.util.JRUtils.ATT_IS_STRETCH_WITH_OVERFLOW;
import static fr.sirs.util.JRUtils.ATT_KEY;
import static fr.sirs.util.JRUtils.ATT_MARKUP;
import static fr.sirs.util.JRUtils.ATT_NAME;
import static fr.sirs.util.JRUtils.ATT_SIZE;
import static fr.sirs.util.JRUtils.ATT_STYLE;
import static fr.sirs.util.JRUtils.ATT_SUB_DATASET;
import static fr.sirs.util.JRUtils.ATT_WIDTH;
import static fr.sirs.util.JRUtils.ATT_X;
import static fr.sirs.util.JRUtils.ATT_Y;
import fr.sirs.util.JRUtils.Markup;
import static fr.sirs.util.JRUtils.TAG_BAND;
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
import static fr.sirs.util.JRUtils.URI_JRXML;
import static fr.sirs.util.JRUtils.URI_JRXML_COMPONENTS;
import static fr.sirs.util.JRUtils.getCanonicalName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.geotoolkit.feature.type.AttributeType;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.feature.type.PropertyDescriptor;
import org.geotoolkit.report.FeatureCollectionDataSource;
import org.opengis.feature.PropertyType;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class JRDomWriterQueryResultSheet extends AbstractJDomWriter {
    
    private final Map<String, ResourceBundle> bundles = new HashMap<>();
    private final String bundlePrefix = SIRS.MODEL_PACKAGE+".";
    
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
    private int columnWidth;
    
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
    private static final int PAGE_HEIGHT = 595;
    private static final int PAGE_WIDTH = 842;
    private static final int LEFT_MARGIN = 20;
    private static final int RIGHT_MARGIN = 20;
    private static final int TOP_MARGIN = 20;
    private static final int BOTTOM_MARGIN = 20;
    
    public static final String TABLE_DATA_SOURCE = "TABLE_DATA_SOURCE";
    
    private JRDomWriterQueryResultSheet(){
        super();
        subDataset = null;
        title = null; 
        pageHeader = null;
        columnHeader = null;
        detail = null;
        columnFooter = null;
        pageFooter = null;
        lastPageFooter = null;
        
        fields_interline = 8;
        height_multiplicator = 1;
    }
    
    public JRDomWriterQueryResultSheet(final InputStream stream) throws ParserConfigurationException, SAXException, IOException {
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
        height_multiplicator = 1;
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
     * @param featureType
     * @param avoidFields field names to avoid.
     * @throws TransformerException
     * @throws IOException
     */
    public void write(final FeatureType featureType, final List<String> avoidFields) throws TransformerException, IOException, Exception {
        
        columnWidth = (PAGE_WIDTH - 40)/featureType.getProperties(true).size();
                
        // Remove elements before inserting fields.-----------------------------
        root.removeChild(title);
        root.removeChild(pageHeader);
        root.removeChild(columnHeader);
        root.removeChild(detail);
        
        // Modifies the template, based on the given class.---------------------
        writeObject(featureType, avoidFields);
        
        // Serializes the document.---------------------------------------------
        //DomUtilities.write(this.document, this.output);
        final Source source = new DOMSource(document);
        final Result result = new StreamResult(output);
        final TransformerFactory factory = TransformerFactory.newInstance();
        final Transformer trs = factory.newTransformer();
        trs.setOutputProperty(OutputKeys.INDENT, "yes");
        trs.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        trs.transform(source, result);
    }

    /**
     * <p>This method modifies the body of the DOM.</p>
     * @param featureType
     * @param avoidFields field names to avoid.
     * @throws Exception 
     */
    private void writeObject(final FeatureType featureType, List<String> avoidFields) {
        
        if(avoidFields==null) avoidFields=new ArrayList<>();
        writeSubDataset(featureType, avoidFields);
        
        // Modifies the title block.--------------------------------------------
        writeTitle();
        
        // Writes the headers.--------------------------------------------------
        writePageHeader();
        writeColumnHeader();
        
        // Builds the body of the Jasper Reports template.----------------------
        writeComponentElement(featureType);
    }
    
    
    private void writeComponentElement(final FeatureType featureType){
        final Element band = (Element) detail.getElementsByTagName(TAG_BAND).item(0);
        band.setAttribute(ATT_HEIGHT, String.valueOf(491));
        
        // Set the component element
        final Element componentElement = document.createElement(TAG_COMPONENT_ELEMENT);
        final Element componentElementReportElement = document.createElement(TAG_REPORT_ELEMENT);
        componentElementReportElement.setAttribute(ATT_KEY, "table");
        componentElementReportElement.setAttribute(ATT_STYLE, "table");
        componentElementReportElement.setAttribute(ATT_X, String.valueOf(0));
        componentElementReportElement.setAttribute(ATT_Y, String.valueOf(0));
        componentElementReportElement.setAttribute(ATT_WIDTH, String.valueOf(802));
        componentElementReportElement.setAttribute(ATT_HEIGHT, String.valueOf(491));
        
        // Set the table element
        final Element table = document.createElementNS(URI_JRXML_COMPONENTS, TAG_TABLE);
        
        final Element datasetRun = document.createElementNS(URI_JRXML, TAG_DATASET_RUN);
        datasetRun.setAttribute(ATT_SUB_DATASET, "Query Dataset");
        final Element datasourceExpression = document.createElementNS(URI_JRXML, TAG_DATA_SOURCE_EXPRESSION);
        
        final CDATASection datasourceExpressionField = document.createCDATASection("(("+FeatureCollectionDataSource.class.getCanonicalName()+") $P{"+TABLE_DATA_SOURCE+"})");//.cloneDataSource()
        
        datasourceExpression.appendChild(datasourceExpressionField);
        datasetRun.appendChild(datasourceExpression);
        
        table.appendChild(datasetRun);
        for(final PropertyDescriptor propertyDescriptor : featureType.getDescriptors()){
            writeColumn(propertyDescriptor, table, 7);
        }
        
        componentElement.appendChild(componentElementReportElement);
        componentElement.appendChild(table);
        
        band.appendChild(componentElement);
        
        root.appendChild(detail);
    }
    
    private void writeColumn(final PropertyDescriptor propertyDescriptor, final Element table, final int fontSize){
        
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
        jrColumnHeader.setAttribute(ATT_HEIGHT, String.valueOf(20));
        
        final Element staticText = document.createElementNS(URI_JRXML, TAG_STATIC_TEXT);

        final Element staticTextReportElement = document.createElementNS(URI_JRXML, TAG_REPORT_ELEMENT);
        staticTextReportElement.setAttribute(ATT_X, String.valueOf(INDENT_LABEL/2));
        staticTextReportElement.setAttribute(ATT_Y, String.valueOf(0));
        staticTextReportElement.setAttribute(ATT_WIDTH, String.valueOf(columnWidth-INDENT_LABEL));
        staticTextReportElement.setAttribute(ATT_HEIGHT, String.valueOf(20));
//        staticTextReportElement.setAttribute(ATT_POSITION_TYPE, PositionType.FLOAT.toString());
        staticText.appendChild(staticTextReportElement);
        
        final Element textElement = document.createElement(TAG_TEXT_ELEMENT);
        final Element font = document.createElement(TAG_FONT);
        font.setAttribute(ATT_SIZE, String.valueOf(fontSize));
        textElement.appendChild(font);
        staticText.appendChild(textElement);

        final Element text = document.createElementNS(URI_JRXML, TAG_TEXT);
        final CDATASection labelField = document.createCDATASection(generateFinalColumnName(propertyDescriptor));
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
        textField.setAttribute(ATT_IS_STRETCH_WITH_OVERFLOW, String.valueOf(true));

        final Element textFieldReportElement = document.createElement(TAG_REPORT_ELEMENT);
        textFieldReportElement.setAttribute(ATT_X, String.valueOf(INDENT_LABEL/2));
        textFieldReportElement.setAttribute(ATT_Y, String.valueOf(0));
        textFieldReportElement.setAttribute(ATT_WIDTH, String.valueOf(columnWidth-INDENT_LABEL));
        textFieldReportElement.setAttribute(ATT_HEIGHT, String.valueOf(40));
//        textFieldReportElement.setAttribute(ATT_POSITION_TYPE, PositionType.FLOAT.toString());
        textField.appendChild(textFieldReportElement);
        
        final Element detailTextElement = document.createElement(TAG_TEXT_ELEMENT);
        final Element detailFont = document.createElement(TAG_FONT);
        detailFont.setAttribute(ATT_SIZE, String.valueOf(fontSize));
        detailTextElement.appendChild(detailFont);
        final String fieldName = propertyDescriptor.getType().getName().toString();
        final Markup markup;
        if (fieldName.contains("escript") || fieldName.contains("omment")){
            markup = Markup.HTML;
        } else {
            markup = Markup.NONE;
        }
        detailTextElement.setAttribute(ATT_MARKUP, markup.toString());
        textField.appendChild(detailTextElement);

        final Element textFieldExpression = document.createElement(TAG_TEXT_FIELD_EXPRESSION);
        final CDATASection valueField = document.createCDATASection("$F{"+fieldName+"}");
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
    
    private void writeSubDataset(final FeatureType featureType, final List<String> avoidFields){
        
        for(final PropertyType propertyType : featureType.getProperties(true)){
            if (!avoidFields.contains(propertyType.getName().toString())) {
                writeSubDatasetField(propertyType);
            }
        }
    }
        
    /**
     * <p>This method writes the fiels user by the Jasper Reports template.</p>
     * @param propertyType must be a setter method starting by "set"
     */
    private void writeSubDatasetField(final PropertyType propertyType) {
        
        // Builds the name of the field.----------------------------------------
        final String fieldName = propertyType.getName().toString();
        
        // Creates the field element.-------------------------------------------
        final Element field = document.createElement(TAG_FIELD);
        field.setAttribute(ATT_NAME, fieldName);
        if(propertyType instanceof AttributeType){
            final AttributeType attributeType = (AttributeType) propertyType;
            final Class attributeClass = attributeType.getValueClass();
            final Optional<String> canonicalName = getCanonicalName(attributeClass);
            if(canonicalName.isPresent()) field.setAttribute(ATT_CLASS, canonicalName.get());
        }
        
        final Element fieldDescription = document.createElement(TAG_FIELD_DESCRIPTION);
        final CDATASection description = document.createCDATASection("Mettre ici une description du champ.");
        
        // Builds the DOM tree.-------------------------------------------------
        fieldDescription.appendChild(description);
        field.appendChild(fieldDescription);
        subDataset.appendChild(field);
    }
    
    /**
     * <p>This method writes the title of the template.</p>
     * @param featureType 
     */
    private void writeTitle() {
        
        // Looks for the title content.-----------------------------------------
        final Element band = (Element) this.title.getElementsByTagName(TAG_BAND).item(0);
        final Element staticText = (Element) band.getElementsByTagName(TAG_STATIC_TEXT).item(0);
        final Element text = (Element) staticText.getElementsByTagName(TAG_TEXT).item(0);
        
        // Sets the title.------------------------------------------------------
        ((CDATASection) text.getChildNodes().item(0)).setData("Résultat de requête");
        
        // Builds the DOM tree.-------------------------------------------------
        this.root.appendChild(this.title);
    }
    
    private void writePageHeader(){
        this.root.appendChild(this.pageHeader);
    }
    
    private void writeColumnHeader(){
        this.root.appendChild(this.columnHeader);
    }
    
    private String generateFinalColumnName(final PropertyDescriptor prop) {
        Map<String, Entry<String, String>> labelInfo;
        try {
            labelInfo = (Map) prop.getUserData().get("labelInfo");
        } catch (Exception ex) {
            SIRS.LOGGER.log(Level.INFO, ex.getMessage(), ex);
            labelInfo = null;
        }

        final String labelName = prop.getName().toString().replace("http://geotoolkit.org:", "");
        String columnName = labelName;
        String tableName = null;

        // If exists, explore labelInfo to retrive table and column respect to this label.
        if (labelInfo != null) {
            final Entry<String, String> entry = labelInfo.get(labelName);
            if (entry != null) {
                if (entry.getKey() != null) {
                    tableName = entry.getKey();
                } else {
                    tableName = null;
                }
                if (entry.getValue() != null) {
                    columnName = entry.getValue();
                } else {
                    columnName = labelName;
                }
            }
        }

        //If table name is not null, try to found resourcebundle for this table.
        if (tableName != null) {

            // If there isn't resource bundles (or not for the curruen table), try to generate.
            if (bundles.get(tableName) == null) {
                if (bundlePrefix != null) {
                    bundles.put(tableName, ResourceBundle.getBundle(bundlePrefix + tableName, Locale.getDefault(),
                            Thread.currentThread().getContextClassLoader()));
                }
            }
        }

        final ResourceBundle bundle = bundles.get(tableName);

        String finalColumnName;
        if (labelName == null) {
            finalColumnName = "";
        } else if (bundle == null) {
            if (!labelName.equals(columnName)) {
                finalColumnName = columnName + " as " + labelName;
            } else {
                finalColumnName = columnName;
            }
        } else {
            try {
                if (!labelName.equals(columnName)) {
                    finalColumnName = bundle.getString(columnName) + " as " + labelName;
                } else {
                    finalColumnName = bundle.getString(columnName);
                }
            } catch (MissingResourceException ex) {
                if (!labelName.equals(columnName)) {
                    finalColumnName = columnName + " as " + labelName;
                } else {
                    finalColumnName = columnName;
                }
            }
        }
        return finalColumnName;
    }
}
