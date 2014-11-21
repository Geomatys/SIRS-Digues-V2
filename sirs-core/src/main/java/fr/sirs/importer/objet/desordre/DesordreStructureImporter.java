package fr.sirs.importer.objet.desordre;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.core.model.DesordreStructure;
import fr.sirs.core.model.Objet;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.objet.structure.StructureImporter;
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
public class DesordreStructureImporter extends GenericImporter {

    private Map<Integer, List<DesordreStructure>> desordresStructuresByDesordreId = null;
    private Map<Integer, List<DesordreStructure>> desordresStructuresByStructureId = null;
    private final StructureImporter structureImporter;

    public DesordreStructureImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final StructureImporter structureImporter) {
        super(accessDatabase, couchDbConnector);
        this.structureImporter = structureImporter;
    }

    private enum DesordreStructureColumns {
        ID_DESORDRE,
        ID_ELEMENT_STRUCTURE,
        DATE_DERNIERE_MAJ
    };

    /**
     *
     * @return A map containing all DesordreStructure instances accessibles from the
     * internal database <em>Desordre</em> identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, List<DesordreStructure>> getDesordresStructuresByDesordreId() throws IOException, AccessDbImporterException {
        if (this.desordresStructuresByDesordreId == null) {
            compute();
        }
        return this.desordresStructuresByDesordreId;
    }

    /**
     *
     * @return A map containing all DesordreStructure instances accessibles from the
     * internal database <em>Structure</em> identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, List<DesordreStructure>> getDesordresStructuresByStructureId() throws IOException, AccessDbImporterException {
        if (this.desordresStructuresByStructureId == null) {
            compute();
        }
        return this.desordresStructuresByStructureId;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.DESORDRE_ELEMENT_STRUCTURE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        this.desordresStructuresByDesordreId = new HashMap<>();
        this.desordresStructuresByStructureId = new HashMap<>();
        
        final Map<Integer, Objet> structures = structureImporter.getStructures();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final DesordreStructure desordreStructure = new DesordreStructure();
            
            if(structures.get(row.getInt(DesordreStructureColumns.ID_ELEMENT_STRUCTURE.toString()))!=null){
                desordreStructure.setStructureId(structures.get(row.getInt(DesordreStructureColumns.ID_ELEMENT_STRUCTURE.toString())).getId());
            }
            
            if (row.getDate(DesordreStructureColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                desordreStructure.setDateMaj(LocalDateTime.parse(row.getDate(DesordreStructureColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            // Set the lists ById
            List<DesordreStructure> listByDesordreId = new ArrayList<>();
            listByDesordreId.add(desordreStructure);
            desordresStructuresByDesordreId.put(row.getInt(DesordreStructureColumns.ID_DESORDRE.toString()), listByDesordreId);
            
            List<DesordreStructure> listByStructureId = desordresStructuresByStructureId.get(row.getInt(DesordreStructureColumns.ID_ELEMENT_STRUCTURE.toString()));
            if (listByStructureId == null) {
                listByStructureId = new ArrayList<>();
            }
            listByStructureId.add(desordreStructure);
            desordresStructuresByStructureId.put(row.getInt(DesordreStructureColumns.ID_ELEMENT_STRUCTURE.toString()), listByStructureId);
            System.out.println(desordreStructure);
        }
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (DesordreStructureColumns c : DesordreStructureColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }
}
