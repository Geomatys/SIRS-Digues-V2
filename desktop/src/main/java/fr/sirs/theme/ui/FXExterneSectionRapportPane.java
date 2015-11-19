package fr.sirs.theme.ui;

import fr.sirs.SIRS;
import fr.sirs.core.model.report.ExterneSectionRapport;
import fr.sirs.util.FXFileTextField;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class FXExterneSectionRapportPane extends AbstractFXElementPane<ExterneSectionRapport> {

    @FXML private TextField uiTitle;
    @FXML private BorderPane uiPathContainer;
    private final FXFileTextField uiPathEditor;

    @Override
    public void preSave() throws Exception {
        elementProperty.get().setChemin(uiPathEditor.getText());
    }

    public FXExterneSectionRapportPane() {
        super();
        SIRS.loadFXML(this);

        uiPathEditor = new FXFileTextField();
        uiPathContainer.setCenter(uiPathEditor);
        elementProperty.addListener(this::elementChanged);
    }

    public FXExterneSectionRapportPane(final ExterneSectionRapport rapport) {
        this();
        setElement(rapport);
    }


    /**
     * Called when element edited change. We must update all UI to manage the new one.
     * @param obs
     * @param oldValue
     * @param newValue
     */
    private void elementChanged(ObservableValue<? extends ExterneSectionRapport> obs, ExterneSectionRapport oldValue, ExterneSectionRapport newValue) {
        if (oldValue != null) {
            uiTitle.textProperty().unbindBidirectional(oldValue.libelleProperty());
        }

        if (newValue == null) {
            uiPathEditor.setText(null);
        } else {
            uiTitle.textProperty().bindBidirectional(newValue.libelleProperty());
            uiPathEditor.setText(newValue.getChemin());
        }
    }
}
