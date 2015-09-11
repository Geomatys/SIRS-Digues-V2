package fr.sirs.importer.objet.geometry;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.core.model.LargeurFrancBord;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class SysEvtLargeurFrancBordImporter extends GenericGeometrieImporter<LargeurFrancBord> {

    @Override
    protected Class<LargeurFrancBord> getDocumentClass() {
        return LargeurFrancBord.class;
    }

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
    };

    @Override
    public String getTableName() {
        return SYS_EVT_LARGEUR_FRANC_BORD.toString();
    }

    @Override
    public LargeurFrancBord importRow(Row row, LargeurFrancBord largeur) throws IOException, AccessDbImporterException {
        final Integer sourceId = row.getInt(Columns.ID_SOURCE.toString());
        if (sourceId != null) {
            largeur.setSourceId(sourceInfoImporter.getImportedId(sourceId));
        }

        final Integer typeLargeurId = row.getInt(Columns.ID_TYPE_LARGEUR_FB.toString());
        if (typeLargeurId != null) {
            largeur.setTypeLargeurFrancBord(typeLargeurFrancBordImporter.getImportedId(typeLargeurId));
        }

        return largeur;
    }

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }
}
