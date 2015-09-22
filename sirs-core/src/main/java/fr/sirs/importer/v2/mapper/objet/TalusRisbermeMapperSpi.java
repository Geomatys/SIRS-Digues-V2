package fr.sirs.importer.v2.mapper.objet;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.TalusRisberme;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class TalusRisbermeMapperSpi extends GenericMapperSpi<TalusRisberme> {

    private final HashMap<String, String> bindings;

    public TalusRisbermeMapperSpi() throws IntrospectionException {
        super(TalusRisberme.class);

        bindings = new HashMap<>(12);
        bindings.put(StructureColumns.ID_SOURCE.name(), "sourceId");
        bindings.put(StructureColumns.ID_TYPE_COTE.name(), "coteId");

        bindings.put(StructureColumns.ID_TYPE_MATERIAU_HAUT.name(), "materiauHautId");
        bindings.put(StructureColumns.ID_TYPE_NATURE_HAUT.name(), "natureHautId");
        bindings.put(StructureColumns.LONG_RAMP_HAUT.name(), "longueurRampantHaut");

        bindings.put(StructureColumns.ID_TYPE_MATERIAU_BAS.name(), "materiauBasId");
        bindings.put(StructureColumns.ID_TYPE_NATURE_BAS.name(), "natureBasId");
        bindings.put(StructureColumns.LONG_RAMP_BAS.name(), "longueurRampantBas");

        bindings.put(StructureColumns.ID_TYPE_FONCTION.name(), "fonctionBasId");
        bindings.put(StructureColumns.ID_TYPE_POSITION.name(), "positionId");
        bindings.put(StructureColumns.EPAISSEUR.name(), "epaisseur");
        bindings.put(StructureColumns.N_COUCHE.name(), "numCouche");
        bindings.put(StructureColumns.PENTE_INTERIEURE.name(), "penteInterieure");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

    @Override
    protected Collection<BiConsumer<Row, TalusRisberme>> getExtraBindings() {
        return Collections.singleton((input, output)-> output.setFonctionHautId(output.getFonctionBasId()));
    }
}
