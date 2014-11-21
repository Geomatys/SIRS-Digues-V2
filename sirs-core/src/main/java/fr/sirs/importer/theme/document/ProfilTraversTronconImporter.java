package fr.sirs.importer.theme.document;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Document;
import fr.sirs.core.model.ProfilTraversTroncon;
import fr.sirs.importer.AccessDbImporterException;
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
public class ProfilTraversTronconImporter extends GenericImporter {

    private Map<Integer, List<ProfilTraversTroncon>> profilTraversTronconsByLeve = null;
    private DocumentImporter documentImporter;
    
    private ProfilTraversTronconImporter(final Database accessDatabase, final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    ProfilTraversTronconImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final DocumentImporter documentImporter) {
        this(accessDatabase, couchDbConnector);
        this.documentImporter = documentImporter;
    }
    
    public Map<Integer, List<ProfilTraversTroncon>> getProfilTraversTronconByLeveId() throws IOException, AccessDbImporterException{
        if(profilTraversTronconsByLeve==null) compute();
        return profilTraversTronconsByLeve;
    }
    
    private enum ProfilTraversTronconColumns {
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
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (ProfilTraversTronconColumns c : ProfilTraversTronconColumns.values()) {
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
        profilTraversTronconsByLeve = new HashMap<>();
        
        final Map<Integer, Document> documents = documentImporter.getPrecomputedDocuments();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final ProfilTraversTroncon profilTraversTroncon = new ProfilTraversTroncon();
            
            if(documents.get(row.getInt(ProfilTraversTronconColumns.ID_DOC.toString()))!=null){
                profilTraversTroncon.setDocumentProfilTraversId(documents.get(row.getInt(ProfilTraversTronconColumns.ID_DOC.toString())).getId());
            }
            
            if(row.getDouble(ProfilTraversTronconColumns.COTE_RIVIERE_Z_NGF_PIED_DE_DIGUE.toString())!=null){
                profilTraversTroncon.setCoteRivierePiedDigue(row.getDouble(ProfilTraversTronconColumns.COTE_RIVIERE_Z_NGF_PIED_DE_DIGUE.toString()).floatValue());
            }
            
            if(row.getDouble(ProfilTraversTronconColumns.COTE_RIVIERE_Z_NGF_SOMMET_RISBERME.toString())!=null){
                profilTraversTroncon.setCoteRiviereSommetRisberme(row.getDouble(ProfilTraversTronconColumns.COTE_RIVIERE_Z_NGF_SOMMET_RISBERME.toString()).floatValue());
            }
            
            if(row.getDouble(ProfilTraversTronconColumns.CRETE_Z_NGF.toString())!=null){
                profilTraversTroncon.setCoteCrete(row.getDouble(ProfilTraversTronconColumns.CRETE_Z_NGF.toString()).floatValue());
            }
            
            if(row.getDouble(ProfilTraversTronconColumns.COTE_TERRE_Z_NGF_SOMMET_RISBERME.toString())!=null){
                profilTraversTroncon.setCoteTerreSommetRisberme(row.getDouble(ProfilTraversTronconColumns.COTE_TERRE_Z_NGF_SOMMET_RISBERME.toString()).floatValue());
            }
            
            if(row.getDouble(ProfilTraversTronconColumns.COTE_TERRE_Z_NGF_PIED_DE_DIGUE.toString())!=null){
                profilTraversTroncon.setCoteTterrePiedDigue(row.getDouble(ProfilTraversTronconColumns.COTE_TERRE_Z_NGF_PIED_DE_DIGUE.toString()).floatValue());
            }
            
            if (row.getDate(ProfilTraversTronconColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                profilTraversTroncon.setDateMaj(LocalDateTime.parse(row.getDate(ProfilTraversTronconColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            if(row.getDouble(ProfilTraversTronconColumns.CRETE_LARGEUR.toString())!=null){
                profilTraversTroncon.setCreteLargeur(row.getDouble(ProfilTraversTronconColumns.CRETE_LARGEUR.toString()).floatValue());
            }
            
            List<ProfilTraversTroncon> listByLeve = profilTraversTronconsByLeve.get(row.getInt(ProfilTraversTronconColumns.ID_PROFIL_EN_TRAVERS_LEVE.toString()));
            if (listByLeve == null) {
                listByLeve = new ArrayList<>();
            }
            listByLeve.add(profilTraversTroncon);
            profilTraversTronconsByLeve.put(row.getInt(ProfilTraversTronconColumns.ID_PROFIL_EN_TRAVERS_LEVE.toString()), listByLeve);
        }
    }
}
