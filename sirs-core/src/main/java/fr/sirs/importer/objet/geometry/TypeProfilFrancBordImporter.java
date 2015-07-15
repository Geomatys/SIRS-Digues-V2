package fr.sirs.importer.objet.geometry;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefProfilFrancBord;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeProfilFrancBordImporter extends GenericTypeReferenceImporter<RefProfilFrancBord> {
    
    TypeProfilFrancBordImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_TYPE_PROFIL_FB,
        ABREGE_TYPE_PROFIL_FB,
        LIBELLE_TYPE_PROFIL_FB,
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
        return TYPE_PROFIL_FRANC_BORD.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefProfilFrancBord typeProfil = createAnonymValidElement(RefProfilFrancBord.class);
            
            typeProfil.setId(typeProfil.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_PROFIL_FB.toString())));
            typeProfil.setLibelle(row.getString(Columns.LIBELLE_TYPE_PROFIL_FB.toString()));
            typeProfil.setAbrege(row.getString(Columns.ABREGE_TYPE_PROFIL_FB.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeProfil.setDateMaj(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            typeProfil.setDesignation(String.valueOf(row.getInt(Columns.ID_TYPE_PROFIL_FB.toString())));
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_PROFIL_FB.toString())), typeProfil);
        }
        couchDbConnector.executeBulk(types.values());
    }
    
}
