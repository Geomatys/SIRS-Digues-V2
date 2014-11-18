package fr.sirs.importer.theme.document.related;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.LeveeProfilTravers;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.RefSystemeReleveProfil;
import fr.sirs.core.model.RefTypeProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.OrganismeImporter;
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
public class ProfilTraversDescriptionImporter extends GenericImporter {

    private Map<Integer, LeveeProfilTravers> levees = null;
    private Map<Integer, List<LeveeProfilTravers>> leveesByProfil = null;
    private TypeSystemeReleveProfilImporter typeSystemeReleveProfilImporter;
    private TypeProfilTraversImporter typeProfilTraversImporter;
    private OrganismeImporter organismeImporter;
    
    private ProfilTraversDescriptionImporter(final Database accessDatabase, final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    public ProfilTraversDescriptionImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TypeSystemeReleveProfilImporter typeSystemeReleveProfilImporter,
            final TypeProfilTraversImporter typeProfilTraversImporter,
            final OrganismeImporter organismeImporter) {
        this(accessDatabase, couchDbConnector);
        this.typeSystemeReleveProfilImporter = typeSystemeReleveProfilImporter;
        this.typeProfilTraversImporter = typeProfilTraversImporter;
        this.organismeImporter = organismeImporter;
    }
    
    public Map<Integer, LeveeProfilTravers> getLeveeProfilTravers() throws IOException, AccessDbImporterException{
        if(levees==null) compute();
        return levees;
    }
    
    public Map<Integer, List<LeveeProfilTravers>> getLeveeProfilTraversByProfilId() throws IOException, AccessDbImporterException{
        if(leveesByProfil==null) compute();
        return leveesByProfil;
    }
    
    private enum ProfilTraversDescriptionColumns {
        ID_PROFIL_EN_TRAVERS_LEVE,
        ID_PROFIL_EN_TRAVERS,
        DATE_LEVE,
        ID_ORG_CREATEUR,
        ID_TYPE_SYSTEME_RELEVE_PROFIL,
        REFERENCE_PAPIER,
        REFERENCE_NUMERIQUE,
        REFERENCE_CALQUE,
        ID_TYPE_PROFIL_EN_TRAVERS,
//        ID_TYPE_ORIGINE_PROFIL_EN_TRAVERS,
//        ID_DOC_RAPPORT_ETUDES,
        COMMENTAIRE,
//        NOM_FICHIER_PLAN_ENSEMBLE, // Pas dans le nouveau modèle
//        NOM_FICHIER_COUPE_IMAGE, // Pas dans le nouveau modèle
        DATE_DERNIERE_MAJ
    }
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (ProfilTraversDescriptionColumns c : ProfilTraversDescriptionColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.PROFIL_EN_TRAVERS_DESCRIPTION.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        levees = new HashMap<>();
        leveesByProfil = new HashMap<>();
        
        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        final Map<Integer, RefSystemeReleveProfil> systemesReleve = typeSystemeReleveProfilImporter.getTypeSystemeReleve();
        final Map<Integer, RefTypeProfilTravers> typesProfil = typeProfilTraversImporter.getTypeProfilTravers();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final LeveeProfilTravers levee = new LeveeProfilTravers();
            
            if (row.getDate(ProfilTraversDescriptionColumns.DATE_LEVE.toString()) != null) {
                levee.setDateLevee(LocalDateTime.parse(row.getDate(ProfilTraversDescriptionColumns.DATE_LEVE.toString()).toString(), dateTimeFormatter));
            }
            
            final Organisme organisme = organismes.get(row.getInt(ProfilTraversDescriptionColumns.ID_ORG_CREATEUR.toString()));
            if(organisme!=null){
                levee.setOrganismeCreateurId(organisme.getId());
            }
            
            levee.setTypeSystemesReleveId(systemesReleve.get(row.getInt(ProfilTraversDescriptionColumns.ID_TYPE_SYSTEME_RELEVE_PROFIL.toString())).getId());
            
            levee.setReference_papier(row.getString(ProfilTraversDescriptionColumns.REFERENCE_PAPIER.toString()));
            
            levee.setReference_numerique(row.getString(ProfilTraversDescriptionColumns.REFERENCE_NUMERIQUE.toString()));
            
            levee.setReference_calque(row.getString(ProfilTraversDescriptionColumns.REFERENCE_CALQUE.toString()));
            
            levee.setTypeProfilId(typesProfil.get(row.getInt(ProfilTraversDescriptionColumns.ID_TYPE_PROFIL_EN_TRAVERS.toString())).getId());
            
            levee.setCommentaire(row.getString(ProfilTraversDescriptionColumns.COMMENTAIRE.toString()));
            
            if (row.getDate(ProfilTraversDescriptionColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                levee.setDateMaj(LocalDateTime.parse(row.getDate(ProfilTraversDescriptionColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            levees.put(row.getInt(ProfilTraversDescriptionColumns.ID_PROFIL_EN_TRAVERS_LEVE.toString()), levee);
            
            List<LeveeProfilTravers> listByProfil = leveesByProfil.get(row.getInt(ProfilTraversDescriptionColumns.ID_PROFIL_EN_TRAVERS.toString()));
            if (listByProfil == null) {
                listByProfil = new ArrayList<>();
            }
            listByProfil.add(levee);
            leveesByProfil.put(row.getInt(ProfilTraversDescriptionColumns.ID_PROFIL_EN_TRAVERS.toString()), listByProfil);
        }
    }
    
}
