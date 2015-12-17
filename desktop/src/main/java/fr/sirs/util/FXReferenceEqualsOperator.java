
package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.Identifiable;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.ReferenceType;
import java.util.List;
import java.util.Optional;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.gui.javafx.filter.FXFilterOperator;
import org.opengis.feature.AttributeType;
import org.opengis.feature.FeatureAssociationRole;
import org.opengis.feature.PropertyType;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;

/**
 * A special filter case to allow equality test on SIRS links between objects.
 * In application, we expect that links between elements are represented by a
 * {@link FeatureAssociationRole}, whose inner feature type contains a 'class'
 * property. The class property is expected to be an attribute type whose value
 * class is the class of the element bound by its id.
 * @author Alexis Manin (Geomatys)
 */
public class FXReferenceEqualsOperator implements FXFilterOperator {

    public static final String CLASS_ATTRIBUTE = "class";

    @Override
    public boolean canHandle(PropertyType target) {
        if (target instanceof FeatureAssociationRole) {
            final FeatureAssociationRole role = (FeatureAssociationRole) target;
            try {
                return role.getValueType().getProperty(CLASS_ATTRIBUTE) != null;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public CharSequence getTitle() {
        return "est";
    }

    @Override
    public Optional<Node> createFilterEditor(PropertyType target) {
        final Class refClass = getReferenceClass(target);

        final ObservableList<Preview> choices;
        if (ReferenceType.class.isAssignableFrom(refClass)) {
            choices = SIRS.observableList(Injector.getSession().getRepositoryForClass(refClass).getAll());
        } else {
            choices = SIRS.observableList(
                Injector.getSession().getPreviews().getByClass(refClass)).sorted();
        }

        if (choices.isEmpty())
            return Optional.empty();

        final ComboBox cBox = new ComboBox();
        SIRS.initCombo(cBox, choices, target);
        return Optional.of(cBox);
    }

    @Override
    public boolean canExtractSettings(PropertyType propertyType, Node settingsContainer) {
        final Class refClass = getReferenceClass(propertyType);
        if (refClass == null)
            return false;

        final List choices;
        if (settingsContainer instanceof ComboBox) {
            choices = ((ComboBox)settingsContainer).getItems();
        } else if (settingsContainer instanceof ChoiceBox) {
            choices = ((ChoiceBox)settingsContainer).getItems();
        } else {
            choices = null;
        }

        if (choices != null && !choices.isEmpty()) {
            final Object firstChoice = choices.get(0);
            if (refClass.isInstance(firstChoice)) {
                return true;
            } else if (firstChoice instanceof Preview) {
                return refClass.getCanonicalName().equals(((Preview)firstChoice).getElementClass());
            }
        }

        return false;
    }

    @Override
    public Filter getFilterOver(Expression toApplyOn, Node filterEditor) {
        Object choice = null;
        if (filterEditor instanceof ComboBoxBase) {
            choice = ((ComboBoxBase)filterEditor).valueProperty().get();
        }

        final String choiceId;
        if (choice instanceof Preview) {
            choiceId = ((Preview)choice).getElementId();
        } else if (choice instanceof Identifiable) {
            choiceId = ((Identifiable)choice).getId();
        } else {
            throw new IllegalArgumentException("L'éditeur des paramètres du filtre est invalide.");
        }

        return GO2Utilities.FILTER_FACTORY.equals(toApplyOn,
                GO2Utilities.FILTER_FACTORY.literal(((Preview) choice).getElementId()));
    }

    private static Class getReferenceClass(PropertyType propertyType) {
        if (propertyType instanceof FeatureAssociationRole) {
            PropertyType property = ((FeatureAssociationRole)propertyType).getValueType().getProperty(CLASS_ATTRIBUTE);
            if (property instanceof AttributeType) {
                return ((AttributeType)property).getValueClass();
            }
        }

        throw new IllegalArgumentException("Le filtre courant ne peut gérer que des attributs associatifs !");
    }

}
