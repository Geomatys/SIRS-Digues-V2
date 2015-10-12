package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefSystemeReleveProfil;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class TypeSystemeReleveProfilImporter extends GenericTypeReferenceImporter<RefSystemeReleveProfil> {

    @Override
    public String getTableName() {
        return TYPE_SYSTEME_RELEVE_PROFIL.toString();
    }

    @Override
    public Class<RefSystemeReleveProfil> getElementClass() {
        return RefSystemeReleveProfil.class;
    }
}
