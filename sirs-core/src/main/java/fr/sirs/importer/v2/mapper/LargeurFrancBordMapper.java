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
package fr.sirs.importer.v2.mapper;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.LargeurFrancBord;
import fr.sirs.core.model.RefLargeurFrancBord;
import fr.sirs.core.model.RefSource;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.ImportContext;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class LargeurFrancBordMapper extends AbstractMapper<LargeurFrancBord> {

    private enum Columns {
        //        id_nom_element, // Redondant avec ID_ELEMENT_GEOMETRIE
        //        ID_SOUS_GROUPE_DONNEES, // Redondant avec le type de données
        //        LIBELLE_TYPE_ELEMENT_GEOMETRIE, // Redondant avec l'importaton des types de géométries
        //        DECALAGE_DEFAUT, // Affichage
        //        DECALAGE, // Affichage
        //        LIBELLE_SOURCE, // Redondant avec l'importation des sources
        //        LIBELLE_SYSTEME_REP, // Redondant avec l'importation des systèmes de repérage
        //        NOM_BORNE_DEBUT, // Redondant avec l'importation des bornes
        //        NOM_BORNE_FIN, // Redondant avec l'importation des bornes
        //        LIBELLE_TYPE_LARGEUR_FB, // Redondant avec l'importation des types de largeur de FB
        //        LIBELLE_TYPE_PROFIL_FB, // Redondant avec l'importation des types de profil de front de FB
        //        LIBELLE_TYPE_DIST_DIGUE_BERGE, // Redondant avec l'importation des distances digue/berge
        //        ID_TYPE_ELEMENT_GEOMETRIE,
        ID_SOURCE,
        ID_TYPE_LARGEUR_FB,
//        ID_TYPE_PROFIL_FB, // Ne concerne pas cette table
//        ID_TYPE_DIST_DIGUE_BERGE, // Pas dans le modèle actuel
//        ID_AUTO
    }

    public LargeurFrancBordMapper(Table table) {
        super(table);
    }

    @Override
    public void map(Row input, LargeurFrancBord output) throws IllegalStateException, IOException, AccessDbImporterException {
        final Object sourceId = input.get(Columns.ID_SOURCE.toString());
        if (sourceId != null) {
            output.setSourceId(context.importers.get(RefSource.class).getImportedId(sourceId));
        }

        final Object typeLargeurId = input.get(Columns.ID_TYPE_LARGEUR_FB.toString());
        if (typeLargeurId != null) {
            output.setTypeLargeurFrancBord(context.importers.get(RefLargeurFrancBord.class).getImportedId(typeLargeurId));
        }
    }

    @Component
    public static class Spi implements MapperSpi<LargeurFrancBord> {

        @Override
        public Optional<Mapper<LargeurFrancBord>> configureInput(Table inputType) throws IllegalStateException {
            if (!ImportContext.columnExists(inputType, Columns.ID_TYPE_LARGEUR_FB.name())
                    || !ImportContext.columnExists(inputType, Columns.ID_SOURCE.name())) {
                return Optional.empty();
            }
            return Optional.of(new LargeurFrancBordMapper(inputType));
        }

        @Override
        public Class<LargeurFrancBord> getOutputClass() {
            return LargeurFrancBord.class;
        }
    }
}
