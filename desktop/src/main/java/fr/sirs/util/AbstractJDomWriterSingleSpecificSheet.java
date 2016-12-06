/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.util;

import static fr.sirs.util.AbstractJDomWriter.NULL_REPLACEMENT;
import static fr.sirs.util.JRUtils.ATT_BACKCOLOR;
import static fr.sirs.util.JRUtils.ATT_CLASS;
import static fr.sirs.util.JRUtils.ATT_HEIGHT;
import static fr.sirs.util.JRUtils.ATT_IS_BOLD;
import static fr.sirs.util.JRUtils.ATT_IS_ITALIC;
import static fr.sirs.util.JRUtils.ATT_IS_STRETCH_WITH_OVERFLOW;
import static fr.sirs.util.JRUtils.ATT_IS_UNDERLINE;
import static fr.sirs.util.JRUtils.ATT_KEY;
import static fr.sirs.util.JRUtils.ATT_MARKUP;
import static fr.sirs.util.JRUtils.ATT_MODE;
import static fr.sirs.util.JRUtils.ATT_NAME;
import static fr.sirs.util.JRUtils.ATT_POSITION_TYPE;
import static fr.sirs.util.JRUtils.ATT_SIZE;
import static fr.sirs.util.JRUtils.ATT_STYLE;
import static fr.sirs.util.JRUtils.ATT_SUB_DATASET;
import static fr.sirs.util.JRUtils.ATT_WIDTH;
import static fr.sirs.util.JRUtils.ATT_X;
import static fr.sirs.util.JRUtils.ATT_Y;
import static fr.sirs.util.JRUtils.BOOLEAN_PRIMITIVE_NAME;
import static fr.sirs.util.JRUtils.TAG_BAND;
import static fr.sirs.util.JRUtils.TAG_BREAK;
import static fr.sirs.util.JRUtils.TAG_COLUMN;
import static fr.sirs.util.JRUtils.TAG_COLUMN_FOOTER;
import static fr.sirs.util.JRUtils.TAG_COLUMN_HEADER;
import static fr.sirs.util.JRUtils.TAG_COMPONENT_ELEMENT;
import static fr.sirs.util.JRUtils.TAG_DATASET_RUN;
import static fr.sirs.util.JRUtils.TAG_DATA_SOURCE_EXPRESSION;
import static fr.sirs.util.JRUtils.TAG_DETAIL_CELL;
import static fr.sirs.util.JRUtils.TAG_FIELD;
import static fr.sirs.util.JRUtils.TAG_FIELD_DESCRIPTION;
import static fr.sirs.util.JRUtils.TAG_FONT;
import static fr.sirs.util.JRUtils.TAG_FRAME;
import static fr.sirs.util.JRUtils.TAG_REPORT_ELEMENT;
import static fr.sirs.util.JRUtils.TAG_STATIC_TEXT;
import static fr.sirs.util.JRUtils.TAG_TABLE;
import static fr.sirs.util.JRUtils.TAG_TABLE_FOOTER;
import static fr.sirs.util.JRUtils.TAG_TABLE_HEADER;
import static fr.sirs.util.JRUtils.TAG_TEXT;
import static fr.sirs.util.JRUtils.TAG_TEXT_ELEMENT;
import static fr.sirs.util.JRUtils.TAG_TEXT_FIELD;
import static fr.sirs.util.JRUtils.TAG_TEXT_FIELD_EXPRESSION;
import static fr.sirs.util.JRUtils.URI_JRXML;
import static fr.sirs.util.JRUtils.URI_JRXML_COMPONENTS;
import static fr.sirs.util.JRUtils.getCanonicalName;
import static fr.sirs.util.PrinterUtilities.getFieldNameFromSetter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
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
 * @param <T>
 */
public abstract class AbstractJDomWriterSingleSpecificSheet<T extends fr.sirs.core.model.Element> extends AbstractJDomWriterSingleSheet {

    protected int currentY = 0;

    // Static template parameters.
    protected static final int PAGE_WIDTH = 595;
    protected static final int LEFT_MARGIN = 20;
    protected static final int RIGHT_MARGIN = 20;

    protected final Class<T> classToMap;
    private final List<String> avoidFields;
    
    // Couleur d'arrière-plan des titres des sections.
    private final String sectionTitleBackgroundColor;

    public AbstractJDomWriterSingleSpecificSheet(final Class<T> classToMap) {
        super();
        avoidFields = null;
        sectionTitleBackgroundColor = "#ffffff";
        this.classToMap = classToMap;
    }

    public AbstractJDomWriterSingleSpecificSheet(final Class<T> classToMap, 
            final InputStream stream, final List<String> avoidFields, final String sectionTitleBackgroundColor)
            throws ParserConfigurationException, SAXException, IOException{
        super(stream);
        this.avoidFields = avoidFields;
        this.classToMap = classToMap;
        this.sectionTitleBackgroundColor = sectionTitleBackgroundColor;
    }

    /**
     * <p>This method writes a Jasper Reports template mapping the parameter class.</p>
     * 
     * @throws TransformerException
     */
    public void write() throws TransformerException {

        // Remove elements before inserting fields.-----------------------------
        root.removeChild(title);
        root.removeChild(pageHeader);
        root.removeChild(columnHeader);
        root.removeChild(detail);

        // Modifies the template, based on the given class.---------------------
        writeObject();

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
     * 
     */
    protected void writeFields(){
        final Method[] methods = classToMap.getMethods();
        for (final Method method : methods){
            if(PrinterUtilities.isSetter(method)){
                final String fieldName = getFieldNameFromSetter(method);
                if (avoidFields==null || !avoidFields.contains(fieldName)) {
                    writeField(method);
                }
            }
        }
    }

    /**
     *
     * @param elementClass The class to explore fields.
     * @param fields The field list
     * @param print If true, print the field list, if false print all the fields but the ones contained into given field list
     * @param subDataset
     */
    protected void writeSubDataset(final Class<? extends fr.sirs.core.model.Element> elementClass,
            final List<String> fields, final boolean print, final Element subDataset) {

        final Predicate<String> printPredicate = print
                ? (String fieldName) -> fields==null || fields.contains(fieldName)
                : (String fieldName) -> fields==null || !fields.contains(fieldName);

        final Method[] methods = elementClass.getMethods();
        for (final Method method : methods){
            if(PrinterUtilities.isSetter(method)){
                final String fieldName = getFieldNameFromSetter(method);
                if (printPredicate.test(fieldName)) {
                    writeSubDatasetField(method, subDataset);
                }
            }
        }

        // Écriture d'un champ supplémentaire pour la classe de l'objet.
        writeSubDatasetField("class", Class.class, subDataset);
    }

    /**
     * <p>This method writes the fiels user by the Jasper Reports template.</p>
     * @param setter
     * @param subDataset
     */
    protected void writeSubDatasetField(final Method setter, final Element subDataset) {

        // Builds the name of the field.----------------------------------------
        final String fieldName = setter.getName().substring(3, 4).toLowerCase()
                        + setter.getName().substring(4);
        writeSubDatasetField(fieldName, setter.getParameterTypes()[0], subDataset);
    }


    protected void writeSubDatasetField(final String fieldName, final Class fieldClass, final Element subDataset) {

        // Creates the field element.-------------------------------------------
        final Element field = document.createElement(TAG_FIELD);
        field.setAttribute(ATT_NAME, fieldName);

        final Optional<String> canonicalName = getCanonicalName(fieldClass);
        if(canonicalName.isPresent()) field.setAttribute(ATT_CLASS, canonicalName.get());

        final Element fieldDescription = document.createElement(TAG_FIELD_DESCRIPTION);
        final CDATASection description = document.createCDATASection("Mettre ici une description du champ.");

        // Builds the DOM tree.-------------------------------------------------
        fieldDescription.appendChild(description);
        field.appendChild(fieldDescription);
        subDataset.appendChild(field);
    }

    /**
     * <p>This method writes the title of the template.</p>
     */
    protected void writeTitle() {
        writeTitle("Fiche détaillée de ", classToMap);
    }

    /**
     * Insertion d'un titre de section.
     * 
     * @param sectionTitle Titre de la section.
     * @param height Hauteur du cadre.
     * @param margin Marge entre le cadre et le texte (haut et bas).
     * @param indent Intentation du texte.
     * @param textSize Taille de la police.
     * @param bold Vrai si le texte est en gras.
     * @param italic Vrai si le texte est en italique.
     * @param underlined Vrai si le texte est souligné.
     */
    protected void writeSectionTitle(final String sectionTitle, final int height, final int margin, final int indent, 
            final int textSize, final boolean bold, final boolean italic, final boolean underlined){
        
        final Element band = (Element) detail.getElementsByTagName(TAG_BAND).item(0);
        final Element frame = document.createElement(TAG_FRAME);
        final Element frameReportElement = document.createElement(TAG_REPORT_ELEMENT);
        frameReportElement.setAttribute(ATT_BACKCOLOR, sectionTitleBackgroundColor);
        frameReportElement.setAttribute(ATT_HEIGHT, String.valueOf(height));
        frameReportElement.setAttribute(ATT_MODE, JRUtils.Mode.OPAQUE.toString());
        frameReportElement.setAttribute(ATT_POSITION_TYPE, JRUtils.PositionType.FLOAT.toString());
        frameReportElement.setAttribute(ATT_WIDTH, String.valueOf(PAGE_WIDTH-LEFT_MARGIN-RIGHT_MARGIN));
        frameReportElement.setAttribute(ATT_X, String.valueOf(0));
        frameReportElement.setAttribute(ATT_Y, String.valueOf(currentY));
        frame.appendChild(frameReportElement);

        final Element staticText = document.createElement(TAG_STATIC_TEXT);
        final Element staticTextReportElement = document.createElement(TAG_REPORT_ELEMENT);
        staticTextReportElement.setAttribute(ATT_HEIGHT, String.valueOf(height-2*margin));
        staticTextReportElement.setAttribute(ATT_WIDTH, String.valueOf(PAGE_WIDTH-LEFT_MARGIN-RIGHT_MARGIN-indent));
        staticTextReportElement.setAttribute(ATT_X, String.valueOf(indent));
        staticTextReportElement.setAttribute(ATT_Y, String.valueOf(margin));
        staticText.appendChild(staticTextReportElement);

        final Element textElement = document.createElement(TAG_TEXT_ELEMENT);
        final Element font = document.createElement(TAG_FONT);
        font.setAttribute(ATT_IS_BOLD, String.valueOf(bold));
        font.setAttribute(ATT_IS_ITALIC, String.valueOf(italic));
        font.setAttribute(ATT_IS_UNDERLINE, String.valueOf(underlined));
        font.setAttribute(ATT_SIZE, String.valueOf(textSize));
        textElement.appendChild(font);
        staticText.appendChild(textElement);

        final Element text = document.createElement(TAG_TEXT);
        final CDATASection textField = document.createCDATASection(sectionTitle);
        text.appendChild(textField);
        staticText.appendChild(text);
        frame.appendChild(staticText);
        band.appendChild(frame);
        currentY+=height;
    }
    
    /**
     * Insertion d'un saut de page.
     */
    protected void writeDetailPageBreak(){

        final Element band = (Element) detail.getElementsByTagName(TAG_BAND).item(0);
        final Element pageBreak = document.createElement(TAG_BREAK);
        final Element pageBreakReportElement = document.createElement(TAG_REPORT_ELEMENT);
        pageBreakReportElement.setAttribute(ATT_HEIGHT, String.valueOf(1));
        pageBreakReportElement.setAttribute(ATT_WIDTH, String.valueOf(PAGE_WIDTH-LEFT_MARGIN-RIGHT_MARGIN));
        pageBreakReportElement.setAttribute(ATT_X, String.valueOf(0));
        pageBreakReportElement.setAttribute(ATT_Y, String.valueOf(currentY));
        
        currentY++;
        pageBreak.appendChild(pageBreakReportElement);
        band.appendChild(pageBreak);
    }

    /**
     *
     * @param clazz
     * @param fields
     * @param print
     * @param datasourceParameter
     * @param datasetName
     * @param height
     */
    protected void writeTable(final Class clazz, final List<String> fields,
            final boolean print, final String datasourceParameter, final String datasetName, final int height){

        final Predicate<String> printPredicate = print
                ? (String fieldName) -> fields==null || fields.contains(fieldName)
                : (String fieldName) -> fields==null || !fields.contains(fieldName);

        final Element band = (Element) detail.getElementsByTagName(TAG_BAND).item(0);

        final Element componentElement = document.createElement(TAG_COMPONENT_ELEMENT);
        final Element componentElementReportElement = document.createElement(TAG_REPORT_ELEMENT);
        componentElementReportElement.setAttribute(ATT_KEY, "table");
        componentElementReportElement.setAttribute(ATT_STYLE, "table");
        componentElementReportElement.setAttribute(ATT_X, String.valueOf(0));
        componentElementReportElement.setAttribute(ATT_Y, String.valueOf(currentY));
//        componentElementReportElement.setAttribute(ATT_Y, String.valueOf(0));
        componentElementReportElement.setAttribute(ATT_WIDTH, String.valueOf(802));
        componentElementReportElement.setAttribute(ATT_HEIGHT, String.valueOf(height));
        componentElementReportElement.setAttribute(ATT_POSITION_TYPE, JRUtils.PositionType.FLOAT.toString());
//        componentElementReportElement.setAttribute(ATT_IS_STRETCH_WITH_OVERFLOW, String.valueOf(true));

        // Set the table element
        final Element table = document.createElementNS(URI_JRXML_COMPONENTS, TAG_TABLE);

        final Element datasetRun = document.createElementNS(URI_JRXML, TAG_DATASET_RUN);
        datasetRun.setAttribute(ATT_SUB_DATASET, datasetName);
        final Element datasourceExpression = document.createElementNS(URI_JRXML, TAG_DATA_SOURCE_EXPRESSION);

        final CDATASection datasourceExpressionField = document.createCDATASection("(("+ObjectDataSource.class.getCanonicalName()+") $F{"+datasourceParameter+"})");//.cloneDataSource()

        datasourceExpression.appendChild(datasourceExpressionField);
        datasetRun.appendChild(datasourceExpression);

        table.appendChild(datasetRun);

        ////////////////////////////////////////////////////////////////////////
        // COMPUTE NUMBER OF COLUMNS AND COLUMN WIDTH
        ////////////////////////////////////////////////////////////////////////
        int nbColumns=0;
        // Si la liste des champs contient les champs à imprimer et non pas à éviter alors le nombre de champs est directement donnée par la taille de la liste.
        if(print) {
            nbColumns=fields.size();
        }
        // Sinon il faut faire un premier parcours pour calculer le nombre de colonnes
        else {
            for(final Method method : clazz.getMethods()){
                if(PrinterUtilities.isSetter(method)){
                    // Retrives the field name from the setter name.----------------
                    final String fieldName = getFieldNameFromSetter(method);
                    if(printPredicate.test(fieldName))
                        nbColumns++;
                }
            }
        }

        // If class is abstract, add one column to print class name
        if(Modifier.isAbstract(clazz.getModifiers())){
            nbColumns++;
        }

        // Calcul de la largeur d'une colonne en fonction du nombre
        final int columnWidth = (PAGE_WIDTH - (LEFT_MARGIN+LEFT_MARGIN))/nbColumns;


        ////////////////////////////////////////////////////////////////////////
        // FUNCTIONAL PARAMETERS TO FILL COLUMNS
        ////////////////////////////////////////////////////////////////////////
        final Function<Method, Supplier<CDATASection>> getFromMethodSupplier = (Method setter) -> {

            final String fieldName = getFieldNameFromSetter(setter);
            final Class fieldClass = setter.getParameterTypes()[0];

            Supplier<CDATASection> fromMethodSupplier = () -> {

                final CDATASection valueField;
                if(fieldClass==Boolean.class || (fieldClass!=null && BOOLEAN_PRIMITIVE_NAME.equals(fieldClass.getName()))){
                    valueField = document.createCDATASection("$F{"+fieldName+"}==null ? \""+NULL_REPLACEMENT+"\" : ($F{"+fieldName+"} ? \""+TRUE_REPLACEMENT+"\" : \""+FALSE_REPLACEMENT+"\")");
                }
                else{
                    valueField = document.createCDATASection("$F{"+fieldName+"}==null ? \""+NULL_REPLACEMENT+"\" : $F{"+fieldName+"}");
                }
                return valueField;
            };

            return fromMethodSupplier;
        };

        final Function<String, JRUtils.Markup> markupFromFieldName = (String fieldName) ->
                (fieldName.contains("escript") || fieldName.contains("omment")) ? JRUtils.Markup.HTML : JRUtils.Markup.NONE;

        if(Modifier.isAbstract(clazz.getModifiers())){
            writeColumn("Type",
                    () -> document.createCDATASection("$F{class}==null ? \""+NULL_REPLACEMENT+"\" : java.util.ResourceBundle.getBundle($F{class}.getName()).getString(\"class\")"),
                    markupFromFieldName.apply("class"), table, columnWidth, 7, 1, 20, 10);
        }

        ////////////////////////////////////////////////////////////////////////
        // BUILD COLUMNS
        ////////////////////////////////////////////////////////////////////////
        final ResourceBundle rb = ResourceBundle.getBundle(clazz.getName());
        // Si la liste des champs contient les champs à imprimer et non pas à éviter on se base sur l'ordre de la liste pour générer l'ordre des colonnes.
        if(print){
            // Indexation des initialiseurs par les noms de champs.
            final Map<String, Method> settersByFieldName = new HashMap<>();
            for(final Method method : clazz.getMethods()){
                if(PrinterUtilities.isSetter(method)){
                    settersByFieldName.put(getFieldNameFromSetter(method), method);
                }
            }
            for(final String fieldName : fields){
                writeColumn(rb.getString(fieldName), getFromMethodSupplier.apply(settersByFieldName.get(fieldName)), markupFromFieldName.apply(fieldName), table, columnWidth, 7, 1, 20, 10);
            }
        }
        // Sinon on n'a pas d'ordre particulier sur lequel se baser et on parcours donc en premier les méthodes pour trouver les noms des champs à imprimer.
        else{
            for(final Method method : clazz.getMethods()){
                if(PrinterUtilities.isSetter(method)){
                    // Retrives the field name from the setter name.----------------
                    final String fieldName = getFieldNameFromSetter(method);
                    if(printPredicate.test(fieldName)){
                        writeColumn(rb.getString(fieldName), getFromMethodSupplier.apply(method), markupFromFieldName.apply(fieldName), table, columnWidth, 7, 1, 20, 10);
                    }
                }
            }
        }

        componentElement.appendChild(componentElementReportElement);
        componentElement.appendChild(table);

        band.appendChild(componentElement);
        currentY+=height;
    }

    private void writeColumn(final String header, final Supplier<CDATASection> cellSupplier,
            final JRUtils.Markup markup, final Element table, final int columnWidth,
            final int fontSize, final int padding,
            final int headerHeight, final int detailCellHeight){


        final Element column = document.createElementNS(URI_JRXML_COMPONENTS, TAG_COLUMN);
        column.setAttribute(ATT_WIDTH, String.valueOf(columnWidth));

        // Table header and footer
        final Element tableHeader = document.createElementNS(URI_JRXML_COMPONENTS, TAG_TABLE_HEADER);
        tableHeader.setAttribute(ATT_STYLE, "table_TH");
        tableHeader.setAttribute(ATT_HEIGHT, String.valueOf(0));

        final Element tableFooter = document.createElementNS(URI_JRXML_COMPONENTS, TAG_TABLE_FOOTER);
        tableFooter.setAttribute(ATT_STYLE, "table_TH");
        tableFooter.setAttribute(ATT_HEIGHT, String.valueOf(0));

        // Column header
        final Element jrColumnHeader = document.createElementNS(URI_JRXML_COMPONENTS, TAG_COLUMN_HEADER);
        jrColumnHeader.setAttribute(ATT_STYLE, "table_CH");
        jrColumnHeader.setAttribute(ATT_HEIGHT, String.valueOf(headerHeight));

        final Element staticText = document.createElementNS(URI_JRXML, TAG_STATIC_TEXT);

        final Element staticTextReportElement = document.createElementNS(URI_JRXML, TAG_REPORT_ELEMENT);
        staticTextReportElement.setAttribute(ATT_X, String.valueOf(padding));
        staticTextReportElement.setAttribute(ATT_Y, String.valueOf(padding));
        staticTextReportElement.setAttribute(ATT_WIDTH, String.valueOf(columnWidth-2*padding));
        staticTextReportElement.setAttribute(ATT_HEIGHT, String.valueOf(headerHeight-2*padding));
        staticText.appendChild(staticTextReportElement);

        final Element textElement = document.createElement(TAG_TEXT_ELEMENT);
        final Element font = document.createElement(TAG_FONT);
        font.setAttribute(ATT_SIZE, String.valueOf(fontSize));
        textElement.appendChild(font);
        staticText.appendChild(textElement);

        final Element text = document.createElementNS(URI_JRXML, TAG_TEXT);
        final CDATASection labelField = document.createCDATASection(header);
        text.appendChild(labelField);

        staticText.appendChild(text);
        jrColumnHeader.appendChild(staticText);

        // Column footer
        final Element jrColumnFooter = document.createElementNS(URI_JRXML_COMPONENTS, TAG_COLUMN_FOOTER);
        jrColumnFooter.setAttribute(ATT_STYLE, "table_CH");
        jrColumnFooter.setAttribute(ATT_HEIGHT, String.valueOf(0));


        // Detail cell
        final Element detailCell = document.createElementNS(URI_JRXML_COMPONENTS, TAG_DETAIL_CELL);
        detailCell.setAttribute(ATT_STYLE, "table_TD");
        detailCell.setAttribute(ATT_HEIGHT, String.valueOf(detailCellHeight));

        final Element textField = document.createElementNS(URI_JRXML, TAG_TEXT_FIELD);
        textField.setAttribute(ATT_IS_STRETCH_WITH_OVERFLOW, String.valueOf(true));

        final Element textFieldReportElement = document.createElement(TAG_REPORT_ELEMENT);
        textFieldReportElement.setAttribute(ATT_X, String.valueOf(padding));
        textFieldReportElement.setAttribute(ATT_Y, String.valueOf(padding));
        textFieldReportElement.setAttribute(ATT_WIDTH, String.valueOf(columnWidth-2*padding));
        textFieldReportElement.setAttribute(ATT_HEIGHT, String.valueOf(detailCellHeight-2*padding));
        textField.appendChild(textFieldReportElement);

        final Element detailTextElement = document.createElement(TAG_TEXT_ELEMENT);
        final Element detailFont = document.createElement(TAG_FONT);
        detailFont.setAttribute(ATT_SIZE, String.valueOf(fontSize));
        detailTextElement.appendChild(detailFont);
        detailTextElement.setAttribute(ATT_MARKUP, (markup==null ? JRUtils.Markup.NONE : markup).toString());
        textField.appendChild(detailTextElement);

        final Element textFieldExpression = document.createElement(TAG_TEXT_FIELD_EXPRESSION);

        textFieldExpression.appendChild(cellSupplier.get());

        textField.appendChild(textFieldExpression);
        detailCell.appendChild(textField);

        column.appendChild(tableHeader);
        column.appendChild(tableFooter);
        column.appendChild(jrColumnHeader);
        column.appendChild(jrColumnFooter);
        column.appendChild(detailCell);

        table.appendChild(column);
    }

    protected abstract void writeObject();
}
