package fr.sirs.importer.objet.desordre;

import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.LinearReferencingUtilities.buildGeometry;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Desordre;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.Observation;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.RefTypeDesordre;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.CorruptionLevel;
import fr.sirs.importer.v2.ErrorReport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class SysEvtDesordreImporter extends GenericDesordreImporter {

    private final TypeDesordreImporter typeDesordreImporter;
    private final DesordreObservationImporter desordreObservationImporter;

    private enum Columns {

        ID_DESORDRE,
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
        ID_TRONCON_GESTION,
        ID_SOURCE,
        DATE_DEBUT_VAL,
        DATE_FIN_VAL,
        LIEU_DIT_DESORDRE,
        DESCRIPTION_DESORDRE,
        //            ID_AUTO

        //Empty fields
        //     ID_PRESTATION, // obsolète ? voir table DESORDRE_PRESTATION
        //     LIBELLE_PRESTATION, // Dans l'importateur de prestations
        X_DEBUT,
        Y_DEBUT,
        X_FIN,
        Y_FIN,
//     COMMENTAIRE, // obsolète ? voir champ DESCRIPTION_DESORDRE
    };

    @Override
    public String getTableName() {
        return SYS_EVT_DESORDRE.toString();
    }

    @Override
    public public  importRow(Row row) throws IOException, AccessDbImporterException {

        final TronconDigue troncon = tronconGestionDigueImporter.getTronconsDigues().get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();

        final Map<Integer, RefSource> typesSource = sourceInfoImporter.getTypeReferences();
        final Map<Integer, RefCote> typesCote = typeCoteImporter.getTypeReferences();
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypeReferences();

        final Map<Integer, RefTypeDesordre> typesDesordre = typeDesordreImporter.getTypeReferences();
        final Map<Integer, List<Observation>> observations = desordreObservationImporter.getObservationsByDesordreId();

        final Desordre desordre = createAnonymValidElement(Desordre.class);

        desordre.setLinearId(troncon.getId());
        desordre.setDesignation(String.valueOf(row.getInt(Columns.ID_DESORDRE.toString())));

        if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
            final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue());
            if (b != null) {
                desordre.setBorneDebutId(b.getId());
            }
        }
        if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
            desordre.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue());
        }
        if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
            desordre.setPrDebut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
        }

        if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
            final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue());
            if (b != null) {
                desordre.setBorneFinId(b.getId());
            }
        }
        if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
            desordre.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
        }
        if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
            desordre.setPrFin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
        }

        if (row.getInt(Columns.ID_SYSTEME_REP.toString()) != null) {
            desordre.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
        }

        desordre.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString()));
        desordre.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));
        desordre.setLieuDit(row.getString(Columns.LIEU_DIT_DESORDRE.toString()));

        if (row.getInt(Columns.ID_TYPE_DESORDRE.toString()) != null) {
            desordre.setTypeDesordreId(typesDesordre.get(row.getInt(Columns.ID_TYPE_DESORDRE.toString())).getId());
        }

        if (row.getInt(Columns.ID_SOURCE.toString()) != null) {
            desordre.setSourceId(sourceInfoImporter.getImportedId(row.getInt(Columns.ID_SOURCE.toString())).getId());
        }

        if (row.getInt(Columns.ID_TYPE_POSITION.toString()) != null) {
            desordre.setPositionId(typePositionImporter.getImportedId(row.getInt(Columns.ID_TYPE_POSITION.toString())).getId());
        }

        if (row.getInt(Columns.ID_TYPE_COTE.toString()) != null) {
            desordre.setCoteId(typesCote.get(row.getInt(Columns.ID_TYPE_COTE.toString())).getId());
        }

        if (row.getDate(Columns.DATE_DEBUT_VAL.toString()) != null) {
            desordre.setDate_debut(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DEBUT_VAL.toString()), dateTimeFormatter, desordre));
        }

        if (row.getDate(Columns.DATE_FIN_VAL.toString()) != null) {
            desordre.setDate_fin(DbImporter.parseLocalDate(row.getDate(Columns.DATE_FIN_VAL.toString()), dateTimeFormatter, desordre));
        }

        if (row.getString(Columns.DESCRIPTION_DESORDRE.toString()) != null) {
            desordre.setCommentaire(row.getString(Columns.DESCRIPTION_DESORDRE.toString()));
        }

        try {
            context.setGeoPositions(row, desordre);
        } catch (TransformException ex) {
            context.reportError(new ErrorReport(ex, row, getTableName(), null, desordre, null, "Cannnot set geographic position.", CorruptionLevel.FIELD));
        }

        if (observations.get(row.getInt(Columns.ID_DESORDRE.toString())) != null) {
            desordre.setObservations(observations.get(row.getInt(Columns.ID_DESORDRE.toString())));
        }

        desordre.setGeometry(buildGeometry(troncon.getGeometry(), desordre, tronconGestionDigueImporter.getBorneDigueRepository()));

        return desordre;
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
