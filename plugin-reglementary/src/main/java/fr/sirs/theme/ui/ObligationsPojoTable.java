package fr.sirs.theme.ui;

import fr.sirs.core.component.ObligationReglementaireRepository;

/**
 * Table présentant les obligations réglementaires.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class ObligationsPojoTable extends PojoTable {
    public ObligationsPojoTable(final ObligationReglementaireRepository obligationRepository) {
        super(obligationRepository, "Liste des obligations réglementaires");
    }
}
