package fr.sirs.importer.v2.contact;

import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.*;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.ContactOrganisme;
import fr.sirs.core.model.Organisme;
import fr.sirs.importer.v2.SimpleUpdater;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * @author Alexis Manin (Geometys)
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class OrganismeDisposeIntervenantImporter extends SimpleUpdater<ContactOrganisme, Organisme> {

    private static final String TABLE_NAME = "ORGANISME_DISPOSE_INTERVENANT";

    private AbstractImporter<Contact> contactImporter;

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
        ID_INTERVENANT,
        DATE_DEBUT_INTERV_ORG,
        DATE_FIN_INTERV_ORG,
        DATE_DERNIERE_MAJ,
    };

    @Override
    protected Class<ContactOrganisme> getElementClass() {
        return ContactOrganisme.class;
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
        return TABLE_NAME;
    }

    @Override
    protected void postCompute() {
        super.postCompute();
        contactImporter = null;
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();
        contactImporter = context.importers.get(Contact.class);
    }


    @Override
    public ContactOrganisme importRow(Row row, ContactOrganisme contactOrganisme) throws IOException, AccessDbImporterException {
        contactOrganisme.setContactId(contactImporter.getImportedId(row.getInt(Columns.ID_INTERVENANT.toString())));

        final Date dateDebut = row.getDate(Columns.DATE_DEBUT_INTERV_ORG.toString());
        if (dateDebut != null) {
            contactOrganisme.setDateDebutIntervenant(context.convertData(dateDebut, LocalDate.class));
        }

        final Date dateFin = row.getDate(Columns.DATE_FIN_INTERV_ORG.toString());
        if (dateFin != null) {
            contactOrganisme.setDateFinIntervenant(context.convertData(dateFin, LocalDate.class));
        }

        final Date dateMaj = row.getDate(Columns.DATE_DERNIERE_MAJ.toString());
        if (dateMaj != null) {
            contactOrganisme.setDateMaj(context.convertData(dateMaj, LocalDate.class));
        }

        return contactOrganisme;
    }
}
