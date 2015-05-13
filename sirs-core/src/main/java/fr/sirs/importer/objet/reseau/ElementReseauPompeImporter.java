package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import static fr.sirs.importer.DbImporter.TableName.*;
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
class ElementReseauPompeImporter extends GenericImporter {

    private Map<Integer, Pompe> pompes = null;
    private Map<Integer, List<Pompe>> pompesByElementReseau = null;

    ElementReseauPompeImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
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
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return ELEMENT_RESEAU_POMPE.toString();
    }

    @Override
    protected void compute() throws IOException {
        pompes = new HashMap<>();
        pompesByElementReseau = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Pompe pompe = createAnonymValidElement(Pompe.class);
            
            pompe.setMarque(cleanNullString(row.getString(Columns.NOM_POMPE.toString())));
            
            if (row.getDouble(Columns.PUISSANCE_POMPE.toString()) != null) {
                pompe.setPuissance(row.getDouble(Columns.PUISSANCE_POMPE.toString()).floatValue());
            }
            
            if (row.getDouble(Columns.DEBIT_POMPE.toString()) != null) {
                pompe.setDebit(row.getDouble(Columns.DEBIT_POMPE.toString()).floatValue());
            }
            
            if (row.getDouble(Columns.HAUTEUR_REFOUL.toString()) != null) {
                pompe.setHauteurRefoulement(row.getDouble(Columns.HAUTEUR_REFOUL.toString()).floatValue());
            }
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                pompe.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            
            pompe.setDesignation(String.valueOf(row.getInt(Columns.ID_POMPE.toString())));
            
            pompes.put(row.getInt(Columns.ID_POMPE.toString()), pompe);

            // Set the list ByEltReseauId
            List<Pompe> listByEltReseauId = pompesByElementReseau.get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            if (listByEltReseauId == null) {
                listByEltReseauId = new ArrayList<>();
                pompesByElementReseau.put(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()), listByEltReseauId);
            }
            listByEltReseauId.add(pompe);
        }
    }
}
