package fr.sirs.util;

import static fr.sirs.util.JRDomWriterDesordreSheet.PHOTOS_SUBREPORT;
import static fr.sirs.util.JRDomWriterDesordreSheet.PHOTO_DATA_SOURCE;
import static fr.sirs.util.JRUtils.ATT_HEIGHT;
import static fr.sirs.util.JRUtils.ATT_POSITION_TYPE;
import static fr.sirs.util.JRUtils.ATT_WIDTH;
import static fr.sirs.util.JRUtils.ATT_X;
import static fr.sirs.util.JRUtils.ATT_Y;
import static fr.sirs.util.JRUtils.TAG_BAND;
import static fr.sirs.util.JRUtils.TAG_DATA_SOURCE_EXPRESSION;
import static fr.sirs.util.JRUtils.TAG_REPORT_ELEMENT;
import static fr.sirs.util.JRUtils.TAG_SUBREPORT;
import static fr.sirs.util.JRUtils.TAG_SUBREPORT_EXPRESSION;
import static fr.sirs.util.JRUtils.URI_JRXML;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public abstract class AbstractJDomWriterSingleSpecificSheetWithPhotoReport<T extends fr.sirs.core.model.Element> extends AbstractJDomWriterSingleSpecificSheet<T> {

    public AbstractJDomWriterSingleSpecificSheetWithPhotoReport(final Class<T> classToMap) {
        super(classToMap);
    }

    public AbstractJDomWriterSingleSpecificSheetWithPhotoReport(final Class<T> classToMap,
            final InputStream stream, final List<String> avoidFields)
            throws ParserConfigurationException, SAXException, IOException{
        super(classToMap, stream, avoidFields);
    }

    protected void includePhotoSubreport(final int height){

        final Element band = (Element) detail.getElementsByTagName(TAG_BAND).item(0);

        final Element subReport = document.createElement(TAG_SUBREPORT);
        final Element reportElement = document.createElement(TAG_REPORT_ELEMENT);
        reportElement.setAttribute(ATT_X, String.valueOf(0));
        reportElement.setAttribute(ATT_Y, String.valueOf(currentY));
        reportElement.setAttribute(ATT_WIDTH, String.valueOf(802));
        reportElement.setAttribute(ATT_HEIGHT, String.valueOf(height));
        reportElement.setAttribute(ATT_POSITION_TYPE, JRUtils.PositionType.FLOAT.toString());
        subReport.appendChild(reportElement);

        final Element datasourceExpression = document.createElementNS(URI_JRXML, TAG_DATA_SOURCE_EXPRESSION);

        final CDATASection datasourceExpressionField = document.createCDATASection("(("+ObjectDataSource.class.getCanonicalName()+") $F{"+PHOTO_DATA_SOURCE+"})");

        datasourceExpression.appendChild(datasourceExpressionField);
        subReport.appendChild(datasourceExpression);

        final Element subreportExpression = document.createElementNS(URI_JRXML, TAG_SUBREPORT_EXPRESSION);
        final CDATASection subreportExpressionField = document.createCDATASection("$P{"+PHOTOS_SUBREPORT+"}");

        subreportExpression.appendChild(subreportExpressionField);
        subReport.appendChild(subreportExpression);

        band.appendChild(subReport);
        currentY+=height;
    }
}
