
package fr.sirs;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import static fr.sirs.CorePlugin.POINTS_TO_LINE;
import java.util.logging.Level;
import org.geotoolkit.filter.function.AbstractFunction;
import org.opengis.filter.expression.Expression;

/**
 * Try to convert two Points feature's parameters in its {@linkplain Point center}.
 *
 *
 * @author Matthieu Bastianelli (Geomatys)
 * @throw {@link IllegalArgumentException} if one of the parameter is missing.
 * @throw {@link RuntimeException} if fail to convert the 2nd point in the 1st point CRS.
 */
public final class PointsToCenter extends AbstractFunction {

    private static final String NAME = "PointsToCenter";

    public PointsToCenter(final Expression expr1, final Expression expr2) {
        super(NAME, new Expression[] {expr1,expr2}, null);
    }

    @Override
    public Object evaluate(final Object feature) {

        final Object ptsToLine = POINTS_TO_LINE.evaluate(feature);
        if (ptsToLine instanceof Point) {
            return ptsToLine;
        } else if (ptsToLine instanceof LineString) {

            final LineString lineString = (LineString) POINTS_TO_LINE.evaluate(feature);
            if (lineString == null) {
                return lineString;
            }
            final Point center = lineString.getCentroid();
            center.setSRID(lineString.getSRID());
            center.setUserData(lineString.getUserData());
            return center;

        } else {
            SIRS.LOGGER.log(Level.WARNING, "Fail to compute center for the assessed feature. Return Null.");
            return null;
        }
    }



}

