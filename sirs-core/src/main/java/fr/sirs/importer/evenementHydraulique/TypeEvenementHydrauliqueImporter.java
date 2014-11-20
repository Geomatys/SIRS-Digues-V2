package fr.sirs.importer.evenementHydraulique;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.RefEvenementHydrauliqueRepository;
import fr.sirs.core.component.RefTypeDesordreRepository;
import fr.sirs.core.model.RefEvenementHydraulique;
import fr.sirs.core.model.RefTypeDesordre;
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
public class TypeEvenementHydrauliqueImporter extends GenericImporter {

    private Map<Integer, RefEvenementHydraulique> typesEvenement = null;
//    private Map<Integer, Class> classesEvenement = null;

    public TypeEvenementHydrauliqueImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final RefEvenementHydrauliqueRepository refEvenementHydrauliqueRepository) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum TypeEvenementHydrauliqueColumns {
        ID_TYPE_EVENEMENT_HYDRAU,
        ABREGE_TYPE_EVENEMENT_HYDRAU,
        LIBELLE_TYPE_EVENEMENT_HYDRAU,
//        NOM_TABLE_EVT, // Ces table sont innexistentes dans la base de l'isère
//        ID_TYPE_OBJET_CARTO,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the RefEvenementHydraulique referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefEvenementHydraulique> getTypeEvenementHydraulique() throws IOException {
        if(typesEvenement == null) compute();
        return typesEvenement;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeEvenementHydrauliqueColumns c : TypeEvenementHydrauliqueColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_EVENEMENT_HYDRAU.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesEvenement = new HashMap<>();
//        classesEvenement = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefEvenementHydraulique typeEvenement = new RefEvenementHydraulique();
            
            typeEvenement.setLibelle(row.getString(TypeEvenementHydrauliqueColumns.LIBELLE_TYPE_EVENEMENT_HYDRAU.toString()));
            typeEvenement.setAbrege(row.getString(TypeEvenementHydrauliqueColumns.ABREGE_TYPE_EVENEMENT_HYDRAU.toString()));
            if (row.getDate(TypeEvenementHydrauliqueColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeEvenement.setDateMaj(LocalDateTime.parse(row.getDate(TypeEvenementHydrauliqueColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            try{
//                final Class classe;
//                final DbImporter.TableName table = DbImporter.TableName.valueOf(row.getString(TypeEvenementHydrauliqueColumns.NOM_TABLE_EVT.toString()));
//
//                switch(table){
//    //                case SYS_CRUE_OBSERVEE: break;
//    //                case SIMULATION_HYDRAU: break;
//                    default: classe = null;
//                }
//
//                classesEvenement.put(row.getInt(String.valueOf(TypeEvenementHydrauliqueColumns.ID_TYPE_EVENEMENT_HYDRAU.toString())), classe);
                typesEvenement.put(row.getInt(String.valueOf(TypeEvenementHydrauliqueColumns.ID_TYPE_EVENEMENT_HYDRAU.toString())), typeEvenement);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
        couchDbConnector.executeBulk(typesEvenement.values());
    }
}
