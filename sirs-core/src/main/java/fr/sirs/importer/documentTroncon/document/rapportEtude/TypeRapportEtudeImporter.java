package fr.sirs.importer.documentTroncon.document.rapportEtude;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefRapportEtude;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericTypeReferenceImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeRapportEtudeImporter extends GenericTypeReferenceImporter<RefRapportEtude> {

    TypeRapportEtudeImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum Columns {
        ID_TYPE_RAPPORT_ETUDE,
        LIBELLE_TYPE_RAPPORT_ETUDE,
        ABREGE_TYPE_RAPPORT_ETUDE,
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
        return DbImporter.TableName.TYPE_RAPPORT_ETUDE.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefRapportEtude typeRapportEtude = new RefRapportEtude();
            
            typeRapportEtude.setId(typeRapportEtude.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_RAPPORT_ETUDE.toString())));
            typeRapportEtude.setLibelle(row.getString(Columns.LIBELLE_TYPE_RAPPORT_ETUDE.toString()));
            typeRapportEtude.setAbrege(row.getString(Columns.ABREGE_TYPE_RAPPORT_ETUDE.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeRapportEtude.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typeRapportEtude.setPseudoId(String.valueOf(row.getInt(Columns.ID_TYPE_RAPPORT_ETUDE.toString())));
            typeRapportEtude.setValid(true);
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_RAPPORT_ETUDE.toString())), typeRapportEtude);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
