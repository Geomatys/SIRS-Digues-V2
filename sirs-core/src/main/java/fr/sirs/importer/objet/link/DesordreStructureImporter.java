package fr.sirs.importer.objet.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Desordre;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.core.model.DesordreStructure;
import fr.sirs.core.model.Objet;
import fr.sirs.importer.objet.desordre.DesordreImporter;
import fr.sirs.importer.objet.structure.ElementStructureImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DesordreStructureImporter extends GenericObjectLinker {
    
    private final ElementStructureImporter structureImporter;
    private final DesordreImporter desordreImporter;

    public DesordreStructureImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final ElementStructureImporter structureImporter,
            final DesordreImporter desordreImporter) {
        super(accessDatabase, couchDbConnector);
        this.structureImporter = structureImporter;
        this.desordreImporter = desordreImporter;
    }

    @Override
    public void link() throws IOException, AccessDbImporterException {
        compute();
    }

    private enum Columns {
        ID_DESORDRE,
        ID_ELEMENT_STRUCTURE,
        DATE_DERNIERE_MAJ
    };

    @Override
    public String getTableName() {
        return DbImporter.TableName.DESORDRE_ELEMENT_STRUCTURE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, Objet> structures = structureImporter.getStructures();
        final Map<Integer, Desordre> desordres = desordreImporter.getStructures();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final DesordreStructure desordreStructure = new DesordreStructure();
            
            final Objet structure = structures.get(row.getInt(Columns.ID_ELEMENT_STRUCTURE.toString()));
            final Desordre desordre = desordres.get(row.getInt(Columns.ID_DESORDRE.toString()));
            
            if(structure!=null){
                desordreStructure.setStructureId(structure.getId());
            }
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                desordreStructure.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
                
            List<DesordreStructure> listByDesordre =  desordre.getDesordreStructure();
            if(listByDesordre==null) {
                listByDesordre = new ArrayList<>();
                desordre.setDesordreStructure(listByDesordre);
            }
            listByDesordre.add(desordreStructure);
        }
    }

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }
}
