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

import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.Observation;
import fr.sirs.core.model.Prestation;
import static fr.sirs.util.JRUtils.ATT_HEIGHT;
import static fr.sirs.util.JRUtils.TAG_BAND;
import static fr.sirs.util.JRUtils.TAG_SUB_DATASET;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class JRDomWriterDesordreSheet extends AbstractJDomWriterSingleSpecificSheetWithPhotoReport<Desordre> {
    
    public static final String OBSERVATION_DATASET = "Observation Dataset";
    public static final String OBSERVATION_TABLE_DATA_SOURCE = "OBSERVATION_TABLE_DATA_SOURCE";
    public static final String PRESTATION_DATASET = "Prestation Dataset";
    public static final String PRESTATION_TABLE_DATA_SOURCE = "PRESTATION_TABLE_DATA_SOURCE";
    
    public static final String RESEAU_OUVRAGE_DATASET = "ReseauOuvrage Dataset";
    public static final String RESEAU_OUVRAGE_TABLE_DATA_SOURCE = "RESEAU_OUVRAGE_TABLE_DATA_SOURCE";
    public static final String VOIRIE_DATASET = "Voirie Dataset";
    public static final String VOIRIE_TABLE_DATA_SOURCE = "VOIRIE_TABLE_DATA_SOURCE";
    
    public static final String PHOTO_DATA_SOURCE = "PHOTO_DATA_SOURCE";
    public static final String PHOTOS_SUBREPORT = "PHOTO_SUBREPORT";
    
    private final List<String> observationFields;
    private final float[] observationWidths;
    private final List<String> prestationFields;
    private final float[] prestationWidths;
    private final List<String> reseauFields;
    
    private final boolean printPhoto;
    private final boolean printReseauOuvrage;
    private final boolean printVoirie;
    
    private JRDomWriterDesordreSheet(final Class<Desordre> classToMap){
        super(classToMap);
        
        observationFields = null;
        observationWidths = null;
        prestationFields = null;
        prestationWidths = null;
        reseauFields = null;
        printPhoto = printReseauOuvrage = printVoirie = true;
    }
    
    public JRDomWriterDesordreSheet(final InputStream stream,
            final List<String> avoidFields,
            final List<String> observationFields,
            final float[] observationWidths,
            final List<String> prestationFields,
            final float[] prestationWidths,
            final List<String> reseauFields,
            final boolean printPhoto, 
            final boolean printReseauOuvrage, 
            final boolean printVoirie) throws ParserConfigurationException, SAXException, IOException {
        super(Desordre.class, stream, avoidFields, "#47daff");
        
        this.observationFields = observationFields;
        this.observationWidths = observationWidths;
        this.prestationFields = prestationFields;
        this.prestationWidths = prestationWidths;
        this.reseauFields = reseauFields;
        this.printPhoto = printPhoto;
        this.printReseauOuvrage = printReseauOuvrage;
        this.printVoirie = printVoirie;
    }

    /**
     * <p>This method modifies the body of the DOM.</p>
     */
    @Override
    protected void writeObject() {
        
        writeSubDataset(Observation.class, observationFields, true, (Element) root.getElementsByTagName(TAG_SUB_DATASET).item(0));
        writeSubDataset(Prestation.class, prestationFields, true, (Element) root.getElementsByTagName(TAG_SUB_DATASET).item(1));
        writeSubDataset(ObjetReseau.class, reseauFields, true, (Element) root.getElementsByTagName(TAG_SUB_DATASET).item(2));
        writeSubDataset(ObjetReseau.class, reseauFields, true, (Element) root.getElementsByTagName(TAG_SUB_DATASET).item(3));
        
        
        // Sets the initial fields used by the template.------------------------
        writeFields();
        writeField(ObjectDataSource.class, PHOTO_DATA_SOURCE, "Source de données des photos");
        writeField(ObjectDataSource.class, OBSERVATION_TABLE_DATA_SOURCE, "Source de données des observations");
        writeField(ObjectDataSource.class, PRESTATION_TABLE_DATA_SOURCE, "Source de données des prestations");
        writeField(ObjectDataSource.class, RESEAU_OUVRAGE_TABLE_DATA_SOURCE, "Source de données des réseaux");
        writeField(ObjectDataSource.class, VOIRIE_TABLE_DATA_SOURCE, "Source de données des voiries");

        // Modifies the title block.--------------------------------------------
        writeTitle();
        
        // Writes the headers.--------------------------------------------------
        writePageHeader();
        writeColumnHeader();
        
        // Builds the body of the Jasper Reports template.----------------------
        writeDetail();

        // Writes the footers
        writeColumnFooter();
        writePageFooter();
    }
    
    /**
     * <p>This method writes the content of the detail element.</p>
     * @param classToMap
     * @throws Exception 
     */
    private void writeDetail() {
        
        final Element band = (Element) detail.getElementsByTagName(TAG_BAND).item(0);
        currentY = Integer.valueOf(band.getAttribute(ATT_HEIGHT));
        
        /*----------------------------------------------------------------------
        TABLEAU DES OBSERVATIONS
        ----------------------------------------------------------------------*/
        currentY+=2;
        writeSectionTitle("Observations", 14, 1, 10, 9, true, false, false);
        currentY+=2;
        writeTable(Observation.class, observationFields, true, OBSERVATION_TABLE_DATA_SOURCE, OBSERVATION_DATASET, 30, observationWidths);
        currentY+=2;
        
        /*----------------------------------------------------------------------
        TABLEAU DES PRESTATIONS
        ----------------------------------------------------------------------*/
        currentY+=2;
        writeSectionTitle("Prestations", 14, 1, 10, 9, true, false, false);
        currentY+=2;
        writeTable(Prestation.class, prestationFields, true, PRESTATION_TABLE_DATA_SOURCE, PRESTATION_DATASET, 30, prestationWidths);
        currentY+=2;
        
        /*----------------------------------------------------------------------
        SOUS-RAPPORTS DES PHOTOS
        ----------------------------------------------------------------------*/
        if(printPhoto){
            currentY+=2;
            includePhotoSubreport(64);
        }
        
        /*----------------------------------------------------------------------
        TABLEAU DES OUVRAGES ET RÉSEAUX
        ----------------------------------------------------------------------*/
        if(printReseauOuvrage){
            currentY+=2;
            writeSectionTitle("Réseaux et ouvrages", 14, 1, 10, 9, true, false, false);
            currentY+=2;
            writeTable(ObjetReseau.class, reseauFields, true, RESEAU_OUVRAGE_TABLE_DATA_SOURCE, RESEAU_OUVRAGE_DATASET, 30, null);
            currentY+=2;
        }
        
        /*----------------------------------------------------------------------
        TABLEAU DES VOIRIES
        ----------------------------------------------------------------------*/
        if(printVoirie){
            currentY+=2;
            writeSectionTitle("Voiries", 14, 1, 10, 9, true, false, false);
            currentY+=2;
            writeTable(ObjetReseau.class, reseauFields, true, VOIRIE_TABLE_DATA_SOURCE, VOIRIE_DATASET, 30, null);
            currentY+=2;
        }
        
        writeDetailPageBreak();
        
        // Sizes the detail element given to the field number.------------------
        band.setAttribute(ATT_HEIGHT, String.valueOf(currentY));
        
        // Builds the DOM tree.-------------------------------------------------
        root.appendChild(detail);
    }
}
