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

import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.Observation;
import fr.sirs.core.model.Prestation;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static fr.sirs.util.JRUtils.ATT_HEIGHT;
import static fr.sirs.util.JRUtils.TAG_BAND;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class JRDomWriterPrestationSyntheseSheet extends AbstractJDomWriterSingleSpecificSheetWithPhotoReport<Prestation> {
    public static final String PRESTATION_DATASET = "Prestation Dataset";
    public static final String PRESTATION_TABLE_DATA_SOURCE = "PRESTATION_TABLE_DATA_SOURCE";
    public static final String IMAGE_DATA_SOURCE = "IMAGE_DATA_SOURCE";

    private final List<JRColumnParameter> prestationFields;

    private JRDomWriterPrestationSyntheseSheet(final Class<Prestation> classToMap){
        super(classToMap);

        prestationFields = null;
    }

    public JRDomWriterPrestationSyntheseSheet(final InputStream stream,
                                              final List<String> avoidFields,
                                              final List<JRColumnParameter> prestationFields)
            throws ParserConfigurationException, SAXException, IOException {
        super(Prestation.class, stream, avoidFields, "#47daff");

        this.prestationFields = prestationFields;
    }

    /**
     * <p>This method modifies the body of the DOM.</p>
     */
    @Override
    protected void writeObject() {

        writeSubDataset(Prestation.class, prestationFields, true, 1);

        // Sets the initial fields used by the template.------------------------
        writeFields();
        writeField(String.class, SirsCore.DIGUE_ID_FIELD, "Champ ajouté de force pour prendre en compte l'intitulé de la digue.");// Ajout d'un champ pour l'intitulé de la digue.
        writeField(ObjectDataSource.class, PRESTATION_TABLE_DATA_SOURCE, "Source de données des prestations");
        writeField(Image.class, IMAGE_DATA_SOURCE, "Image de l'élément");

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
     * @throws Exception
     */
    private void writeDetail() {

        final Element band = (Element) detail.getElementsByTagName(TAG_BAND).item(0);
        currentY = Integer.valueOf(band.getAttribute(ATT_HEIGHT));

        /*----------------------------------------------------------------------
        TABLEAU DES PRESTATIONS
        ----------------------------------------------------------------------*/
        currentY+=24;
        writeSectionTitle("Tableau de synthèse  prestation pour Registre horodaté", TITLE_SECTION_BG_HEIGHT, TITLE_SECTION_MARGIN_V, TITLE_SECTION_INDENT, TITLE_SECTION_FONT_SIZE, true, false, false);
        currentY+=2;
        writeTable(Prestation.class, prestationFields, true, PRESTATION_TABLE_DATA_SOURCE, PRESTATION_DATASET,
                TABLE_HEIGHT, TABLE_FONT_SIZE, TABLE_HEADER_HEIGHT, TABLE_CELL_HEIGHT, TABLE_FILL_WIDTH);

//        writeDetailPageBreak();

        // Sizes the detail element given to the field number.------------------
        band.setAttribute(ATT_HEIGHT, String.valueOf(DETAIL_HEIGHT));

        // Builds the DOM tree.-------------------------------------------------
        root.appendChild(detail);
    }
}
