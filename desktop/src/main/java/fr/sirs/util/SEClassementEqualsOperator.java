package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.SystemeEndiguement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.DocumentNotFoundException;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.gui.javafx.filter.FXFilterOperator;
import org.opengis.feature.FeatureAssociationRole;
import org.opengis.feature.PropertyType;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

/**
 * A custom filter which works on objects bound to a {@link SystemeEndiguement}
 * object. It seeks for the SE "classement" attribute, and will compare its value
 * to the one specified as parameter.
 *
 * @author Alexis Manin (Geomatys)
 */
public class SEClassementEqualsOperator implements FXFilterOperator {

    public static final String CLASSEMENT_ATTRIBUTE = "classement";

    @Override
    public boolean canHandle(PropertyType target) {
        if (target instanceof FeatureAssociationRole) {
            final FeatureAssociationRole role = (FeatureAssociationRole) target;
            try {
                return role.getValueType().getProperty(CLASSEMENT_ATTRIBUTE) != null;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public CharSequence getTitle() {
        return "=";
    }

    @Override
    public Optional<Node> createFilterEditor(PropertyType target) {
        return Optional.of(new TextField());
    }

    @Override
    public boolean canExtractSettings(PropertyType propertyType, Node settingsContainer) {
        return canHandle(propertyType) && (settingsContainer instanceof TextInputControl);
    }

    @Override
    public Filter getFilterOver(Expression toApplyOn, Node filterEditor) {
        ArgumentChecks.ensureNonNull("Expression to evaluate", toApplyOn);
        if (filterEditor instanceof TextInputControl) {
            final String filterValue = ((TextInputControl)filterEditor).getText();
            return GO2Utilities.FILTER_FACTORY.equals(new GetClassement(toApplyOn), GO2Utilities.FILTER_FACTORY.literal(filterValue));
        } else {
            throw new IllegalArgumentException("Unknown editor !");
        }
    }

    private static class GetClassement implements Function {

        private final Expression source;

        public GetClassement(final Expression toEvaluate) {
            source = toEvaluate;
        }

        @Override
        public String getName() {
            return "getClassement";
        }

        @Override
        public List<Expression> getParameters() {
            return Collections.singletonList(source);
        }

        @Override
        public Literal getFallbackValue() {
            return GO2Utilities.FILTER_FACTORY.literal("");
        }

        @Override
        public Object evaluate(Object o) {
            Object evaluate = GO2Utilities.evaluate(source, o, null, null);
            if (evaluate instanceof String) {
                AbstractSIRSRepository<SystemeEndiguement> repo = Injector.getSession().getRepositoryForClass(SystemeEndiguement.class);
                try {
                    return repo.get((String)evaluate).getClassement();
                } catch (DocumentNotFoundException e) {
                    SIRS.LOGGER.log(Level.FINE, "SystemeEndiguement not found.", e);
                    return null;
                }
            } else {
                return null;
            }
        }

        @Override
        public <T> T evaluate(Object o, Class<T> type) {
            if (String.class.equals(type)) {
                return (T) evaluate(o);
            } else {
                return null;
            }
        }

        @Override
        public Object accept(ExpressionVisitor ev, Object o) {
            return ev.visit(this, o);
        }

    }
}
