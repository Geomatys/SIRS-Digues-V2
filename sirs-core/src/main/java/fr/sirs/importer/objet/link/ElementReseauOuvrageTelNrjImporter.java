package fr.sirs.importer.objet.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ObjetReferenceObjet;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.objet.reseau.ElementReseauImporter;
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
public class ElementReseauOuvrageTelNrjImporter extends GenericObjectLinker {

    private final ElementReseauImporter reseauImpoter;
    
    public ElementReseauOuvrageTelNrjImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final ElementReseauImporter reseauImpoter) {
        super(accessDatabase, couchDbConnector);
        this.reseauImpoter = reseauImpoter;
    }

    private enum Columns {
        ID_ELEMENT_RESEAU,
        ID_ELEMENT_RESEAU_OUVRAGE_TEL_NRJ,
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
        return DbImporter.TableName.ELEMENT_RESEAU_OUVRAGE_TEL_NRJ.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, Objet> reseaux = reseauImpoter.getStructures();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final ObjetReferenceObjet ouvrageReseau = new ObjetReferenceObjet();
            final ObjetReferenceObjet reseauReseau = new ObjetReferenceObjet();
            final OuvrageTelecomEnergie ouvrage = (OuvrageTelecomEnergie) reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU_OUVRAGE_TEL_NRJ.toString()));
            final ReseauTelecomEnergie reseau = (ReseauTelecomEnergie) reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            
            if(ouvrage!=null && reseau!=null){
                if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                    ouvrageReseau.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
                    reseauReseau.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
                }
                
                ouvrageReseau.setObjetId(cleanNullString(reseau.getId()));
                reseauReseau.setObjetId(cleanNullString(ouvrage.getId()));

                List<ObjetReferenceObjet> listReseauOuvrage = ouvrage.getObjet();
                if (listReseauOuvrage == null) {
                    listReseauOuvrage = new ArrayList<>();
                    ouvrage.setObjet(listReseauOuvrage);
                }
                listReseauOuvrage.add(ouvrageReseau);
                
                List<ObjetReferenceObjet> listReseauReseau = reseau.getObjet();
                if (listReseauReseau == null) {
                    listReseauReseau = new ArrayList<>();
                    reseau.setObjet(listReseauReseau);
                }
                listReseauReseau.add(reseauReseau);
            }
        }
    }
}
