package org.apache.sis.referencing.operation;

import java.util.Collections;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.IdentifiedObjects;
import org.apache.sis.referencing.operation.CoordinateOperationContext;
import org.apache.sis.referencing.operation.DefaultCoordinateOperationFactory;
import org.apache.sis.util.collection.Cache;
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
        // We don't know how to define a cache policy using operation context. So, for this partiular case, we do not use cache.
        CoordinateOperation op;
        if (context != null) {
            op = createOperationUncached(sourceCRS, targetCRS, context);

        } else {
            final CRSPair cacheKey = new CRSPair(sourceCRS, targetCRS);
            op = cache.peek(cacheKey);
            if (op == null) {
                final Cache.Handler<CoordinateOperation> lock = cache.lock(cacheKey);
                try {
                    op = lock.peek();
                    if (op != null)
                        return op;
                    op = createOperationUncached(sourceCRS, targetCRS, context);
                } finally {
                    lock.putAndUnlock(op);
                }
            }
        }

        if (op != null)
            return op;

        return super.createOperation(sourceCRS, targetCRS, context);
    }

    /**
     * Try to find a proper operation which apply the hack this class is designed
     * for. If we do not identify it as a proper candidate, we send back a null
     * value.
     * @param sourceCRS
     * @param targetCRS
     * @param context
     * @return
     * @throws OperationNotFoundException
     * @throws FactoryException
     */
    public CoordinateOperation createOperationUncached(CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS, CoordinateOperationContext context) throws OperationNotFoundException, FactoryException {
        // We perform less time consuming checks immediately : CRS subtype check
        if (sourceCRS instanceof ProjectedCRS && targetCRS instanceof ProjectedCRS) {
            Integer code = IdentifiedObjects.lookupEPSG(((ProjectedCRS) sourceCRS).getBaseCRS());
            if (code != null && code == 4807) {
                code = IdentifiedObjects.lookupEPSG(((ProjectedCRS) targetCRS).getBaseCRS());
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

        return null;
    }
}
