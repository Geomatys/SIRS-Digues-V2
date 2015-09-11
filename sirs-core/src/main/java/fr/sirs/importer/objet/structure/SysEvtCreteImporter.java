package fr.sirs.importer.objet.structure;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.core.model.Crete;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class SysEvtCreteImporter extends GenericStructureImporter<Crete> {

    @Override
    protected Class<Crete> getDocumentClass() {
        return Crete.class;
    }

    private enum Columns {
        //        id_nom_element,//Inutile
        //        ID_SOUS_GROUPE_DONNEES,//Redondant
        //        LIBELLE_TYPE_ELEMENT_STRUCTURE,// Redondant
        //        DECALAGE_DEFAUT,//Affichage
        //        DECALAGE,//Affichage
        //        LIBELLE_SOURCE, // Dans le TypeSourceImporter
        //        LIBELLE_SYSTEME_REP,// Dans le SystemeRepImporter
        //        NOM_BORNE_DEBUT, // Dans le BorneImporter
        //        NOM_BORNE_FIN, // Dans le BorneImporter
        //        LIBELLE_TYPE_MATERIAU, // Dans l'importateur de matériaux
        //        LIBELLE_TYPE_NATURE, // Dans l'importation des natures
        //        LIBELLE_TYPE_FONCTION, // Redondant avec l'importation des fonctions
        //        ID_TYPE_ELEMENT_STRUCTURE,// Dans le TypeElementStructureImporter
        ID_SOURCE,
        N_COUCHE,
        ID_TYPE_MATERIAU,
        ID_TYPE_NATURE,
        ID_TYPE_FONCTION,
        EPAISSEUR,
        //        TALUS_INTERCEPTE_CRETE,
        //        ID_AUTO

        // Empty fields
        //     LIBELLE_TYPE_COTE, // Dans le typeCoteimporter
        //     LIBELLE_TYPE_NATURE_HAUT, Dans le NatureImporter
        //     LIBELLE_TYPE_MATERIAU_HAUT, // Dans l'importateur de matériaux
        //     LIBELLE_TYPE_NATURE_BAS, // Dans l'importation des natures
        //     LIBELLE_TYPE_MATERIAU_BAS, // Dans l'importateur de matériaux
        //     LIBELLE_TYPE_OUVRAGE_PARTICULIER,
        //     LIBELLE_TYPE_POSITION, // Dans le TypePositionImporter
        //     RAISON_SOCIALE_ORG_PROPRIO,
        //     RAISON_SOCIALE_ORG_GESTION,
        //     INTERV_PROPRIO,
        //     INTERV_GARDIEN,
        //     LIBELLE_TYPE_COMPOSITION,
        //     LIBELLE_TYPE_VEGETATION,
        ID_TYPE_COTE,
        //     ID_TYPE_NATURE_HAUT, // Pas dans le nouveau modèle
        //     ID_TYPE_MATERIAU_HAUT, // Pas dans le nouveau modèle
        //     ID_TYPE_MATERIAU_BAS, // Pas dans le nouveau modèle
        //     ID_TYPE_NATURE_BAS, // Pas dans le nouveau modèle
        //     LONG_RAMP_HAUT,
        //     LONG_RAMP_BAS,
        //     PENTE_INTERIEURE,
        //     ID_TYPE_OUVRAGE_PARTICULIER,
        ID_TYPE_POSITION,
//     ID_ORG_PROPRIO,
//     ID_ORG_GESTION,
//     ID_INTERV_PROPRIO,
//     ID_INTERV_GARDIEN,
//     DATE_DEBUT_ORGPROPRIO,
//     DATE_FIN_ORGPROPRIO,
//     DATE_DEBUT_GESTION,
//     DATE_FIN_GESTION,
//     DATE_DEBUT_INTERVPROPRIO,
//     DATE_FIN_INTERVPROPRIO,
//     ID_TYPE_COMPOSITION,
//     DISTANCE_TRONCON,
//     LONGUEUR,
//     DATE_DEBUT_GARDIEN,
//     DATE_FIN_GARDIEN,
//     LONGUEUR_PERPENDICULAIRE,
//     LONGUEUR_PARALLELE,
//     COTE_AXE,
//     ID_TYPE_VEGETATION,
//     HAUTEUR,
//     DIAMETRE,
//     DENSITE,
//     EPAISSEUR_Y11,
//     EPAISSEUR_Y12,
//     EPAISSEUR_Y21,
//     EPAISSEUR_Y22,
    };

    @Override
    public String getTableName() {
        return SYS_EVT_CRETE.toString();
    }

    @Override
    public public  importRow(Row row, Crete output) throws IOException, AccessDbImporterException {
        output.setNumCouche(row.getInt(Columns.N_COUCHE.toString()));

        final Integer sourceId = row.getInt(Columns.ID_SOURCE.toString());
        if (sourceId != null) {
            output.setSourceId(sourceInfoImporter.getImportedId(sourceId));
        }

        final Integer materiauId = row.getInt(Columns.ID_TYPE_MATERIAU.toString());
        if (materiauId != null) {
            output.setMateriauId(typeMateriauImporter.getImportedId(materiauId));
        }

        final Integer natureId = row.getInt(Columns.ID_TYPE_NATURE.toString());
        if (natureId != null) {
            output.setNatureId(typeNatureImporter.getImportedId(natureId));
        }

        final Integer fctId = row.getInt(Columns.ID_TYPE_FONCTION.toString());
        if (fctId != null) {
            output.setFonctionId(typeFonctionImporter.getImportedId(fctId));
        }

        if (row.getDouble(Columns.EPAISSEUR.toString()) != null) {
            output.setEpaisseur(row.getDouble(Columns.EPAISSEUR.toString()).floatValue());
        }

        return output;
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
