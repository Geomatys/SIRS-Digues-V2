package fr.sirs.importer.v2.document.profil;

import fr.sirs.core.model.ProfilTravers;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.AbstractImporter;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ProfilTraversImporter extends AbstractImporter<ProfilTravers> {

    @Override
    public Class<ProfilTravers> getElementClass() {
        return ProfilTravers.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.PROFIL_EN_TRAVERS.toString();
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_PROFIL_EN_TRAVERS";
    }
}
