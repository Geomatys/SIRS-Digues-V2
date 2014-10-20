/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.symadrem.sirs.core.model.Organisme;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class OrganismeImporter extends GenericImporter {

    private Map<Integer, Organisme> organismes = null;

    public OrganismeImporter(Database accessDatabase) {
        super(accessDatabase);
    }

    /**
     * *************************************************************************
     * ORGANISME.
     * ----------------------------------------------------------------------------
     * x ID_ORGANISME RAISON_SOCIALE // Nom STATUT_JURIDIQUE ADRESSE_L1_ORG
     * ADRESSE_L2_ORG ADRESSE_L3_ORG ADRESSE_CODE_POSTAL_ORG
     * ADRESSE_NOM_COMMUNE_ORG TEL_ORG MAIL_ORG FAX_ORG DATE_DEBUT DATE_FIN x
     * DATE_DERNIERE_MAJ // Pas de date de mise à jour dans le modèle.
     * ----------------------------------------------------------------------------
     * Les adresses 1, 2 et 3 sont concaténées. La raison sociale devient le
     * nom. La commune devient la localité. On n'a pas de pays dans la base.
     *
     * on n'a pas de date de mise à jour : est-ce normal ?
     */
    public static enum OrganismeColumns {

        ID("ID_ORGANISME"), RAISON_SOCIALE("RAISON_SOCIALE"), STATUT_JURIDIQUE("STATUT_JURIDIQUE"),
        ADRESSE1("ADRESSE_L1_ORG"), ADRESSE2("ADRESSE_L2_ORG"), ADRESSE3("ADRESSE_L3_ORG"),
        CODE_POSTAL("ADRESSE_CODE_POSTAL_ORG"), COMMUNE("ADRESSE_NOM_COMMUNE_ORG"),
        TEL("TEL_ORG"), COURRIEL("MAIL_ORG"), FAX("FAX_ORG"),
        DEBUT("DATE_DEBUT"), FIN("DATE_FIN"), MAJ("DATE_DERNIERE_MAJ");
        private final String column;

        private OrganismeColumns(final String column) {
            this.column = column;
        }

        @Override
        public String toString() {
            return this.column;
        }
    };

    public Map<Integer, Organisme> getOrganismes() throws IOException {

        if (organismes == null) {
            organismes = new HashMap<>();
            final Iterator<Row> it = this.accessDatabase.getTable("ORGANISME").iterator();

            while (it.hasNext()) {
                final Row row = it.next();
                final Organisme organisme = new Organisme();

                organisme.setNom(row.getString(OrganismeColumns.RAISON_SOCIALE.toString()));
                organisme.setStatut_juridique(row.getString(OrganismeColumns.STATUT_JURIDIQUE.toString()));
                organisme.setAdresse(row.getString(OrganismeColumns.ADRESSE1.toString())
                        + row.getString(OrganismeColumns.ADRESSE2.toString())
                        + row.getString(OrganismeColumns.ADRESSE3.toString()));
                organisme.setCode_postal(String.valueOf(row.getInt(OrganismeColumns.CODE_POSTAL.toString())));
                organisme.setLocalite(row.getString(OrganismeColumns.COMMUNE.toString()));
                organisme.setTelephone(row.getString(OrganismeColumns.TEL.toString()));
                organisme.setEmail(row.getString(OrganismeColumns.COURRIEL.toString()));
                organisme.setFax(row.getString(OrganismeColumns.FAX.toString()));
                if (row.getDate(OrganismeColumns.DEBUT.toString()) != null) {
                    organisme.setDate_debut(LocalDateTime.parse(row.getDate(OrganismeColumns.DEBUT.toString()).toString(), dateTimeFormatter));
                }
                if (row.getDate(OrganismeColumns.FIN.toString()) != null) {
                    organisme.setDate_fin(LocalDateTime.parse(row.getDate(OrganismeColumns.FIN.toString()).toString(), dateTimeFormatter));
                }

                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                organismes.put(row.getInt(OrganismeColumns.ID.toString()), organisme);
            }
        }
        return organismes;
    }
}
