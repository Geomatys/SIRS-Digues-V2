package fr.sirs.importer.objet.geometry;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefProfilFrancBord;
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
public class TypeProfilFrontFrancBordImporter extends GenericImporter {

    private Map<Integer, RefProfilFrancBord> typesProfil = null;
    
    
    TypeProfilFrontFrancBordImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum TypeLargeurFrancBordColumns {
        ID_TYPE_PROFIL_FB,
        ABREGE_TYPE_PROFIL_FB,
        LIBELLE_TYPE_PROFIL_FB,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefProfilFrancBord referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefProfilFrancBord> getTypeLargeur() throws IOException {
        if(typesProfil == null) compute();
        return typesProfil;
    }
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeLargeurFrancBordColumns c : TypeLargeurFrancBordColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_PROFIL_FRANC_BORD.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesProfil = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefProfilFrancBord typeProfil = new RefProfilFrancBord();
            
            typeProfil.setLibelle(row.getString(TypeLargeurFrancBordColumns.LIBELLE_TYPE_PROFIL_FB.toString()));
            typeProfil.setAbrege(row.getString(TypeLargeurFrancBordColumns.ABREGE_TYPE_PROFIL_FB.toString()));
            if (row.getDate(TypeLargeurFrancBordColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeProfil.setDateMaj(LocalDateTime.parse(row.getDate(TypeLargeurFrancBordColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesProfil.put(row.getInt(String.valueOf(TypeLargeurFrancBordColumns.ID_TYPE_PROFIL_FB.toString())), typeProfil);
        }
        couchDbConnector.executeBulk(typesProfil.values());
    }
    
}
