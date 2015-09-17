package fr.sirs.importer.v2.contact;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Organisme;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.ORGANISME;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.v2.AbstractImporter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class OrganismeImporter extends AbstractImporter<Organisme> {

    private enum Columns {
        ID_ORGANISME,
        RAISON_SOCIALE,
        STATUT_JURIDIQUE,
        ADRESSE_L1_ORG,
        ADRESSE_L2_ORG,
        ADRESSE_L3_ORG,
        ADRESSE_CODE_POSTAL_ORG,
        ADRESSE_NOM_COMMUNE_ORG,
        TEL_ORG,
        MAIL_ORG,
        FAX_ORG,
        DATE_DEBUT,
        DATE_FIN,
        DATE_DERNIERE_MAJ
    };

    @Override
    protected Class<Organisme> getDocumentClass() {
        return Organisme.class;
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_ORGANISME.name();
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
        return ORGANISME.toString();
    }

    @Override
    public Organisme importRow(Row row, Organisme organisme) throws IOException, AccessDbImporterException {
        organisme.setNom(row.getString(Columns.RAISON_SOCIALE.toString()));
        organisme.setStatutJuridique(row.getString(Columns.STATUT_JURIDIQUE.toString()));
        organisme.setAdresse(cleanNullString(row.getString(Columns.ADRESSE_L1_ORG.toString()))
                + cleanNullString(row.getString(Columns.ADRESSE_L2_ORG.toString()))
                + cleanNullString(row.getString(Columns.ADRESSE_L3_ORG.toString())));
        organisme.setCodePostal(cleanNullString(String.valueOf(row.getInt(Columns.ADRESSE_CODE_POSTAL_ORG.toString()))));
        organisme.setCommune(row.getString(Columns.ADRESSE_NOM_COMMUNE_ORG.toString()));
        organisme.setTelephone(row.getString(Columns.TEL_ORG.toString()));
        organisme.setEmail(row.getString(Columns.MAIL_ORG.toString()));
        organisme.setFax(row.getString(Columns.FAX_ORG.toString()));

        final Date dateDebut = row.getDate(Columns.DATE_DEBUT.toString());
        if (dateDebut != null) {
            organisme.setDate_debut(context.convertData(dateDebut, LocalDate.class));
        }

        final Date dateFin = row.getDate(Columns.DATE_FIN.toString());
        if (dateFin != null) {
            organisme.setDate_fin(context.convertData(dateFin, LocalDate.class));
        }

        final Date dateMaj = row.getDate(Columns.DATE_DERNIERE_MAJ.toString());
        if (dateMaj != null) {
            organisme.setDateMaj(context.convertData(dateMaj, LocalDate.class));
        }

        return organisme;
    }
}
