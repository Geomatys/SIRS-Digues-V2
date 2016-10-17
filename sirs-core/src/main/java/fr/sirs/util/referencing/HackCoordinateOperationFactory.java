package fr.sirs.util.referencing;

import java.util.Collections;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.IdentifiedObjects;
import org.apache.sis.referencing.operation.CoordinateOperationContext;
import org.apache.sis.referencing.operation.DefaultCoordinateOperationFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.util.FactoryException;

/**
 * Hacked operation factory to force NTV2 grid usage when projecting points from
 * NTF-Paris to RGF93.
 *
 * @author Alexis Manin (Geomatys)
 */
public class HackCoordinateOperationFactory extends DefaultCoordinateOperationFactory {

    @Override
    public CoordinateOperation createOperation(CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS, CoordinateOperationContext context) throws OperationNotFoundException, FactoryException {
        if (sourceCRS instanceof ProjectedCRS) {
            Integer code = IdentifiedObjects.lookupEPSG(((ProjectedCRS)sourceCRS).getBaseCRS());
            if (code != null && code == 4807) {
                if (targetCRS instanceof ProjectedCRS) {
                    code = IdentifiedObjects.lookupEPSG(((ProjectedCRS)targetCRS).getBaseCRS());
                    if (code != null && code == 4171) {
                        CoordinateReferenceSystem step1CRS = CRS.forCode("EPSG:4275");
                        CoordinateReferenceSystem step2CRS = CRS.forCode("EPSG:4171");

                        final CoordinateOperation step1 = super.createOperation(sourceCRS, step1CRS, context);
                        final CoordinateOperation step2 = super.createOperation(step1CRS, step2CRS, context);
                        final CoordinateOperation step3 = super.createOperation(step2CRS, targetCRS, context);
                        
                        return super.createConcatenatedOperation(Collections.singletonMap("name", "NTF-Paris to RGF93"), step1, step2, step3);
                    }
                }
            }
        }
        return super.createOperation(sourceCRS, targetCRS, context);
    }
}
