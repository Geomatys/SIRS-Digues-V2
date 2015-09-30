package fr.sirs.importer.v2.linear;

import fr.sirs.core.TronconUtils;
import fr.sirs.core.model.Positionable;
import fr.sirs.importer.v2.ElementModifier;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class GeometryComputer implements ElementModifier<Positionable> {

    @Override
    public Class<Positionable> getDocumentClass() {
        return Positionable.class;
    }

    @Override
    public void modify(Positionable outputData) {
        new TronconUtils.PosInfo(outputData).getGeometry();
    }

}
