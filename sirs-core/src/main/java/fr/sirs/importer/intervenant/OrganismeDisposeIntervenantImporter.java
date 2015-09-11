package fr.sirs.importer.intervenant;

import fr.sirs.importer.*;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.ContactOrganisme;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.Organisme;
import fr.sirs.importer.v2.DocumentUpdater;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class OrganismeDisposeIntervenantImporter extends DocumentUpdater<Organisme> {

    private static final String TABLE_NAME = "ORGANISME_DISPOSE_INTERVENANT";

    private GenericImporter<Contact> contactImporter;

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_ORGANISME.name();
    }

    private enum Columns {
        ID_ORGANISME,
        ID_INTERVENANT,
        DATE_DEBUT_INTERV_ORG,
        DATE_FIN_INTERV_ORG,
        DATE_DERNIERE_MAJ,
    };

    @Override
    protected Class<Organisme> getDocumentClass() {
        return Organisme.class;
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
    protected void postCompute() throws AccessDbImporterException {
        super.postCompute();
        contactImporter = null;
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();
        contactImporter = context.importers.get(Contact.class);
    }


    @Override
    public Organisme importRow(Row row, Organisme output) throws IOException, AccessDbImporterException {
        final ContactOrganisme contactOrganisme = createAnonymValidElement(ContactOrganisme.class);

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
            contactOrganisme.setDateMaj(DbImporter.parseLocalDate(dateMaj, dateTimeFormatter));
        }

        // Table de jointure, donc pas d'ID propre. On choisit arbitrairement l'ID de l'intervenant comme pseudo-id.
        contactOrganisme.setDesignation(String.valueOf(row.getInt(Columns.ID_INTERVENANT.toString())));
        output.getContactOrganisme().add(contactOrganisme);

        return output;
    }
}
