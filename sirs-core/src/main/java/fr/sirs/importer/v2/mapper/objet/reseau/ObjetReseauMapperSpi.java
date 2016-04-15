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
package fr.sirs.importer.v2.mapper.objet.reseau;

import org.springframework.stereotype.Component;

import fr.sirs.core.model.ObjetReseau;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ObjetReseauMapperSpi extends GenericMapperSpi<ObjetReseau> {

    enum Columns {
//        ID_ELEMENT_RESEAU,
        //        id_nom_element,
        //        ID_SOUS_GROUPE_DONNEES,
        //        LIBELLE_TYPE_ELEMENT_RESEAU,
        //        DECALAGE_DEFAUT,
        //        DECALAGE,
        //        LIBELLE_SOURCE,
        //        LIBELLE_TYPE_COTE,
        //        LIBELLE_SYSTEME_REP,
        //        NOM_BORNE_DEBUT,
        //        NOM_BORNE_FIN,
        //        LIBELLE_ECOULEMENT,
        //        LIBELLE_IMPLANTATION,
        //        LIBELLE_UTILISATION_CONDUITE,
        //        LIBELLE_TYPE_CONDUITE_FERMEE,
        //        LIBELLE_TYPE_OUVR_HYDRAU_ASSOCIE,
        //        LIBELLE_TYPE_RESEAU_COMMUNICATION,
        //        LIBELLE_TYPE_VOIE_SUR_DIGUE,
        //        NOM_OUVRAGE_VOIRIE,
        //        LIBELLE_TYPE_POSITION,
        //        LIBELLE_TYPE_OUVRAGE_VOIRIE,
        //        LIBELLE_TYPE_RESEAU_EAU,
        //        LIBELLE_TYPE_REVETEMENT,
        //        LIBELLE_TYPE_USAGE_VOIE,

        NOM,
        //                ID_TYPE_ELEMENT_RESEAU,
        ID_TYPE_COTE,
        ID_SOURCE,
        N_SECTEUR,
        ID_ECOULEMENT,
        ID_IMPLANTATION,
        ID_UTILISATION_CONDUITE,
        ID_TYPE_CONDUITE_FERMEE,
        AUTORISE,
        ID_TYPE_OUVR_HYDRAU_ASSOCIE,
        ID_TYPE_RESEAU_COMMUNICATION,
        ID_OUVRAGE_COMM_NRJ,
        ID_TYPE_VOIE_SUR_DIGUE,
        //        ID_OUVRAGE_VOIRIE,
        ID_TYPE_REVETEMENT,
        ID_TYPE_USAGE_VOIE,
        ID_TYPE_POSITION,
        LARGEUR,
        ID_TYPE_OUVRAGE_VOIRIE,
        ID_TYPE_OUVRAGE_PARTICULIER,
        HAUTEUR,
        DIAMETRE,
        ID_TYPE_RESEAU_EAU,
        ID_TYPE_NATURE,
        //        LIBELLE_TYPE_NATURE,
        //        ID_TYPE_NATURE_HAUT,
        //        LIBELLE_TYPE_NATURE_HAUT,
        //        ID_TYPE_NATURE_BAS,
        //        LIBELLE_TYPE_NATURE_BAS,
        ID_TYPE_REVETEMENT_HAUT,
        //        LIBELLE_TYPE_REVETEMENT_HAUT,
        ID_TYPE_REVETEMENT_BAS,
//        LIBELLE_TYPE_REVETEMENT_BAS,
//        ID_AUTO
    }

    private final HashMap<String, String> bindings = new HashMap<>();

    public ObjetReseauMapperSpi() throws IntrospectionException {
        super(ObjetReseau.class);

        bindings.put(Columns.NOM.name(), "libelle");
        bindings.put(Columns.ID_TYPE_COTE.name(), "coteId");
        bindings.put(Columns.ID_SOURCE.name(), "sourceId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
