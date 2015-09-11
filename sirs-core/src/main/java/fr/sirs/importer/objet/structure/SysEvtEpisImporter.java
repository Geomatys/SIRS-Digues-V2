package fr.sirs.importer.objet.structure;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.core.model.Epi;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class SysEvtEpisImporter extends GenericStructureImporter<Epi> {

    @Override
    protected Class<Epi> getDocumentClass() {
        return Epi.class;
    }

    private enum Columns {
        //        id_nom_element, // Redondant avec ID_ELEMENT_STRUCTURE
        //        ID_SOUS_GROUPE_DONNEES, // Redondant avec le type de données
        //        LIBELLE_TYPE_ELEMENT_STRUCTURE, // Redondant avec le type de données
        //        DECALAGE_DEFAUT, // Affichage
        //        DECALAGE, // Affichage
        //        LIBELLE_SOURCE, // Redondant avec l'importation des sources
        //        LIBELLE_TYPE_COTE, // Redondant avec l'importation des types de côtés
        //        LIBELLE_SYSTEME_REP, // Redondant avec l'importation des SR
        //        NOM_BORNE_DEBUT, // Redondant avec l'importatoin des bornes
        //        NOM_BORNE_FIN, // Redondant avec l'importation des bornes
        //        LIBELLE_TYPE_MATERIAU, // Redondant avec l'importation des matériaux
        //        LIBELLE_TYPE_NATURE, // Redondant avec l'importation des matériaux
        //        LIBELLE_TYPE_FONCTION, // Redondant avec l'importation des fonctions
        //        LIBELLE_TYPE_NATURE_HAUT, // Redondant avec l'importation des natures
        //        LIBELLE_TYPE_MATERIAU_HAUT, // Redondant avec l'importation des matériaux
        //        LIBELLE_TYPE_NATURE_BAS, // Redondant avec l'importation des natures
        //        LIBELLE_TYPE_MATERIAU_BAS, // Redondant avec l'importation des matériaux
        //        LIBELLE_TYPE_OUVRAGE_PARTICULIER,
        //        LIBELLE_TYPE_POSITION,
        //        RAISON_SOCIALE_ORG_PROPRIO, // Redondant avec l'importation des organismes
        //        RAISON_SOCIALE_ORG_GESTION, // Redondant avec l'importation des organismes
        //        INTERV_PROPRIO,
        //        INTERV_GARDIEN,
        //        LIBELLE_TYPE_COMPOSITION,
        //        LIBELLE_TYPE_VEGETATION,
        //        ID_TYPE_ELEMENT_STRUCTURE, // Redondant avec le type de données
        ID_TYPE_COTE,
        ID_SOURCE,
        //        N_COUCHE, // Pas dans le nouveau modèle
        //        ID_TYPE_MATERIAU, // Pas dans le nouveau modèle
        //        ID_TYPE_NATURE, // Pas dans le nouveau modèle
        //        ID_TYPE_FONCTION, // Pas dans le nouveau modèle
        //        EPAISSEUR, // Pas dans le nouveau modèle
        //        TALUS_INTERCEPTE_CRETE,
        //        ID_TYPE_NATURE_HAUT,
        //        ID_TYPE_MATERIAU_HAUT,
        //        ID_TYPE_MATERIAU_BAS,
        //        ID_TYPE_NATURE_BAS,
        //        LONG_RAMP_HAUT,
        //        LONG_RAMP_BAS,
        //        PENTE_INTERIEURE,
        //        ID_TYPE_OUVRAGE_PARTICULIER,
        ID_TYPE_POSITION,
//        ID_ORG_PROPRIO,
//        ID_ORG_GESTION,
//        ID_INTERV_PROPRIO,
//        ID_INTERV_GARDIEN,
//        DATE_DEBUT_ORGPROPRIO,
//        DATE_FIN_ORGPROPRIO,
//        DATE_DEBUT_GESTION,
//        DATE_FIN_GESTION,
//        DATE_DEBUT_INTERVPROPRIO,
//        DATE_FIN_INTERVPROPRIO,
//        ID_TYPE_COMPOSITION,
//        DISTANCE_TRONCON,
//        LONGUEUR,
//        DATE_DEBUT_GARDIEN,
//        DATE_FIN_GARDIEN,
//        LONGUEUR_PERPENDICULAIRE,
//        LONGUEUR_PARALLELE,
//        COTE_AXE,
//        ID_TYPE_VEGETATION,
//        HAUTEUR,
//        DIAMETRE,
//        DENSITE,
//        EPAISSEUR_Y11,
//        EPAISSEUR_Y12,
//        EPAISSEUR_Y21,
//        EPAISSEUR_Y22,
//        ID_AUTO
    };

    @Override
    public String getTableName() {
        return SYS_EVT_EPIS.toString();
    }

    @Override
    public public  importRow(Row row, Epi epi) throws IOException, AccessDbImporterException {

        if (row.getInt(Columns.ID_SOURCE.toString()) != null) {
            epi.setSourceId(sourceInfoImporter.getImportedId(row.getInt(Columns.ID_SOURCE.toString())));
        }

        if (row.getInt(Columns.ID_TYPE_POSITION.toString()) != null) {
            epi.setPositionId(typePositionImporter.getImportedId(row.getInt(Columns.ID_TYPE_POSITION.toString())));
        }

        return epi;
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
