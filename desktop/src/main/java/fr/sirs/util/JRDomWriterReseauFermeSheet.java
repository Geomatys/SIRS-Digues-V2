
package fr.sirs.util;

import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.Observation;
import fr.sirs.core.model.ObservationReseauHydrauliqueFerme;
import fr.sirs.core.model.Photo;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import static fr.sirs.util.JRUtils.ATT_HEIGHT;
import static fr.sirs.util.JRUtils.ATT_POSITION_TYPE;
import static fr.sirs.util.JRUtils.ATT_WIDTH;
import static fr.sirs.util.JRUtils.ATT_X;
import static fr.sirs.util.JRUtils.ATT_Y;
import fr.sirs.util.JRUtils.PositionType;
import static fr.sirs.util.JRUtils.TAG_BAND;
import static fr.sirs.util.JRUtils.TAG_DATA_SOURCE_EXPRESSION;
import static fr.sirs.util.JRUtils.TAG_REPORT_ELEMENT;
import static fr.sirs.util.JRUtils.TAG_SUBREPORT;
import static fr.sirs.util.JRUtils.TAG_SUBREPORT_EXPRESSION;
import static fr.sirs.util.JRUtils.TAG_SUB_DATASET;
import static fr.sirs.util.JRUtils.URI_JRXML;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class JRDomWriterReseauFermeSheet extends AbstractJDomWriterSingleSpecificSheet<ReseauHydrauliqueFerme> {
    
    public static final String OBSERVATION_DATASET = "Observation Dataset";
    public static final String OBSERVATION_TABLE_DATA_SOURCE = "OBSERVATION_TABLE_DATA_SOURCE";
    
    public static final String RESEAU_OUVRAGE_DATASET = "ReseauOuvrage Dataset";
    public static final String RESEAU_OUVRAGE_TABLE_DATA_SOURCE = "RESEAU_OUVRAGE_TABLE_DATA_SOURCE";
    
    public static final String PHOTO_DATA_SOURCE = "PHOTO_DATA_SOURCE";
    public static final String PHOTOS_SUBREPORT = "PHOTO_SUBREPORT";
    
    private final List<String> observationFields;
    private final List<String> reseauFields;
    
    private final boolean printPhoto;
    private final boolean printReseauOuvrage;
    
    private JRDomWriterReseauFermeSheet(){
        super();
        
        observationFields = null;
        reseauFields = null;
        printPhoto = printReseauOuvrage = true;
    }
    
    public JRDomWriterReseauFermeSheet(final InputStream stream, 
            final List<String> avoidFields,
            final List<String> observationFields,
            final List<String> reseauFields,
            final boolean printPhoto, 
            final boolean printReseauOuvrage) throws ParserConfigurationException, SAXException, IOException {
        super(stream, avoidFields);
        
        this.observationFields = observationFields;
        this.reseauFields = reseauFields;
        this.printPhoto = printPhoto;
        this.printReseauOuvrage = printReseauOuvrage;
    }

    /**
     * <p>This method modifies the body of the DOM.</p>
     * @param candidate
     */
    @Override
    protected void writeObject(final ReseauHydrauliqueFerme candidate) {
        
        writeSubDataset(ObservationReseauHydrauliqueFerme.class, observationFields, true, (Element) root.getElementsByTagName(TAG_SUB_DATASET).item(0));
        writeSubDataset(ObjetReseau.class, reseauFields, true, (Element) root.getElementsByTagName(TAG_SUB_DATASET).item(1));
        
        // Sets the initial fields used by the template.------------------------
        writeFields(ReseauHydrauliqueFerme.class);
        
        // Modifies the title block.--------------------------------------------
        writeTitle(ReseauHydrauliqueFerme.class);
        
        // Writes the headers.--------------------------------------------------
        writePageHeader();
        writeColumnHeader();
        
        // Builds the body of the Jasper Reports template.----------------------
        writeDetail(candidate);
    }
    
    /**
     * <p>This method writes the content of the detail element.</p>
     * @param classToMap
     * @throws Exception 
     */
    private void writeDetail(final ReseauHydrauliqueFerme candidate) {
        
        final Element band = (Element) detail.getElementsByTagName(TAG_BAND).item(0);
        currentY = Integer.valueOf(band.getAttribute(ATT_HEIGHT));
        
        /*----------------------------------------------------------------------
        TABLEAU DES OBSERVATIONS
        ----------------------------------------------------------------------*/
        if(candidate.getObservations()!=null && !candidate.getObservations().isEmpty()){
            currentY+=2;
            writeSectionTitle("Observations", 15, 1, 10, 9);
            currentY+=2;
            writeTable(Observation.class, observationFields, true, OBSERVATION_TABLE_DATA_SOURCE, OBSERVATION_DATASET, 30);
            currentY+=2;
        }
        
        /*----------------------------------------------------------------------
        SOUS-RAPPORTS DES PHOTOS
        ----------------------------------------------------------------------*/
        if(printPhoto){
            
            // On sait que les photos qui seront fournies par le datasource seront les photos des observations du désordre courant
            final List<Photo> photos = new ArrayList<>();
            for(final ObservationReseauHydrauliqueFerme observation : candidate.getObservations()){
                final List<Photo> obsPhotos = observation.getPhotos();
                if(obsPhotos!=null && !obsPhotos.isEmpty()){
                    photos.addAll(obsPhotos);
                }
            }
            if(candidate.getPhotos()!=null && !candidate.getPhotos().isEmpty()){
                photos.addAll(candidate.getPhotos());
            }
            if(!photos.isEmpty()){
                currentY+=2;
                includePhotoSubreport(64);
            }
        }
        
        /*----------------------------------------------------------------------
        TABLEAU DES OUVRAGES ET RÉSEAUX
        ----------------------------------------------------------------------*/
        final int nbReseauOuvrage = candidate.getOuvrageHydrauliqueAssocieIds().size()
                + candidate.getReseauHydrauliqueCielOuvertIds().size()
                + candidate.getStationPompageIds().size();
        if(printReseauOuvrage && nbReseauOuvrage>0){
            currentY+=2;
            writeSectionTitle("Réseaux et ouvrages", 15, 1, 10, 9);
            currentY+=2;
            writeTable(ObjetReseau.class, reseauFields, true, RESEAU_OUVRAGE_TABLE_DATA_SOURCE, RESEAU_OUVRAGE_DATASET, 30);
            currentY+=2;
        }
        
        // Sizes the detail element given to the field number.------------------
        band.setAttribute(ATT_HEIGHT, String.valueOf(currentY));
        
        // Builds the DOM tree.-------------------------------------------------
        root.appendChild(detail);
    }
    
    private void includePhotoSubreport(final int height){
        
        final Element band = (Element) detail.getElementsByTagName(TAG_BAND).item(0);
        
        final Element subReport = document.createElement(TAG_SUBREPORT);
        final Element reportElement = document.createElement(TAG_REPORT_ELEMENT);
        reportElement.setAttribute(ATT_X, String.valueOf(0));
        reportElement.setAttribute(ATT_Y, String.valueOf(currentY));
        reportElement.setAttribute(ATT_WIDTH, String.valueOf(802));
        reportElement.setAttribute(ATT_HEIGHT, String.valueOf(height));
        reportElement.setAttribute(ATT_POSITION_TYPE, PositionType.FLOAT.toString());
        subReport.appendChild(reportElement);
        
        final Element datasourceExpression = document.createElementNS(URI_JRXML, TAG_DATA_SOURCE_EXPRESSION);
        
        final CDATASection datasourceExpressionField = document.createCDATASection("(("+ObjectDataSource.class.getCanonicalName()+") $P{"+PHOTO_DATA_SOURCE+"})");
        
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
