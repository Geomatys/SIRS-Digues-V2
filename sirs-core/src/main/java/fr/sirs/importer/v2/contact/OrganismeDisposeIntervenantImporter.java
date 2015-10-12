package fr.sirs.importer.v2.contact;

import fr.sirs.importer.*;
import fr.sirs.core.model.ContactOrganisme;
import fr.sirs.core.model.Organisme;
import fr.sirs.importer.v2.SimpleUpdater;
import org.springframework.stereotype.Component;

/**
 * @author Alexis Manin (Geometys)
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class OrganismeDisposeIntervenantImporter extends SimpleUpdater<ContactOrganisme, Organisme> {

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_INTERVENANT.name();
    }

    @Override
    public String getDocumentIdField() {
        return Columns.ID_ORGANISME.name();
    }

    @Override
    public void put(Organisme container, ContactOrganisme toPut) {
        container.contactOrganisme.add(toPut);
    }

    @Override
    public Class<Organisme> getDocumentClass() {
        return Organisme.class;
    }

    private enum Columns {
        ID_ORGANISME,
        ID_INTERVENANT
    };

    @Override
    public Class<ContactOrganisme> getElementClass() {
        return ContactOrganisme.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.ORGANISME_DISPOSE_INTERVENANT.name();
    }
}
