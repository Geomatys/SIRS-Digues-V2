package org.apache.sis.referencing.operation;

import java.util.Collections;
import java.util.logging.Level;
import org.apache.sis.internal.util.Constants;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.IdentifiedObjects;
import static org.apache.sis.referencing.operation.DefaultCoordinateOperationFactory.USE_EPSG_FACTORY;
import org.apache.sis.util.Utilities;
import org.apache.sis.util.collection.Cache;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.DerivedCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.util.FactoryException;

import fr.sirs.core.SirsCore;

/**
 * Hacked operation factory to force NTV2 grid usage when projecting points from
 * NTF-Paris to RGF93.
 * Another hack has been introduced : create operation from derived CRS is very
 * time consuming, due to the fact that SIS performs a big search in EPSG db.
 * We short this behavior here.
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

        } else if (sourceCRS instanceof DerivedCRS) {
            final DerivedCRS derivedCRS = (DerivedCRS)sourceCRS;
            if (Utilities.equalsApproximatively(derivedCRS.getBaseCRS(), targetCRS)) {
                // We cannot use the same workaround as below, or it will cause
                // a recursive locking error.
                final AuthorityFactory registry = USE_EPSG_FACTORY ? CRS.getAuthorityFactory(Constants.EPSG) : null;
                try {
                    return new CoordinateOperationFinder((registry instanceof CoordinateOperationAuthorityFactory) ?
                            (CoordinateOperationAuthorityFactory) registry : null, this, context).inverse(derivedCRS.getConversionFromBase());
                } catch (NoninvertibleTransformException ex) {
                    SirsCore.LOGGER.log(Level.FINE, "Cannot apply hack on coordinate operation search", ex);
                }
            } else {
                derivedCRS.getConversionFromBase();
                final SingleCRS baseCRS = derivedCRS.getBaseCRS();
                final CoordinateOperation step1 = super.createOperation(sourceCRS, baseCRS, context);
                final CoordinateOperation step2 = super.createOperation(baseCRS, targetCRS, context);

                return createConcatenatedOperation(Collections.singletonMap("name", "derivedToOther"), step1, step2);
            }
        }

        return null;
    }
}
