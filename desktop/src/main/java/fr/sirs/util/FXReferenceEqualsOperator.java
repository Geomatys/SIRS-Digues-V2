
package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Preview;
import java.util.List;
import java.util.Optional;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.gui.javafx.filter.FXFilterOperator;
import org.geotoolkit.gui.javafx.util.ComboBoxCompletion;
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
    private static final SirsStringConverter CHOICE_CONVERTER = new SirsStringConverter();

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
        ObservableList<Preview> choices = SirsCore.observableList(
                Injector.getSession().getPreviews().getByClass(refClass));
        if (choices.isEmpty())
            return Optional.empty();

        final ComboBox cBox = new ComboBox(choices);
        cBox.setConverter(CHOICE_CONVERTER);
        cBox.setEditable(true);
        new ComboBoxCompletion(cBox);
        return Optional.of(cBox);
    }

    @Override
    public boolean canExtractSettings(PropertyType propertyType, Node settingsContainer) {
        final List choices;
        final Class refClass = getReferenceClass(propertyType);
        if (settingsContainer instanceof ComboBox) {
            choices = ((ComboBox)settingsContainer).getItems();
        } else if (settingsContainer instanceof ChoiceBox) {
            choices = ((ChoiceBox)settingsContainer).getItems();
        } else {
            choices = null;
        }

        if (choices != null && !choices.isEmpty() && refClass.isInstance(choices.get(0))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Filter getFilterOver(Expression toApplyOn, Node filterEditor) {
        Object choice = null;
        if (filterEditor instanceof ComboBoxBase) {
            choice = ((ComboBoxBase)filterEditor).valueProperty().get();
        }

        if (choice instanceof Preview) {
            return GO2Utilities.FILTER_FACTORY.equals(toApplyOn,
                    GO2Utilities.FILTER_FACTORY.literal(((Preview)choice).getElementId()));
        } else {
            throw new IllegalArgumentException("L'éditeur des paramètres du filtre est invalide.");
        }
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
