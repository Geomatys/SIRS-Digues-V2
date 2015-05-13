package fr.sirs.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.BorneDigue;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import static fr.sirs.importer.DbImporter.TableName.BORNE_PAR_SYSTEME_REP;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class SystemeReperageBorneImporter extends GenericImporter {

    private Map<Integer, List<SystemeReperageBorne>> byBorneId = null;
    private Map<Integer, List<SystemeReperageBorne>> bySystemeReperageId = null;
    private SystemeReperageImporter systemeReperageImporter;
    private BorneDigueImporter borneDigueImporter;

    private SystemeReperageBorneImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    SystemeReperageBorneImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter) {
        this(accessDatabase, couchDbConnector);
        this.systemeReperageImporter = systemeReperageImporter;
        this.borneDigueImporter = borneDigueImporter;
    }

    private enum Columns {
        ID_BORNE,
        ID_SYSTEME_REP,
        VALEUR_PR,
        DATE_DERNIERE_MAJ
    };

    /**
     *
     * @return A map containing all SystemeReperageBorne instances accessibles from
     * the internal database <em>BorneDigue</em> identifier.
     * @throws IOException
     * @throws fr.sirs.importer.AccessDbImporterException
     */
    public Map<Integer, List<SystemeReperageBorne>> getByBorneId() throws IOException, AccessDbImporterException {
        if (byBorneId == null) compute();
        return byBorneId;
    }

    /**
     *
     * @return A map containing all SystemeReperageBorne instances accessibles from
     * the internal database <em>SystemeReperage</em> identifier.
     * @throws IOException
     * @throws fr.sirs.importer.AccessDbImporterException
     */
    public Map<Integer, List<SystemeReperageBorne>> getBySystemeReperageId() throws IOException, AccessDbImporterException {
        if (bySystemeReperageId == null) compute();
        return bySystemeReperageId;
    }

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
        return BORNE_PAR_SYSTEME_REP.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        byBorneId = new HashMap<>();
        bySystemeReperageId = new HashMap<>();
        
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final SystemeReperageBorne systemeReperageBorne = createAnonymValidElement(SystemeReperageBorne.class);

            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                systemeReperageBorne.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            
            if (row.getDouble(Columns.VALEUR_PR.toString())!=null){
                systemeReperageBorne.setValeurPR(row.getDouble(Columns.VALEUR_PR.toString()).floatValue());
            }
            
            final BorneDigue borne = bornes.get(row.getInt(Columns.ID_BORNE.toString()));
            systemeReperageBorne.setBorneId(borne.getId());
            
            // Table de jointure, donc pas d'id propre : on affecte arbitrairement l'id de la borne comme pseudo id.
            systemeReperageBorne.setDesignation(String.valueOf(row.getInt(Columns.ID_BORNE.toString())));
            
            final SystemeReperage systemeReperage = systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString()));
            if(systemeReperage!=null){
                systemeReperage.systemeReperageBorne.add(systemeReperageBorne);
            }
            
            if(byBorneId.get(row.getInt(Columns.ID_BORNE.toString()))==null) {
                byBorneId.put(row.getInt(Columns.ID_BORNE.toString()), new ArrayList<>());
            }
            byBorneId.get(row.getInt(Columns.ID_BORNE.toString())).add(systemeReperageBorne);
            
            if(bySystemeReperageId.get(row.getInt(Columns.ID_SYSTEME_REP.toString()))==null) {
                bySystemeReperageId.put(row.getInt(Columns.ID_SYSTEME_REP.toString()), new ArrayList<>());
            }
            bySystemeReperageId.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).add(systemeReperageBorne);
        }
        
        couchDbConnector.executeBulk(systemesReperage.values());
    }
}
