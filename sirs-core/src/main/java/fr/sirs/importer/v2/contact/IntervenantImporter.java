package fr.sirs.importer.v2.contact;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Contact;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.INTERVENANT;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.v2.AbstractImporter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class IntervenantImporter extends AbstractImporter<Contact> {

    @Override
    protected Class<Contact> getElementClass() {
        return Contact.class;
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_INTERVENANT.name();
    }

    private enum Columns {
        ID_INTERVENANT,
        NOM_INTERVENANT,
        PRENOM_INTERVENANT,
        ADRESSE_PERSO_INTERV,
        ADRESSE_L1_PERSO_INTERV,
        ADRESSE_L2_PERSO_INTERV,
        ADRESSE_L3_PERSO_INTERV,
        ADRESSE_CODE_POSTAL_PERSO_INTERV,
        ADRESSE_NOM_COMMUNE_PERSO_INTERV,
        TEL_PERSO_INTERV,
        FAX_PERSO_INTERV,
        MAIL_INTERV,
        SERVICE_INTERV,
        FONCTION_INTERV,
//        DATE_DEBUT,
//        DATE_FIN,
        DATE_DERNIERE_MAJ
    };

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
        return INTERVENANT.toString();
    }

    @Override
    public Contact importRow(Row row, Contact intervenant) throws IOException, AccessDbImporterException {
        intervenant.setNom(row.getString(Columns.NOM_INTERVENANT.toString()));

        intervenant.setPrenom(row.getString(Columns.PRENOM_INTERVENANT.toString()));

        intervenant.setAdresse(cleanNullString(row.getString(Columns.ADRESSE_PERSO_INTERV.toString()))
                + cleanNullString(row.getString(Columns.ADRESSE_L1_PERSO_INTERV.toString()))
                + cleanNullString(row.getString(Columns.ADRESSE_L2_PERSO_INTERV.toString()))
                + cleanNullString(row.getString(Columns.ADRESSE_L3_PERSO_INTERV.toString())));

        intervenant.setCodePostal(cleanNullString(String.valueOf(row.getInt(Columns.ADRESSE_CODE_POSTAL_PERSO_INTERV.toString()))));

        intervenant.setCommune(row.getString(Columns.ADRESSE_NOM_COMMUNE_PERSO_INTERV.toString()));

        intervenant.setTelephone(row.getString(Columns.TEL_PERSO_INTERV.toString()));

        intervenant.setEmail(row.getString(Columns.MAIL_INTERV.toString()));

        intervenant.setFax(row.getString(Columns.FAX_PERSO_INTERV.toString()));

        intervenant.setService(row.getString(Columns.SERVICE_INTERV.toString()));

        intervenant.setFonction(row.getString(Columns.FONCTION_INTERV.toString()));
        final Date dateMaj = row.getDate(Columns.DATE_DERNIERE_MAJ.toString());

        if (dateMaj != null) {
            intervenant.setDateMaj(context.convertData(dateMaj, LocalDate.class));
        }

        return intervenant;
    }
}
