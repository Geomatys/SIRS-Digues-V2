package fr.sirs.importer.objet.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ObjetReferenceObjet;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.VoieAcces;
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
public class ElementReseauCheminAccessImporter extends GenericObjectLinker {

    private final ElementReseauImporter reseauImpoter;
    
    public ElementReseauCheminAccessImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final ElementReseauImporter reseauImpoter) {
        super(accessDatabase, couchDbConnector);
        this.reseauImpoter = reseauImpoter;
    }

    private enum Columns {
        ID_ELEMENT_RESEAU,
        ID_ELEMENT_RESEAU_CHEMIN_ACCES,
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
        return DbImporter.TableName.ELEMENT_RESEAU_CHEMIN_ACCES.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, Objet> reseaux = reseauImpoter.getStructures();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final ObjetReferenceObjet referenceReseauFerme = new ObjetReferenceObjet();
            final ObjetReferenceObjet referenceOuvrageAssocie = new ObjetReferenceObjet();
            
            final VoieAcces ouvrageHydrauliqueAssocie = (VoieAcces) reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU_CHEMIN_ACCES.toString()));
            final Objet reseauHydrau = reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            
            if(ouvrageHydrauliqueAssocie!=null && reseauHydrau!=null){

//                if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
//                    referenceReseauFerme.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
//                    referenceOuvrageAssocie.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
//                }
//                
//                referenceReseauFerme.setObjetId(cleanNullString(reseauHydrau.getId()));
//                referenceOuvrageAssocie.setObjetId(cleanNullString(ouvrageHydrauliqueAssocie.getId()));
//
//                List<ObjetReferenceObjet> listReseauFerme = ouvrageHydrauliqueAssocie.getObjet();
//                if (listReseauFerme == null) {
//                    listReseauFerme = new ArrayList<>();
//                    ouvrageHydrauliqueAssocie.setObjet(listReseauFerme);
//                }
//                listReseauFerme.add(referenceReseauFerme);
//
//                List<ObjetReferenceObjet> listOuvrageAssocie = reseauHydrau.getObjet();
//                if (listOuvrageAssocie == null) {
//                    listOuvrageAssocie = new ArrayList<>();
//                    reseauHydrau.setObjet(listOuvrageAssocie);
//                }
//                listOuvrageAssocie.add(referenceOuvrageAssocie);
            }
        }
    }
}
