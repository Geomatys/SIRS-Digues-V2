package fr.sirs.importer.objet.ligneEau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.MesureLigneEau;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
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
class LigneEauMesuresPrzImporter extends GenericImporter {

    private Map<Integer, List<MesureLigneEau>> mesuresByLigneEau = null;

    LigneEauMesuresPrzImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_LIGNE_EAU,
//        PR_SAISI,
        HAUTEUR_EAU,
//        PR_CALCULE,
//        ID_POINT,
        DATE_DERNIERE_MAJ,
    };

    /**
     *
     * @return A map containing all the MesureLigneEau elements
     * referenced by the corresponding element reseau internal ID.
     * @throws IOException
     */
    public Map<Integer, List<MesureLigneEau>> getMesuresByLigneEau() throws IOException {
        if (mesuresByLigneEau == null) {
            compute();
        }
        return mesuresByLigneEau;
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
        return DbImporter.TableName.LIGNE_EAU_MESURES_PRZ.toString();
    }

    @Override
    protected void compute() throws IOException {
        mesuresByLigneEau = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final MesureLigneEau mesure = new MesureLigneEau();
            
            if (row.getDouble(Columns.HAUTEUR_EAU.toString()) != null) {
                mesure.setHauteur(row.getDouble(Columns.HAUTEUR_EAU.toString()).floatValue());
            }
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                mesure.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }

            // Pas d'ID : on met arbitrairement celui de la ligne d'eau comme pseudo id.
            mesure.setDesignation(String.valueOf(row.getInt(Columns.ID_LIGNE_EAU.toString())));
            mesure.setValid(true);
            
            // Set the list ByLigneEauId
            List<MesureLigneEau> listByEltReseauId = mesuresByLigneEau.get(row.getInt(Columns.ID_LIGNE_EAU.toString()));
            if (listByEltReseauId == null) {
                listByEltReseauId = new ArrayList<>();
                mesuresByLigneEau.put(row.getInt(Columns.ID_LIGNE_EAU.toString()), listByEltReseauId);
            }
            listByEltReseauId.add(mesure);
        }
    }
}
