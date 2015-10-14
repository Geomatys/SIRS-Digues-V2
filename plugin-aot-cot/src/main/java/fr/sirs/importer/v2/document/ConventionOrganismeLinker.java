package fr.sirs.importer.v2.document;

import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.Convention;
import fr.sirs.importer.v2.JoinTableLinker;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ConventionOrganismeLinker extends JoinTableLinker<Organisme, Convention> {

    public ConventionOrganismeLinker() {
        super("CONVENTION_SIGNATAIRES_PM", Organisme.class, Convention.class, "ID_ORG_SIGNATAIRE", "ID_CONVENTION");
    }
}
