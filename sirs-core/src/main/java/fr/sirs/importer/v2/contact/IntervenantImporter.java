package fr.sirs.importer.v2.contact;

import fr.sirs.core.model.Contact;
import static fr.sirs.importer.DbImporter.TableName.INTERVENANT;
import fr.sirs.importer.v2.AbstractImporter;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class IntervenantImporter extends AbstractImporter<Contact> {

    @Override
    public Class<Contact> getElementClass() {
        return Contact.class;
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_INTERVENANT";
    }

    @Override
    public String getTableName() {
        return INTERVENANT.toString();
    }
}
