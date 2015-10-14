package fr.sirs.importer.v2.document;

import fr.sirs.core.model.Contact;
import fr.sirs.core.model.Convention;
import fr.sirs.importer.v2.JoinTableLinker;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ConventionContactLinker extends JoinTableLinker<Contact, Convention> {

    public ConventionContactLinker() {
        super("CONVENTION_SIGNATAIRES_PP", Contact.class, Convention.class, "ID_INTERV_SIGNATAIRE", "ID_CONVENTION");
    }
}
