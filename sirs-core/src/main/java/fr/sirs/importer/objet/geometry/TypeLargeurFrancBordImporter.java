package fr.sirs.importer.objet.geometry;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefLargeurFrancBord;
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
class TypeLargeurFrancBordImporter extends GenericImporter {

    private Map<Integer, RefLargeurFrancBord> typesLargeur = null;
    
    
    TypeLargeurFrancBordImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum TypeLargeurFrancBordColumns {
        ID_TYPE_LARGEUR_FB,
        ABREGE_TYPE_LARGEUR_FB,
        LIBELLE_TYPE_LARGEUR_FB,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefLargeurFrancBord referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefLargeurFrancBord> getTypeLargeur() throws IOException {
        if(typesLargeur == null) compute();
        return typesLargeur;
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
        return DbImporter.TableName.TYPE_LARGEUR_FRANC_BORD.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesLargeur = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefLargeurFrancBord typeLargeur = new RefLargeurFrancBord();
            
            typeLargeur.setLibelle(row.getString(TypeLargeurFrancBordColumns.LIBELLE_TYPE_LARGEUR_FB.toString()));
            typeLargeur.setAbrege(row.getString(TypeLargeurFrancBordColumns.ABREGE_TYPE_LARGEUR_FB.toString()));
            if (row.getDate(TypeLargeurFrancBordColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeLargeur.setDateMaj(LocalDateTime.parse(row.getDate(TypeLargeurFrancBordColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesLargeur.put(row.getInt(String.valueOf(TypeLargeurFrancBordColumns.ID_TYPE_LARGEUR_FB.toString())), typeLargeur);
        }
        couchDbConnector.executeBulk(typesLargeur.values());
    }
    
}
