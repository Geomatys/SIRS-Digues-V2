package fr.sirs.util;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class JRUtils {
    
    // Jasper Reports attributes.
    public static final String URI_JRXML = "http://jasperreports.sourceforge.net/jasperreports";
    public static final String URI_JRXML_COMPONENTS = "http://jasperreports.sourceforge.net/jasperreports/components";
    public static final String PREFIX_JRXML_COMPONENTS = "jr";
    public static final String SCHEMA_LOCATION_JRXML_COMPONENTS = "http://jasperreports.sourceforge.net/xsd/components.xsd";
    public static final String URI_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String PREFIX_XSI = "xsi";
    public static final String ATT_XSI_SCHEMA_LOCATION = "schemaLocation";
    
    // Jasper Reports tags.
    public static final String TAG_BAND = "band";
    public static final String TAG_BOTTOM_MARGIN = "bottomMargin";
    public static final String TAG_BOTTOM_PEN = "bottomPen";
    public static final String TAG_BOX = "box";
    public static final String TAG_COLUMN = "column";
    public static final String TAG_COLUMN_FOOTER = "columnFooter";
    public static final String TAG_COLUMN_HEADER = "columnHeader";
    public static final String TAG_COLUMN_WIDTH = "columnWidth";
    public static final String TAG_COMPONENT_ELEMENT = "componentElement";
    public static final String TAG_DETAIL = "detail";
    public static final String TAG_DETAIL_CELL = "detailCell";
    public static final String TAG_DATASET_PARAMETER = "datasetParameter";
    public static final String TAG_DATASET_PARAMETER_EXPRESSION = "datasetParameterExpression";
    public static final String TAG_DATASET_RUN = "datasetRun";
    public static final String TAG_DATA_SOURCE_EXPRESSION = "dataSourceExpression";
    public static final String TAG_FIELD = "field";
    public static final String TAG_FIELD_DESCRIPTION = "fieldDescription";
    public static final String TAG_FONT = "font";
    public static final String TAG_FRAME = "frame";
    public static final String TAG_JASPER_REPORT = "jasperReport";
    public static final String TAG_LAST_PAGE_FOOTER = "lastPageFooter";
    public static final String TAG_LEFT_MARGIN = "leftMargin";
    public static final String TAG_PAGE_FOOTER = "pageFooter";
    public static final String TAG_PAGE_HEADER = "pageHeader";
    public static final String TAG_PAGE_HEIGHT = "pageHeight";
    public static final String TAG_PAGE_WIDTH = "pageWidth";
    public static final String TAG_PATTERN = "pattern";
    public static final String TAG_REPORT_ELEMENT = "reportElement";
    public static final String TAG_RIGHT_MARGIN = "rightMargin";
    public static final String TAG_STATIC_TEXT = "staticText";
    public static final String TAG_STYLE = "style";
    public static final String TAG_SUB_DATASET = "subDataset";
    public static final String TAG_TABLE = "table";
    public static final String TAG_TABLE_HEADER = "tableHeader";
    public static final String TAG_TABLE_FOOTER = "tableFooter";
    public static final String TAG_TEXT = "text";
    public static final String TAG_TEXT_ELEMENT = "textElement";
    public static final String TAG_TEXT_FIELD = "textField";
    public static final String TAG_TEXT_FIELD_EXPRESSION = "textFieldExpression";
    public static final String TAG_TITLE = "title";
    public static final String TAG_TOP_MARGIN = "topMargin";
    
    public static final String BOOLEAN_PRIMITIVE_NAME = "boolean";
    public static final String FLOAT_PRIMITIVE_NAME = "float";
    public static final String DOUBLE_PRIMITIVE_NAME = "double";
    public static final String INTEGER_PRIMITIVE_NAME = "int";
    public static final String LONG_PRIMITIVE_NAME = "long";
    
    public static final String BOOLEAN_CANONICAL_NAME = "java.lang.Boolean";
    public static final String FLOAT_CANONICAL_NAME = "java.lang.Float";
    public static final String DOUBLE_CANONICAL_NAME = "java.lang.Double";
    public static final String INTEGER_CANONICAL_NAME = "java.lang.Integer";
    public static final String LONG_CANONICAL_NAME = "java.lang.Long";
    
    // Jasper Reports attributes.
    public static final String ATT_MODE = "mode";
    public enum Mode {
        OPAQUE("Opaque"), TRANSPARENT("Transparent");
        private final String mode;
        private Mode(final String mode){this.mode=mode;}
        
        @Override
        public String toString(){return mode;}
    };
    
    public static final String ATT_LINE_WIDTH = "lineWidth";
    public static final String ATT_LINE_COLOR = "lineColor";
    public static final String ATT_BACKCOLOR = "backcolor";
    public static final String ATT_NAME = "name";
    
    public static final String ATT_TEXT_ALIGNMENT = "textAlignment";
    public enum TextAlignment {
        LEFT("Left"), CENTER("Center"), RIGHT("Right"), JUSTIFIED("Justified");
        private final String textAlignment;
        private TextAlignment(final String textAlignment){this.textAlignment=textAlignment;}
        
        @Override
        public String toString(){return textAlignment;}
    }; 
    
    public static final String ATT_VERTICAL_ALIGNMENT = "verticalAlignment";
    public static final String ATT_FONT_NAME = "fontName";
    public static final String ATT_IS_BOLD = "isBold";
    public static final String ATT_LANGUAGE = "language";
    public static final String ATT_PAGE_WIDTH = "pageWidth";
    public static final String ATT_PAGE_HEIGHT = "pageHeight";
    public static final String ATT_COLUMN_WIDTH = "columnWidth";
    public static final String ATT_LEFT_MARGIN = "leftMargin";
    public static final String ATT_RIGHT_MARGIN = "rightMargin";
    public static final String ATT_TOP_MARGIN = "topMargin";
    public static final String ATT_BOTTOM_MARGIN = "bottomMargin";
    public static final String ATT_UUID = "uuid";
    public static final String ATT_CLASS = "class";
    public static final String ATT_HEIGHT = "height";
    public static final String ATT_STYLE = "style";
    public static final String ATT_X = "x";
    public static final String ATT_Y = "y";
    public static final String ATT_WIDTH = "width";
    public static final String ATT_IS_STRETCH_WITH_OVERFLOW = "isStretchWithOverflow";
    public static final String ATT_IS_BLANK_WHEN_NULL = "isBlankWhenNull";
    public static final String ATT_POSITION_TYPE = "positionType";
    public static final String ATT_SUB_DATASET = "subDataset";
    
    public static final String ATT_KEY = "key";
    public enum PositionType {
        FLOAT("Float"), FIX_RELATIVE_TO_TOP("FixRelativeToTop"), FIX_RELATIVE_TO_BOTTOM("FixRelativeToBottom");
        private final String positionType;
        private PositionType(final String positionType){this.positionType=positionType;}
        
        @Override
        public String toString(){return positionType;}
    }; 
    
    public static final String ATT_STRETCH_TYPE = "stretchType";
    public enum StretchType {
        NO_STRETCH("NoStretch"), RELATIVE_TO_TALLEST_OBJECT("RelativeToTallestObject"), RELATIVE_TO_BAND_HEIGHT("RelativeToBandHeight");
        private final String stretchType;
        private StretchType(final String stretchType){this.stretchType=stretchType;}
        
        @Override
        public String toString(){return stretchType;}
    };
    
    public static final String ATT_MARKUP = "markup";
    public enum Markup {
        NONE("none"), STYLED("styled"), HTML("html"), RTF("rtf");
        private final String markup;
        private Markup(final String markup){this.markup=markup;}
        
        @Override
        public String toString(){return markup;}
    }; 
}
