package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefFonction;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class TypeFonctionImporter extends GenericTypeReferenceImporter<RefFonction> {

    @Override
    public String getTableName() {
        return TYPE_FONCTION.toString();
    }

    @Override
    protected Class<RefFonction> getElementClass() {
        return RefFonction.class;
    }
}
