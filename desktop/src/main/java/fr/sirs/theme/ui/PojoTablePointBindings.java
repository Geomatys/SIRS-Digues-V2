package fr.sirs.theme.ui;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.PointDZ;
import fr.sirs.core.model.PointXYZ;
import fr.sirs.core.model.PointZ;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.PrZPointImporter;
import fr.sirs.core.model.SystemeReperage;
import java.util.logging.Level;
import javafx.beans.binding.DoubleBinding;

/**
 *
 * Utilitary bindings for pojotable autocomputable colums (to import points).
 *
 * @author Samuel Andrés (Geomatys)
 */
public class PojoTablePointBindings {

    /**
     * An abstract binding to compute something aboute a point.
     *
     * @param <T>
     */
    public static abstract class PointBinding<T extends PointZ> extends DoubleBinding {

        protected final T point;

        public PointBinding(final T point){
            this.point = point;
        }
    }

    /**
     * A binding to compute the point distance from an origin point.
     *
     * Both points are XYZ points.
     *
     * Attepted return:
     * The distance is computed using the euclidian distance.
     *
     * Other possible return:
     * If the binding is unable to compute the distance, then it returns 0..
     */
    public static class DXYZBinding extends PointBinding<PointXYZ> {

        protected final PointXYZ origine;

        public DXYZBinding(final PointXYZ point, final PointXYZ origine){
            super(point);
            this.origine = origine;
            super.bind(point.xProperty(), point.yProperty(), origine.xProperty(), origine.yProperty());
        }

        @Override
        protected double computeValue() {
            // La valeur du point ne doit pas être nulle !
            if(point!=null && origine!=null){
                return Math.sqrt(Math.pow(point.getX() - origine.getX(), 2) + Math.pow(point.getY() - origine.getY(), 2));
            }
            else {
                final PointXYZ[] params = {origine, point};
                SIRS.LOGGER.log(Level.INFO, "Les points d'origine {0} et de calcul {1} ne doit pas \u00eatre nul", params);
                return 0.;
            }
        }
    }

    /**
     * A binding to compute the point "distance" (PR) on a "Positionable" entity.
     *
     * The point is an XYZ point.
     *
     * Attempted return:
     * The PR is computed using the default SR of the linear entity (tronçon)
     * the positionable is associated on. The positionable entity needs to be
     * associated on a linear entity (tronçon) having a geometry and, this last
     * one must have a default SR to compute the PR of the point.
     *
     * Other possible returns:
     * If the binding cannot compute the PR, it returns 0..
     */
    public static class PRXYZBinding extends PointBinding<PointXYZ> {

        protected final TronconUtils.PosInfo posInfo;

        public PRXYZBinding(final PointXYZ pointLeve, final Positionable positionable){
            super(pointLeve);
            posInfo = new TronconUtils.PosInfo(positionable);
            super.bind(pointLeve.xProperty(), pointLeve.yProperty());
        }

        @Override
        protected double computeValue() {
            // La valeur du point ne doit pas être nulle !
            if(point!=null){
                if(posInfo==null || posInfo.getTroncon()==null) {
                    SIRS.LOGGER.log(Level.INFO, "Impossible de récupérer le linéaire associé ({0}).", posInfo);
                    return 0.;
                } else if (posInfo.getTroncon().getSystemeRepDefautId()==null){
                    SIRS.LOGGER.log(Level.INFO, "Le linéaire associé n'a pas de système de repérage par défaut ({0}).", posInfo.getTroncon());
                    return 0.;
                } else if (posInfo.getTronconSegments(false)==null){
                    SIRS.LOGGER.log(Level.INFO, "Le linéaire de référence n'est pas disponible ({0}).", posInfo);
                    return 0.;
                } else{
                    try{
                    return (double) TronconUtils.computePR(
                            posInfo.getTronconSegments(false),
                            Injector.getSession().getRepositoryForClass(SystemeReperage.class).get(posInfo.getTroncon().getSystemeRepDefautId()),
                            new GeometryFactory().createPoint(new Coordinate(point.getX(),point.getY())),
                            Injector.getSession().getRepositoryForClass(BorneDigue.class));
                    } catch(Exception e){
                        SIRS.LOGGER.log(Level.WARNING, e.getMessage());
                        return 0.;
                    }
                }
            }
            else {
                SIRS.LOGGER.log(Level.INFO, "Le point {0} ne doit pas \u00eatre nul", point);
                return 0.;
            }
        }
    }


    /**
     * A binding to compute the point "distance" (PR) on a "Positionable" entity
     * which is a PrZPointImporter.
     *
     * The point is a PointDZ, which have a "distance" (PR).
     *
     * A PrZPointImporter is a kind of objects able to specify the SR in wich
     * the original "distance" (PR) of the point is given.
     *
     * Attempted return:
     * The PR is computed using the default SR of the linear entity (tronçon)
     * the positionable is associated on. The positionable entity needs to be
     * associated on a linear entity (tronçon) having a geometry and, this last
     * one must have a default SR to compute the PR of the point.
     *
     * Other possible returns:
     * If the point is null (must never happen), it returns 0..
     * Otherwise, if the binding cannot compute the PR in the new SR, it
     * returns the PR in the old SR.
     *
     * @param <P>
     */
    public static class PRZBinding<P extends Positionable & PrZPointImporter> extends PointBinding<PointDZ> {

        protected final TronconUtils.PosInfo posInfo;
        protected final P pointImporter;

        public PRZBinding(final PointDZ point, final P pointImporter){
            super(point);
            if(pointImporter instanceof Positionable){
                this.pointImporter = pointImporter;
                posInfo = new TronconUtils.PosInfo(pointImporter);
                super.bind(point.dProperty(), pointImporter.systemeRepDzIdProperty());
            }
            else throw new UnsupportedOperationException(pointImporter.toString()+"("+pointImporter.getClass().getName()+") must be "+Positionable.class.getName());
        }

        @Override
        protected double computeValue() {
            // La valeur du point ne doit pas être nulle !
            if(point!=null){
                if(pointImporter==null || pointImporter.getSystemeRepDzId()==null) {
                    SIRS.LOGGER.log(Level.INFO, "Le système de repérage de saisie des points n'est pas disponible ({0}).", pointImporter);
                    return point.getD();
                } else if(posInfo==null || posInfo.getTroncon()==null) {
                    SIRS.LOGGER.log(Level.INFO, "Impossible de récupérer le linéaire associé ({0}).", posInfo);
                    return point.getD();
                } else if (posInfo.getTroncon().getSystemeRepDefautId()==null){
                    SIRS.LOGGER.log(Level.INFO, "Le linéaire associé n'a pas de système de repérage par défaut ({0}).", posInfo.getTroncon());
                    return point.getD();
                } else if (posInfo.getTronconSegments(false)==null){
                    SIRS.LOGGER.log(Level.INFO, "Le linéaire de référence n'est pas disponible ({0}).", posInfo);
                    return point.getD();
                }
                /*
                Si on est déjà dans le même système de repérage, on se
                contente de renvoyer la valeur telle quelle sans la
                recalculer (ni provoquer au passage des erreurs dues aux
                approximations).
                */
                else if(pointImporter.getSystemeRepDzId().equals(posInfo.getTroncon().getSystemeRepDefautId())){
                    return point.getD();
                } else {
                    try{
                        return TronconUtils.switchSRForPR(posInfo.getTronconSegments(false),
                            point.getD(),
                            Injector.getSession().getRepositoryForClass(SystemeReperage.class).get(pointImporter.getSystemeRepDzId()),
                            Injector.getSession().getRepositoryForClass(SystemeReperage.class).get(posInfo.getTroncon().getSystemeRepDefautId()),
                            Injector.getSession().getRepositoryForClass(BorneDigue.class));
                    } catch(Exception e){
                        SIRS.LOGGER.log(Level.WARNING, e.getMessage());
                        return point.getD();
                    }
                }
            }
            else {
                SIRS.LOGGER.log(Level.INFO, "Le point {0} ne doit pas \u00eatre nul", point);
                return 0.;
            }
        }
    }

}
