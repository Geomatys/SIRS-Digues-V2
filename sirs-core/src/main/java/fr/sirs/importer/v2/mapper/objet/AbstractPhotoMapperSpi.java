package fr.sirs.importer.v2.mapper.objet;

import org.springframework.stereotype.Component;

import fr.sirs.core.model.AbstractPhoto;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class AbstractPhotoMapperSpi extends GenericMapperSpi<AbstractPhoto> {

    private final HashMap<String, String> bindings;

    public AbstractPhotoMapperSpi() throws IntrospectionException {
        super(AbstractPhoto.class);

        bindings = new HashMap<>(4);
        bindings.put(PhotoColumns.REF_PHOTO.name(), "libelle");
        bindings.put(PhotoColumns.NOM_FICHIER_PHOTO.name(), "chemin");
        bindings.put(PhotoColumns.ID_INTERV_PHOTOGRAPH.name(), "photographeId");
        bindings.put(PhotoColumns.DATE_PHOTO.name(), "date");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
