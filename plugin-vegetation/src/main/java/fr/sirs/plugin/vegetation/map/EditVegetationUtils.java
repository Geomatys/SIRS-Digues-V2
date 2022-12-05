package fr.sirs.plugin.vegetation.map;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.ZoneVegetation;
import javafx.collections.FXCollections;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

final class EditVegetationUtils {

    static Label generateHeaderLabel(final String labelText) {
        final Label label = new Label(labelText);
        label.getStyleClass().add("label-header");
        return label;
    }

    static void initChoixCoteComboBox(final ComboBox<Preview> toInit, final String defaultId) {
        final Previews previewRepository = Injector.getSession().getPreviews();
        SIRS.initCombo(toInit, FXCollections.observableList(
                        previewRepository.getByClass(RefCote.class)),
                (defaultId == null || defaultId.trim().isEmpty()) ? null : defaultId);
    }

    /**
     * set to an inpur {@link ZoneVegetation} the {@link ZoneVegetation#typeCoteId} and the  {@link ZoneVegetation#contactEau}
     * from input comboBox and checkBox respectively
     */
    static void setEditedAttributes(final ZoneVegetation zone, final ComboBox<Preview> coteComboBox, final CheckBox checkCoteEau) {
        final Preview cote = coteComboBox.getValue();
        zone.setTypeCoteId(cote.getElementId());
        zone.setContactEau(checkCoteEau.isSelected());
    }

}
