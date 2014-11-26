package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.core.model.Pompe;
import static fr.sirs.importer.DbImporter.cleanNullString;
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
class PompeImporter extends GenericImporter {

    private Map<Integer, Pompe> pompes = null;
    private Map<Integer, List<Pompe>> pompesByElementReseau = null;

    PompeImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum PompeColumns {
        ID_POMPE,
        ID_ELEMENT_RESEAU,
        NOM_POMPE,
        PUISSANCE_POMPE,
        DEBIT_POMPE,
        HAUTEUR_REFOUL,
        DATE_DERNIERE_MAJ
    };

    /**
     *
     * @return A map containing all the Pompe elements
     * referenced by their internal ID.
     * @throws IOException
     */
    public Map<Integer, Pompe> getPompe() throws IOException {
        if (pompes == null) {
            compute();
        }
        return pompes;
    }

    /**
     *
     * @return A map containing all the Pompe elements
     * referenced by the corresponding element reseau internal ID.
     * @throws IOException
     */
    public Map<Integer, List<Pompe>> getPompeByElementReseau() throws IOException {
        if (pompesByElementReseau == null) {
            compute();
        }
        return pompesByElementReseau;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (PompeColumns c : PompeColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.ELEMENT_RESEAU_POMPE.toString();
    }

    @Override
    protected void compute() throws IOException {
        pompes = new HashMap<>();
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();

        while (it.hasNext()) {
            final Row row = it.next();
            final Pompe pompe = new Pompe();
            
            pompe.setMarque(cleanNullString(row.getString(PompeColumns.NOM_POMPE.toString())));
            
            if (row.getDouble(PompeColumns.PUISSANCE_POMPE.toString()) != null) {
                pompe.setPuissance(row.getDouble(PompeColumns.PUISSANCE_POMPE.toString()).floatValue());
            }
            
            if (row.getDouble(PompeColumns.DEBIT_POMPE.toString()) != null) {
                pompe.setDebit(row.getDouble(PompeColumns.DEBIT_POMPE.toString()).floatValue());
            }
            
            if (row.getDouble(PompeColumns.HAUTEUR_REFOUL.toString()) != null) {
                pompe.setHauteur_refoulement(row.getDouble(PompeColumns.HAUTEUR_REFOUL.toString()).floatValue());
            }
            
            if (row.getDate(PompeColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                pompe.setDateMaj(LocalDateTime.parse(row.getDate(PompeColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            pompes.put(row.getInt(PompeColumns.ID_POMPE.toString()), pompe);

            // Set the list ByEltReseauId
            List<Pompe> listByEltReseauId = pompesByElementReseau.get(row.getInt(PompeColumns.ID_ELEMENT_RESEAU.toString()));
            if (listByEltReseauId == null) {
                listByEltReseauId = new ArrayList<>();
                pompesByElementReseau.put(row.getInt(PompeColumns.ID_ELEMENT_RESEAU.toString()), listByEltReseauId);
            }
            listByEltReseauId.add(pompe);
        }
    }
}
