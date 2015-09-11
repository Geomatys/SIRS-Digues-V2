package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.core.component.SystemeEndiguementRepository;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.SystemeEndiguement;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * Surcharge du panneau autogénéré afin de gérer la propriété "classement" d'un SE.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class FXObligationReglementairePane extends FXObligationReglementairePaneStub {
    @FXML private TextField ui_class;

    private FXObligationReglementairePane() {
        super();

        ui_class.setEditable(false);
        ui_class.setDisable(true);
    }

    public FXObligationReglementairePane(final ObligationReglementaire obligationReglementaire){
        this();
        this.elementProperty().set(obligationReglementaire);
    }

    @Override
    protected void initFields(ObservableValue<? extends ObligationReglementaire> observableElement, ObligationReglementaire oldElement, ObligationReglementaire newElement) {
        super.initFields(observableElement, oldElement, newElement);

        final SystemeEndiguementRepository seRepo = Injector.getBean(SystemeEndiguementRepository.class);
        ui_class.textProperty().unbind();
        if (ui_systemeEndiguementId.getSelectionModel().getSelectedItem() instanceof Preview) {
            final SystemeEndiguement se = seRepo.get(((Preview)ui_systemeEndiguementId.getSelectionModel().getSelectedItem()).getElementId());
            ui_class.setText(se.getClassement());
        }

        ui_systemeEndiguementId.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            final SystemeEndiguement se = seRepo.get(((Preview)newValue).getElementId());
            ui_class.textProperty().bind(se.classementProperty());
        });
    }
}
