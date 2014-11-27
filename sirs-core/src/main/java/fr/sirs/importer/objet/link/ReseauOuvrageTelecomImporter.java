package fr.sirs.importer.objet.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauReseau;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.StationPompage;
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
public class ReseauOuvrageTelecomImporter extends GenericObjectLinker {

    private final ElementReseauImporter reseauImpoter;
    
    public ReseauOuvrageTelecomImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final ElementReseauImporter reseauImpoter) {
        super(accessDatabase, couchDbConnector);
        this.reseauImpoter = reseauImpoter;
    }

    @Override
    public void link() throws IOException, AccessDbImporterException {
        compute();
    }

    private enum ElementReseauConduiteFermeeColumns {
        ID_ELEMENT_RESEAU,
        ID_ELEMENT_RESEAU_OUVRAGE_TEL_NRJ,
        DATE_DERNIERE_MAJ
    };
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (ElementReseauConduiteFermeeColumns c : ElementReseauConduiteFermeeColumns.values()) {
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
            final ReseauReseau ouvrageReseau = new ReseauReseau();
            final ReseauReseau reseauReseau = new ReseauReseau();
            
            
            if (row.getDate(ElementReseauConduiteFermeeColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                ouvrageReseau.setDateMaj(LocalDateTime.parse(row.getDate(ElementReseauConduiteFermeeColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
                reseauReseau.setDateMaj(LocalDateTime.parse(row.getDate(ElementReseauConduiteFermeeColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }

            final OuvrageTelecomEnergie ouvrage = (OuvrageTelecomEnergie) reseaux.get(row.getInt(ElementReseauConduiteFermeeColumns.ID_ELEMENT_RESEAU_OUVRAGE_TEL_NRJ.toString()));
            if(ouvrage!=null){
                reseauReseau.setReseauId(cleanNullString(ouvrage.getId()));

                List<ReseauReseau> listReseauOuvrage = ouvrage.getReseau();
                if (listReseauOuvrage == null) {
                    listReseauOuvrage = new ArrayList<>();
                    ouvrage.setReseau(listReseauOuvrage);
                }
                listReseauOuvrage.add(ouvrageReseau);
            }
            
            final ReseauTelecomEnergie objet = (ReseauTelecomEnergie) reseaux.get(row.getInt(ElementReseauConduiteFermeeColumns.ID_ELEMENT_RESEAU.toString()));
                
            if(objet!=null){
                ouvrageReseau.setReseauId(cleanNullString(objet.getId()));

                if(objet instanceof ReseauTelecomEnergie){
                    final ReseauTelecomEnergie reseau = (ReseauTelecomEnergie) objet;
                    List<ReseauReseau> listReseauReseau = reseau.getReseau();
                    if (listReseauReseau == null) {
                        listReseauReseau = new ArrayList<>();
                        reseau.setReseau(listReseauReseau);
                    }
                    listReseauReseau.add(reseauReseau);
                }
//                else if(objet instanceof StationPompage){
//                    final StationPompage reseau = (StationPompage) objet;
//                    List<ReseauReseau> listReseauReseau = reseau.getReseau();
//                    if (listReseauReseau == null) {
//                        listReseauReseau = new ArrayList<>();
//                        reseau.setReseau(listReseauReseau);
//                    }
//                    listReseauReseau.add(reseauReseau);
//                }
                else {
                    System.out.println("Type de réseau non pris en charge : "+reseaux.get(row.getInt(ElementReseauConduiteFermeeColumns.ID_ELEMENT_RESEAU.toString())).getClass());
                }
                
//                System.out.println("Objet : "+objet);
//                System.out.println("Ouvrage-Réseau : "+ouvrageReseau);
//                System.out.println("Réseau-Réseau : "+reseauReseau);
            }
            else {
//                System.out.println("Autre instance : "+objet);
            }
        }
    }
}
