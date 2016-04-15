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

import static fr.sirs.SIRS.BORNE_DEBUT_AVAL;
import static fr.sirs.SIRS.BORNE_FIN_AVAL;
import fr.sirs.core.model.LabelMapper;
import static fr.sirs.util.JRUtils.ATT_BACKCOLOR;
import static fr.sirs.util.JRUtils.ATT_FONT_NAME;
import static fr.sirs.util.JRUtils.ATT_HEIGHT;
import static fr.sirs.util.JRUtils.ATT_IS_BOLD;
import static fr.sirs.util.JRUtils.ATT_IS_STRETCH_WITH_OVERFLOW;
import static fr.sirs.util.JRUtils.ATT_LINE_COLOR;
import static fr.sirs.util.JRUtils.ATT_LINE_WIDTH;
import static fr.sirs.util.JRUtils.ATT_MARKUP;
import static fr.sirs.util.JRUtils.ATT_MODE;
import static fr.sirs.util.JRUtils.ATT_POSITION_TYPE;
import static fr.sirs.util.JRUtils.ATT_SIZE;
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
import static fr.sirs.util.JRUtils.TAG_FONT;
import static fr.sirs.util.JRUtils.TAG_FRAME;
import static fr.sirs.util.JRUtils.TAG_REPORT_ELEMENT;
import static fr.sirs.util.JRUtils.TAG_STATIC_TEXT;
import static fr.sirs.util.JRUtils.TAG_TEXT;
import static fr.sirs.util.JRUtils.TAG_TEXT_ELEMENT;
import static fr.sirs.util.JRUtils.TAG_TEXT_FIELD;
import static fr.sirs.util.JRUtils.TAG_TEXT_FIELD_EXPRESSION;
import fr.sirs.util.JRUtils.TextAlignment;
import static fr.sirs.util.PrinterUtilities.getFieldNameFromSetter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
public class JRDomWriterElementSheet extends AbstractJDomWriterSingleSheet {
    
    
    // Dynamic template parameters.
    private int fields_interline;
    
    // Static template parameters.
    private static final String FIELDS_VERTICAL_ALIGNMENT = "Middle";
    private static final String FIELDS_FONT_NAME = "Serif";
    private static final int FIELDS_FONT_SIZE = 8;
    private static final int FIELDS_HEIGHT = 10;
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
    
    
    private JRDomWriterElementSheet(){
        super();
        fields_interline = 4;
    }
    
    public JRDomWriterElementSheet(final InputStream stream) throws ParserConfigurationException, SAXException, IOException {
        super(stream);
        fields_interline = 4;
    }
    
    /**
     * This setter changes the default fields interline.
     * @param fieldsInterline 
     */
    public void setFieldsInterline(int fieldsInterline){
        fields_interline = fieldsInterline;
    }
    
    /**
     * <p>This method writes a Jasper Reports template mapping the parameter class.</p>
     * @param classToMap
     * @param avoidFields field names to avoid.
     * @throws TransformerException
     * @throws IOException
     */
    public void write(final Class classToMap, final List<String> avoidFields) throws TransformerException, IOException {
        
        // Remove elements before inserting fields.-----------------------------
        root.removeChild(title);
        root.removeChild(pageHeader);
        root.removeChild(columnHeader);
        root.removeChild(detail);
        
        // Modifies the template, based on the given class.---------------------
        this.writeObject(classToMap, avoidFields);
        
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
     * @param classToMap
     * @param avoidFields field names to avoid.
     * @throws Exception 
     */
    private void writeObject(final Class classToMap, final List<String> avoidFields) {
        
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
        writeTitle("Fiche synoptique de ", classToMap);
        
        // Writes the headers.--------------------------------------------------
        writePageHeader();
        writeColumnHeader();
        
        // Builds the body of the Jasper Reports template.----------------------
        writeDetail(classToMap, avoidFields);
    }
    
    /**
     * <p>This method writes the content of the detail element.</p>
     * @param classToMap
     * @throws Exception 
     */
    private void writeDetail(final Class classToMap, List<String> avoidFields) {
        
        final LabelMapper resourceBundle = LabelMapper.get(classToMap);
        
        // Loops over the method looking for setters (based on the field names).
        final Method[] methods = classToMap.getMethods();
        final Collection<Method> setters = new ArrayList<>();

        for(final Method method : methods){
            if(PrinterUtilities.isSetter(method)){
                setters.add(method);
            }
        }

        final Collection<Method> orderedSetters = new ArrayList<>();
        final Collection<String> prioritaryMethodNames = new ArrayList<>();
        prioritaryMethodNames.add("setDesignation");
        prioritaryMethodNames.add("setLinearId");
        prioritaryMethodNames.add("setSystemeRepId");
        prioritaryMethodNames.add("setPrDebut");
        prioritaryMethodNames.add("setPrFin");
        prioritaryMethodNames.add("setBorneDebutId");
        prioritaryMethodNames.add("setBorne_debut_aval");
        prioritaryMethodNames.add("setBorne_debut_distance");
        prioritaryMethodNames.add("setBorneFinId");
        prioritaryMethodNames.add("setBorne_fin_aval");
        prioritaryMethodNames.add("setBorne_fin_distance");
        prioritaryMethodNames.add("setDate_debut");
        prioritaryMethodNames.add("setDate_fin");
        prioritaryMethodNames.add("setLibelle");
        prioritaryMethodNames.add("setNom");
        prioritaryMethodNames.add("setCommentaire");
        prioritaryMethodNames.add("setDescription");

        for(final String prioritaryName : prioritaryMethodNames){
            final Iterator<Method> it = setters.iterator();
            while(it.hasNext()){
                final Method current = it.next();
                if(current.getName().equals(prioritaryName)){
                    it.remove();
                    orderedSetters.add(current);
                }
            }
        }

        orderedSetters.addAll(setters);

        int i = 0;
        for (final Method method : orderedSetters){
                
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
    private void writeDetailField(final String field, final Class fieldClass, final int order, final Markup markup, final LabelMapper resourceBundle){
        
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
        staticTextFont.setAttribute(ATT_SIZE, String.valueOf(FIELDS_FONT_SIZE));
        
        final Element text = this.document.createElement(TAG_TEXT);
        
        final CDATASection labelField;
        if(resourceBundle != null) {
            labelField = this.document.createCDATASection(resourceBundle.mapPropertyName(field));
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
        if(markup!=null && markup!=Markup.NONE) 
            textFieldTextElement.setAttribute(ATT_MARKUP, markup.toString());
        
        final Element textFieldFont = document.createElement(TAG_FONT);
        textFieldFont.setAttribute(ATT_FONT_NAME, FIELDS_FONT_NAME);
        textFieldFont.setAttribute(ATT_SIZE, String.valueOf(FIELDS_FONT_SIZE));
        
        final Element textFieldExpression = document.createElement(TAG_TEXT_FIELD_EXPRESSION);
        
        // The content of the field is specific in case of Calendar field.------
        final CDATASection valueField;
        //if (c==Instant.class) 
        //    valueField = this.document.createCDATASection("$F{"+field+"}");
        //else $F{permit_quantity}.equals(null) ? $F{fst_insp_qpqlml_quantity} : $F{permit_quantity}
        
        if(fieldClass==Boolean.class || (fieldClass!=null && BOOLEAN_PRIMITIVE_NAME.equals(fieldClass.getName()))){
            if(BORNE_DEBUT_AVAL.equals(field) || BORNE_FIN_AVAL.equals(field)){
                valueField = document.createCDATASection("$F{"+field+"}==null ? \""+NULL_REPLACEMENT+"\" : ($F{"+field+"} ? \"Amont\" : \"Aval\")");
            }
            else{
                valueField = document.createCDATASection("$F{"+field+"}==null ? \""+NULL_REPLACEMENT+"\" : ($F{"+field+"} ? \""+TRUE_REPLACEMENT+"\" : \""+FALSE_REPLACEMENT+"\")");
            }
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
