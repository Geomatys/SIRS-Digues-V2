package fr.sirs.importer.v2.mapper;

import fr.sirs.core.model.ContactOrganisme;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ContactOrganismeMapperSpi extends GenericMapperSpi<ContactOrganisme> {

    private final HashMap<String, String> bindings;
    public ContactOrganismeMapperSpi() throws IntrospectionException {
        super(ContactOrganisme.class);

        bindings = new HashMap<>(1);
        bindings.put("ID_INTERVENANT", "contactId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
