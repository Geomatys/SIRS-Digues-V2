package fr.sirs.importer.troncon;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.LinearReferencingUtilities.buildGeometry;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Contact;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.GardeTroncon;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.v2.CorruptionLevel;
import fr.sirs.importer.v2.ErrorReport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class GardienTronconGestionImporter extends GenericPeriodeLocaliseeImporter<GardeTroncon> {

    private final IntervenantImporter intervenantImporter;

    public GardienTronconGestionImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter,
            final IntervenantImporter intervenantImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter);
        this.intervenantImporter = intervenantImporter;
    }

    private enum Columns {
        ID_GARDIEN_TRONCON_GESTION, // Pas dans le nouveau modèle
        ID_INTERVENANT,
        ID_TRONCON_GESTION,
        DATE_DEBUT,
        DATE_FIN,
        PR_DEBUT_CALCULE, // Pas dans le nouveau modèle
        PR_FIN_CALCULE,  // Pas dans le nouveau modèle
        X_DEBUT, // Pas dans le nouveau modèle
        X_FIN, // Pas dans le nouveau modèle
        Y_DEBUT, // Pas dans le nouveau modèle
        Y_FIN, // Pas dans le nouveau modèle
        ID_BORNEREF_DEBUT, // Pas dans le nouveau modèle
        ID_BORNEREF_FIN, // Pas dans le nouveau modèle
        ID_SYSTEME_REP, // Pas dans le nouveau modèle
        DIST_BORNEREF_DEBUT, // Pas dans le nouveau modèle
        DIST_BORNEREF_FIN, // Pas dans le nouveau modèle
        AMONT_AVAL_DEBUT, // Pas dans le nouveau modèle
        AMONT_AVAL_FIN, // Pas dans le nouveau modèle
        DATE_DERNIERE_MAJ
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
        return GARDIEN_TRONCON_GESTION.toString();
    }

    @Override
    public void compute() throws IOException, AccessDbImporterException {
        objets = new HashMap<>();

        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, Contact> intervenants = intervenantImporter.getIntervenants();

        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final GardeTroncon garde = createAnonymValidElement(GardeTroncon.class);

            final TronconDigue troncon = tronconGestionDigueImporter.getTronconsDigues().get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            garde.setLinearId(troncon.getId());

            garde.setDesignation(String.valueOf(row.getInt(Columns.ID_GARDIEN_TRONCON_GESTION.toString())));

            if (row.getDate(Columns.DATE_DEBUT.toString()) != null) {
                garde.setDate_debut(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DEBUT.toString()), dateTimeFormatter));
            }
            if (row.getDate(Columns.DATE_FIN.toString()) != null) {
                garde.setDate_fin(DbImporter.parseLocalDate(row.getDate(Columns.DATE_FIN.toString()), dateTimeFormatter));
            }
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                garde.setDateMaj(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }

            if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
                garde.setPrDebut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
            }

            if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
                garde.setPrFin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
            }

            if (row.getInt(Columns.ID_SYSTEME_REP.toString()) != null) {
                garde.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
            }

            if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
                garde.setBorneDebutId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
            }

            garde.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString()));

            if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
                garde.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue());
            }

            if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
                garde.setBorneFinId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
            }

            garde.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));

            if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
                garde.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
            }

            try {
                context.setGeoPositions(row, garde);
            } catch (TransformException ex) {
                context.reportError(new ErrorReport(ex, row, getTableName(), null, garde, null, "Cannnot set geographic position.", CorruptionLevel.FIELD));
            }

            garde.setGeometry(buildGeometry(troncon.getGeometry(), garde, tronconGestionDigueImporter.getBorneDigueRepository()));

            // Set the references.
            final Contact intervenant = intervenants.get(row.getInt(Columns.ID_INTERVENANT.toString()));
            if (intervenant.getId() != null) {
                garde.setContactId(intervenant.getId());
            } else {
                throw new AccessDbImporterException("Le contact " + intervenant + " n'a pas encore d'identifiant CouchDb !");
            }

            objets.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), garde);
        }
        context.outputDb.executeBulk(objets.values());
    }
}
