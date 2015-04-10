package fr.sirs.importer.documentTroncon.document.profilTravers;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.LevePositionProfilTravers;
import fr.sirs.core.model.LeveProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ProfilEnTraversTronconImporter extends GenericImporter {

    private Map<Integer, List<LevePositionProfilTravers>> byLocalisationId = null;
    private Collection<LevePositionProfilTravers> levesPositionProfilTravers = null;
//    private DocumentImporter documentImporter;
    private ProfilEnTraversDescriptionImporter profilTraversDescriptionImporter;

    ProfilEnTraversTronconImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final ProfilEnTraversDescriptionImporter profilTraversDescriptionImporter) {
        super(accessDatabase, couchDbConnector);
//        this.documentImporter = documentImporter;
        this.profilTraversDescriptionImporter = profilTraversDescriptionImporter;
    }
    
    public Map<Integer, List<LevePositionProfilTravers>> getByLocalisationId() throws IOException, AccessDbImporterException{
        if(byLocalisationId==null) compute();
        return byLocalisationId;
    }
    
    private enum Columns {
        ID_PROFIL_EN_TRAVERS_LEVE,
        ID_DOC,
        COTE_RIVIERE_Z_NGF_PIED_DE_DIGUE,
        COTE_RIVIERE_Z_NGF_SOMMET_RISBERME,
        CRETE_Z_NGF,
        COTE_TERRE_Z_NGF_SOMMET_RISBERME,
        COTE_TERRE_Z_NGF_PIED_DE_DIGUE,
        DATE_DERNIERE_MAJ,
        CRETE_LARGEUR
    }
    
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
        return DbImporter.TableName.PROFIL_EN_TRAVERS_TRONCON.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        byLocalisationId = new HashMap<>();
        levesPositionProfilTravers = new ArrayList<>();
        
//        final Map<Integer, AbstractDocumentTroncon> documents = documentImporter.getPrecomputedDocuments();
        final Map<Integer, LeveProfilTravers> leves = profilTraversDescriptionImporter.getLeveProfilTravers();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final LevePositionProfilTravers levePositionProfilTravers = new LevePositionProfilTravers();
            
            if (row.getDouble(Columns.COTE_RIVIERE_Z_NGF_PIED_DE_DIGUE.toString()) != null) {
                levePositionProfilTravers.setCotePiedDigueRiviere(row.getDouble(Columns.COTE_RIVIERE_Z_NGF_PIED_DE_DIGUE.toString()));
            }

            if (row.getDouble(Columns.COTE_RIVIERE_Z_NGF_SOMMET_RISBERME.toString()) != null) {
                levePositionProfilTravers.setCoteSommetRisbermeRiviere(row.getDouble(Columns.COTE_RIVIERE_Z_NGF_SOMMET_RISBERME.toString()));
            }

            if (row.getDouble(Columns.CRETE_Z_NGF.toString()) != null) {
                levePositionProfilTravers.setCoteCrete(row.getDouble(Columns.CRETE_Z_NGF.toString()));
            }

            if (row.getDouble(Columns.COTE_TERRE_Z_NGF_SOMMET_RISBERME.toString()) != null) {
                levePositionProfilTravers.setCoteSommetRisbermeTerre(row.getDouble(Columns.COTE_TERRE_Z_NGF_SOMMET_RISBERME.toString()));
            }

            if (row.getDouble(Columns.COTE_TERRE_Z_NGF_PIED_DE_DIGUE.toString()) != null) {
                levePositionProfilTravers.setCotePiedDigueTerre(row.getDouble(Columns.COTE_TERRE_Z_NGF_PIED_DE_DIGUE.toString()));
            }

            if (row.getDouble(Columns.CRETE_LARGEUR.toString()) != null) {
                levePositionProfilTravers.setLargeurCrete(row.getDouble(Columns.CRETE_LARGEUR.toString()));
            }

            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                levePositionProfilTravers.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            levePositionProfilTravers.setLeveId(leves.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString())).getId());
            
            // Faute d'identifiant spécifique, on attribue celui du levé par défaut
            levePositionProfilTravers.setDesignation(
                    String.valueOf(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString()))+"/"+String.valueOf(row.getInt(Columns.ID_DOC.toString()))
            );
            levePositionProfilTravers.setValid(true);

            levesPositionProfilTravers.add(levePositionProfilTravers);
            
            List<LevePositionProfilTravers> listByLocalisationId = byLocalisationId.get(row.getInt(Columns.ID_DOC.toString()));
            if (listByLocalisationId == null) {
                listByLocalisationId = new ArrayList<>();
                byLocalisationId.put(row.getInt(Columns.ID_DOC.toString()), listByLocalisationId);
            }
            listByLocalisationId.add(levePositionProfilTravers);
            
//            AbstractDocumentTroncon[] docTroncons = documentTronconsByLeve.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString()));
//            if(docTroncons==null){
//                docTroncons = new AbstractDocumentTroncon[2];
//                docTroncons[0]=documents.get(row.getInt(Columns.ID_DOC.toString()));//row.getInt(Columns.ID_DOC.toString());
//                documentTronconsByLeve.put(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString()), docTroncons);
//            }
//            else{
//                docTroncons[1]=documents.get(row.getInt(Columns.ID_DOC.toString()));//row.getInt(Columns.ID_DOC.toString());
//            }
        }
        couchDbConnector.executeBulk(levesPositionProfilTravers);
    }
}
