package fr.sirs.importer.theme.document.related;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.core.component.ProfilTraversRepository;
import fr.sirs.core.model.LeveeProfilTravers;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ProfilTraversDescriptionImporter extends GenericImporter {

    private Map<Integer, LeveeProfilTravers> levees = null;
    private ProfilTraversRepository profilTraversRepository;
    private TypeProfilTraversImporter typeProfilTraversImporter;
    
    public ProfilTraversDescriptionImporter(final Database accessDatabase, final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    public ProfilTraversDescriptionImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final ProfilTraversRepository profilTraversRepository,
            final TypeProfilTraversImporter typeProfilTraversImporter){
        this(accessDatabase, couchDbConnector);
        this.profilTraversRepository = profilTraversRepository;
        this.typeProfilTraversImporter = typeProfilTraversImporter;
    }
    
    public Map<Integer, LeveeProfilTravers> getLeveeProfilTravers() throws IOException, AccessDbImporterException{
        if(levees==null) compute();
        return levees;
    }
    
    private enum ProfilTraversDescriptionColumns {
//        ID_PROFIL_EN_TRAVERS_LEVE,
//        ID_PROFIL_EN_TRAVERS,
//        DATE_LEVE,
//        ID_ORG_CREATEUR,
//        ID_TYPE_SYSTEME_RELEVE_PROFIL,
//        REFERENCE_PAPIER,
//        REFERENCE_NUMERIQUE,
//        REFERENCE_CALQUE,
//        ID_TYPE_PROFIL_EN_TRAVERS,
//        ID_TYPE_ORIGINE_PROFIL_EN_TRAVERS,
//        ID_DOC_RAPPORT_ETUDES,
//        COMMENTAIRE,
//        NOM_FICHIER_PLAN_ENSEMBLE,
//        NOM_FICHIER_COUPE_IMAGE,
//        DATE_DERNIERE_MAJ
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
    
    }
    
}
