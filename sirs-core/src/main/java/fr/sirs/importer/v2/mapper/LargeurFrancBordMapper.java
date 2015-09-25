/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.mapper;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.LargeurFrancBord;
import fr.sirs.core.model.RefLargeurFrancBord;
import fr.sirs.core.model.RefSource;
import fr.sirs.importer.AccessDbImporterException;
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
        final Integer sourceId = input.getInt(Columns.ID_SOURCE.toString());
        if (sourceId != null) {
            output.setSourceId(context.importers.get(RefSource.class).getImportedId(sourceId));
        }

        final Integer typeLargeurId = input.getInt(Columns.ID_TYPE_LARGEUR_FB.toString());
        if (typeLargeurId != null) {
            output.setTypeLargeurFrancBord(context.importers.get(RefLargeurFrancBord.class).getImportedId(typeLargeurId));
        }
    }

    @Component
    public static class Spi implements MapperSpi<LargeurFrancBord> {

        @Override
        public Optional<Mapper<LargeurFrancBord>> configureInput(Table inputType) throws IllegalStateException {
            if (inputType.getColumn(Columns.ID_TYPE_LARGEUR_FB.name()) == null
                    || inputType.getColumn(Columns.ID_SOURCE.name()) == null) {
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
