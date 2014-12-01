package fr.sirs.importer.objet.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Desordre;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ObjetReferenceObjet;
import fr.sirs.importer.objet.desordre.DesordreImporter;
import fr.sirs.importer.objet.reseau.ElementReseauImporter;
import fr.sirs.importer.objet.reseau.TypeElementReseauImporter;
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
public class DesordreElementReseauImporter extends GenericObjectLinker {
    
    private final ElementReseauImporter elementReseauImporter;
    private final DesordreImporter desordreImporter;
    private final TypeElementReseauImporter typeElementReseauImporter;

    public DesordreElementReseauImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final ElementReseauImporter elementReseauImporter,
            final DesordreImporter desordreImporter) {
        super(accessDatabase, couchDbConnector);
        this.elementReseauImporter = elementReseauImporter;
        this.desordreImporter = desordreImporter;
        typeElementReseauImporter = elementReseauImporter.getTypeElementReseauImporter();
    }

    private enum Columns {
        ID_DESORDRE,
        ID_ELEMENT_RESEAU,
        ID_TYPE_ELEMENT_RESEAU,
        DATE_DERNIERE_MAJ
    };

    @Override
    public String getTableName() {
        return DbImporter.TableName.DESORDRE_ELEMENT_RESEAU.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, Objet> elementsReseaux = elementReseauImporter.getStructures();
        final Map<Integer, Desordre> desordres = desordreImporter.getStructures();
        final Map<Integer, Class> classesElementReseaux = typeElementReseauImporter.getTypes();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final ObjetReferenceObjet elementReseauDesordre = new ObjetReferenceObjet();
            
            final Objet elementReseau = elementsReseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            final Class classeElementReseau = classesElementReseaux.get(row.getInt(Columns.ID_TYPE_ELEMENT_RESEAU.toString()));
            final Desordre desordre = desordres.get(row.getInt(Columns.ID_DESORDRE.toString()));
            
            if(elementReseau!=null && desordre!=null){

                if(elementReseau.getClass() != classeElementReseau){
                    throw new AccessDbImporterException("Bad type !");
                }
            
                if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                    elementReseauDesordre.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
                }
                
                elementReseauDesordre.setObjetId(elementReseau.getId());
                
                List<ObjetReferenceObjet> listByDesordre =  desordre.getObjet();
                if(listByDesordre==null) {
                    listByDesordre = new ArrayList<>();
                    desordre.setObjet(listByDesordre);
                }
                listByDesordre.add(elementReseauDesordre);
            }
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
