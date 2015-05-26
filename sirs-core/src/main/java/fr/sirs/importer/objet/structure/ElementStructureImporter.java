package fr.sirs.importer.objet.structure;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SirsCore;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.core.model.Crete;
import fr.sirs.core.model.Epi;
import fr.sirs.core.model.Fondation;
import fr.sirs.core.model.FrontFrancBord;
import fr.sirs.core.model.OuvrageRevanche;
import fr.sirs.core.model.PiedDigue;
import fr.sirs.core.model.SommetRisberme;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ObjetStructure;
import fr.sirs.core.model.PiedFrontFrancBord;
import fr.sirs.core.model.TalusDigue;
import fr.sirs.core.model.TalusRisberme;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.TypeCoteImporter;
import fr.sirs.importer.objet.TypeFonctionImporter;
import fr.sirs.importer.objet.TypeMateriauImporter;
import fr.sirs.importer.objet.TypeNatureImporter;
import fr.sirs.importer.objet.TypePositionImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ElementStructureImporter extends GenericStructureImporter<ObjetStructure> {

    private final TypeElementStructureImporter typeElementStructureImporter;

    private final SysEvtCreteImporter sysEvtCreteImporter;
    private final SysEvtPiedDeDigueImporter sysEvtPiedDeDigueImporter;
    private final SysEvtTalusDigueImporter sysEvtTalusDigueImporter;
    private final SysEvtSommetRisbermeImporter sysEvtSommetRisbermeImporter;
    private final SysEvtTalusRisbermeImporter sysEvtTalusRisbermeImporter;
    private final SysEvtFondationImporter sysEvtFondationImporter;
    private final SysEvtEpisImporter sysEvtEpiImporter;
    private final SysEvtOuvrageRevancheImporter sysEvtOuvrageRevancheImporter;
    private final SysEvtTalusFrancBordImporter sysEvtTalusFrancBordImporter;
    private final SysEvtPiedFrontFrancBordImporter sysEvtPiedFrontFrancBordImporter;

    public ElementStructureImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter,
            final SourceInfoImporter typeSourceImporter,
            final TypePositionImporter typePositionImporter,
            final TypeCoteImporter typeCoteImporter,
            final TypeMateriauImporter typeMateriauImporter,
            final TypeNatureImporter typeNatureImporter,
            final TypeFonctionImporter typeFonctionImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter,
                typePositionImporter, typeMateriauImporter, typeNatureImporter,
                typeFonctionImporter);
        typeElementStructureImporter = new TypeElementStructureImporter(
                accessDatabase);
        sysEvtCreteImporter = new SysEvtCreteImporter(accessDatabase, couchDbConnector,
                tronconGestionDigueImporter, systemeReperageImporter,
                borneDigueImporter, typeSourceImporter, typeCoteImporter,
                typePositionImporter, typeMateriauImporter, typeNatureImporter,
                typeFonctionImporter);
        sysEvtPiedDeDigueImporter = new SysEvtPiedDeDigueImporter(accessDatabase,
                couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter, typePositionImporter,
                typeMateriauImporter, typeNatureImporter,
                typeFonctionImporter);
        sysEvtTalusDigueImporter = new SysEvtTalusDigueImporter(accessDatabase,
                couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter, typePositionImporter,
                typeMateriauImporter, typeNatureImporter,
                typeFonctionImporter);
        sysEvtSommetRisbermeImporter = new SysEvtSommetRisbermeImporter(accessDatabase,
                couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter, typePositionImporter,
                typeMateriauImporter, typeNatureImporter,
                typeFonctionImporter);
        sysEvtTalusRisbermeImporter = new SysEvtTalusRisbermeImporter(accessDatabase,
                couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter, typePositionImporter,
                typeMateriauImporter, typeNatureImporter,
                typeFonctionImporter);
        sysEvtFondationImporter = new SysEvtFondationImporter(accessDatabase,
                couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter, typePositionImporter,
                typeMateriauImporter, typeNatureImporter,
                typeFonctionImporter);
        sysEvtEpiImporter = new SysEvtEpisImporter(accessDatabase, couchDbConnector,
                tronconGestionDigueImporter, systemeReperageImporter,
                borneDigueImporter,
                typeSourceImporter, typeCoteImporter, typePositionImporter);
        sysEvtOuvrageRevancheImporter = new SysEvtOuvrageRevancheImporter(accessDatabase,
                couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter, typePositionImporter,
                typeMateriauImporter, typeNatureImporter);
        sysEvtTalusFrancBordImporter = new SysEvtTalusFrancBordImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter, typePositionImporter,
                typeMateriauImporter, typeNatureImporter,
                typeFonctionImporter);
        sysEvtPiedFrontFrancBordImporter = new SysEvtPiedFrontFrancBordImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter, typePositionImporter,
                typeMateriauImporter, typeNatureImporter);
    }

    private enum Columns {

        ID_ELEMENT_STRUCTURE,
        ID_TYPE_ELEMENT_STRUCTURE,
        //        ID_TYPE_COTE,
        //        ID_SOURCE,
        ID_TRONCON_GESTION,
        //        DATE_DEBUT_VAL,
        //        DATE_FIN_VAL,
        //        PR_DEBUT_CALCULE,
        //        PR_FIN_CALCULE,
        //        X_FIN,
        //        Y_FIN,
        //        ID_SYSTEME_REP,
        //        ID_BORNEREF_DEBUT,
        //        AMONT_AVAL_DEBUT,
        //        DIST_BORNEREF_DEBUT,
        //        ID_BORNEREF_FIN,
        //        AMONT_AVAL_FIN,
        //        DIST_BORNEREF_FIN,
        //        COMMENTAIRE,
        //        N_COUCHE, // N'est pas disponible au niveau des structures dans le nouveau modèle
        //        ID_TYPE_MATERIAU,
        //        ID_TYPE_NATURE,
        //        ID_TYPE_FONCTION,
        //        EPAISSEUR, // N'est pas disponible au niveau des structures dans le nouveau modèle
        //        TALUS_INTERCEPTE_CRETE,
        //        ID_TYPE_NATURE_HAUT,
        //        ID_TYPE_MATERIAU_HAUT,
        //        ID_TYPE_MATERIAU_BAS,
        //        ID_TYPE_NATURE_BAS,
        //        LONG_RAMP_HAUT,
        //        LONG_RAMP_BAS,
        //        PENTE_INTERIEURE,
        //        ID_TYPE_POSITION,
        //        ID_TYPE_VEGETATION,
        //        HAUTEUR,
        //        EPAISSEUR_Y11,
        //        EPAISSEUR_Y21,
        //        EPAISSEUR_Y12,
        //        EPAISSEUR_Y22,
        //        ID_TYPE_VEGETATION_ESSENCE_1,
        //        ID_TYPE_VEGETATION_ESSENCE_2,
        //        ID_TYPE_VEGETATION_ESSENCE_3,
        //        ID_TYPE_VEGETATION_ESSENCE_4,
        //        DISTANCE_AXE_M,
        //        PENTE_PCT,
        //        ID_CONTACT_EAU_ON,
        //        RECOUVREMENT_STRATE_1,
        //        RECOUVREMENT_STRATE_2,
        //        RECOUVREMENT_STRATE_3,
        //        RECOUVREMENT_STRATE_4,
        //        RECOUVREMENT_STRATE_5,
        //        ID_TYPE_VEGETATION_ABONDANCE,
        //        ID_TYPE_VEGETATION_STRATE_DIAMETRE,
        //        ID_TYPE_VEGETATION_STRATE_HAUTEUR,
        //        DENSITE_STRATE_DOMINANTE,
        //        ID_TYPE_VEGETATION_ETAT_SANITAIRE,
        //        ID_ABONDANCE_BRAUN_BLANQUET_RENOUE,
        //        ID_ABONDANCE_BRAUN_BLANQUET_BUDLEIA,
        //        ID_ABONDANCE_BRAUN_BLANQUET_SOLIDAGE,
        //        ID_ABONDANCE_BRAUN_BLANQUET_VIGNE_VIERGE,
        //        ID_ABONDANCE_BRAUN_BLANQUET_S_YEBLE,
        //        ID_ABONDANCE_BRAUN_BLANQUET_E_NEGUN,
        //        ID_ABONDANCE_BRAUN_BLANQUET_IMPA_GLANDUL,
        //        ID_ABONDANCE_BRAUN_BLANQUET_GLOBAL,

        // Empty fields
        //    X_DEBUT,
        //    Y_DEBUT,
        //    ID_TYPE_OUVRAGE_PARTICULIER,
        //    ID_ORG_PROPRIO,
        //    ID_ORG_GESTION,
        //    ID_INTERV_PROPRIO,
        //    ID_INTERV_GARDIEN,
        //    DATE_DEBUT_ORGPROPRIO,
        //    DATE_FIN_ORGPROPRIO,
        //    DATE_DEBUT_GESTION,
        //    DATE_FIN_GESTION,
        //    DATE_DEBUT_INTERVPROPRIO,
        //    DATE_FIN_INTERVPROPRIO,
        //    ID_TYPE_COMPOSITION,
        //    DISTANCE_TRONCON,
        //    LONGUEUR,
        //    DATE_DEBUT_GARDIEN,
        //    DATE_FIN_GARDIEN,
        //    LONGUEUR_PERPENDICULAIRE,
        //    LONGUEUR_PARALLELE,
        //    COTE_AXE,
        //    DIAMETRE,
        //    DENSITE,
        //    NUMERO_PARCELLE,
        //    NUMERO_FORMATION_VEGETALE,
        DATE_DERNIERE_MAJ,
//    LARGEUR
    };

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return ELEMENT_STRUCTURE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        objets = new HashMap<>();
        objetsByTronconId = new HashMap<>();

        // Vérification de la cohérence des structures au sens strict.
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();

            final ObjetStructure objet = importRow(row);

            if (objet != null) {
                if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                    objet.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
                }

                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                objets.put(row.getInt(Columns.ID_ELEMENT_STRUCTURE.toString()), objet);

                // Set the list ByTronconId
                List<ObjetStructure> listByTronconId = objetsByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    objetsByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
                }
                listByTronconId.add(objet);
            }
        }
        couchDbConnector.executeBulk(objets.values());
    }

    @Override
    public ObjetStructure importRow(Row row) throws IOException, AccessDbImporterException {

        final Class typeStructure = typeElementStructureImporter.getTypeReferences().get(row.getInt(Columns.ID_TYPE_ELEMENT_STRUCTURE.toString()));
        if (typeStructure == Crete.class) {
            return sysEvtCreteImporter.importRow(row);
        } else if (typeStructure == Epi.class) {
            return sysEvtEpiImporter.importRow(row);
        } else if (typeStructure == Fondation.class) {
            return sysEvtFondationImporter.importRow(row);
        } else if (typeStructure == OuvrageRevanche.class) {
            return sysEvtOuvrageRevancheImporter.importRow(row);
        } else if (typeStructure == PiedDigue.class) {
            return sysEvtPiedDeDigueImporter.importRow(row);
        } else if (typeStructure == PiedFrontFrancBord.class) {
            return sysEvtPiedFrontFrancBordImporter.importRow(row);
        } else if (typeStructure == SommetRisberme.class) {
            return sysEvtSommetRisbermeImporter.importRow(row);
        } else if (typeStructure == TalusDigue.class) {
            return sysEvtTalusDigueImporter.importRow(row);
        } else if (typeStructure == FrontFrancBord.class) {
            return sysEvtTalusFrancBordImporter.importRow(row);
        } else if (typeStructure == TalusRisberme.class) {
            return sysEvtTalusRisbermeImporter.importRow(row);
        } else {
            SirsCore.LOGGER.log(Level.SEVERE, typeStructure + " : Type de structure incohérent.");
            return null;
        }
    }
}
