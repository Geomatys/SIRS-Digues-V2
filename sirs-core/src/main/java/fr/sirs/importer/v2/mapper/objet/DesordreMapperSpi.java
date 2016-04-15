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
package fr.sirs.importer.v2.mapper.objet;

import fr.sirs.core.model.Desordre;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class DesordreMapperSpi extends GenericMapperSpi<Desordre> {

    private enum Columns {

        //            id_nom_element,// Aucun intéret
        //            ID_SOUS_GROUPE_DONNEES,// Aucun intéret
        //            LIBELLE_SOUS_GROUPE_DONNEES,// Aucun intéret
        ID_TYPE_DESORDRE,
        //            LIBELLE_TYPE_DESORDRE,// Dans TypeDesordreImporter
        //            DECALAGE_DEFAUT, // Info d'affichage
        //            DECALAGE, // Info d'affichage
        //            LIBELLE_SOURCE, // Dans TypeSourceImporter
        //            LIBELLE_TYPE_COTE, // Dans TypeCoteImporter
        //            LIBELLE_SYSTEME_REP, // Dans SystemeRepImporter
        //            NOM_BORNE_DEBUT, //Dans BorneImporter
        //            NOM_BORNE_FIN, //Dans BorneImporter
        //            DISPARU_OUI_NON,
        //            DEJA_OBSERVE_OUI_NON,
        //            LIBELLE_TYPE_POSITION,// Dans typePositionImporter
        ID_TYPE_COTE,
        ID_TYPE_POSITION,
        ID_SOURCE,
        LIEU_DIT_DESORDRE
        //            ID_AUTO

        //Empty fields
        //     ID_PRESTATION, // obsolète ? voir table DESORDRE_PRESTATION
        //     LIBELLE_PRESTATION, // Dans l'importateur de prestations
    }

    private final HashMap<String, String> bindings = new HashMap<>();

    public DesordreMapperSpi() throws IntrospectionException {
        super(Desordre.class);

        bindings.put(Columns.ID_TYPE_DESORDRE.name(), "typeDesordreId");
        bindings.put(Columns.ID_TYPE_COTE.name(), "coteId");
        bindings.put(Columns.ID_TYPE_POSITION.name(), "positionId");
        bindings.put(Columns.ID_SOURCE.name(), "sourceId");
        bindings.put(Columns.LIEU_DIT_DESORDRE.name(), "lieuDit");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
