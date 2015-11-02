package fr.sirs.ui.report;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.report.ModeleRapport;
import fr.sirs.theme.ui.AbstractFXElementPane;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class FXModeleRapportsPane extends BorderPane {

    @FXML
    private ListView<Preview> uiReportList;

    private AbstractFXElementPane<ModeleRapport> editor;

    private final AbstractSIRSRepository<ModeleRapport> repo;

    public FXModeleRapportsPane() {
        super();
        SIRS.loadFXML(this);

        final Session session = Injector.getSession();
        repo = session.getRepositoryForClass(ModeleRapport.class);
        if (repo == null) {
            throw new IllegalStateException("No repository available for type "+ModeleRapport.class.getCanonicalName());
        }

        uiReportList.getSelectionModel().selectedItemProperty().addListener(this::selectionChanged);
        uiReportList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        uiReportList.setItems(FXCollections.observableList(session.getPreviews().getByClass(ModeleRapport.class)));
        uiReportList.setCellFactory((ListView<Preview> param) -> {
            return new ListCell<Preview>() {

                @Override
                protected void updateItem(Preview item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getLibelle());
                    }
                }
            };
        });
    }

    @FXML
    void addReport(ActionEvent event) {
        ModeleRapport newModele = repo.create();
        repo.add(newModele);

        final Preview p = new Preview();
        p.setElementClass(ModeleRapport.class.getCanonicalName());
        p.setDocClass(p.getElementClass());
        p.setElementId(newModele.getId());
        p.setDocId(newModele.getId());

        uiReportList.getItems().add(p);
        uiReportList.getSelectionModel().select(p);
    }

    @FXML
    void deleteReport(ActionEvent event) {
        ObservableList<Preview> selectedItems = uiReportList.getSelectionModel().getSelectedItems();
        if (selectedItems == null || selectedItems.isEmpty()) {
            return;
        }

        final Alert alert = new Alert(
                Alert.AlertType.WARNING,
                "Vous allez supprimer définitivement les modèles séléctionnés. Êtes-vous sûr ?",
                ButtonType.NO, ButtonType.YES
        );

        alert.setResizable(true);
        if (ButtonType.YES.equals(alert.showAndWait().orElse(ButtonType.NO))) {
            final String[] modeles = new String[selectedItems.size()];
            for (int i = 0; i < modeles.length; i++) {
                modeles[i] = selectedItems.get(i).getElementId();
            }
            repo.executeBulkDelete(repo.get(modeles));
        }
    }

    private void selectionChanged(final ObservableValue<? extends Preview> obs, Preview oldValue, Preview newValue) {
        if (oldValue != null) {
            oldValue.libelleProperty().unbind();
        }

        if (newValue != null) {
            final ModeleRapport rapport = repo.get(newValue.getElementId());
            newValue.libelleProperty().bind(rapport.libelleProperty());

            if (editor == null) {
                editor = SIRS.generateEditionPane(rapport);
                setRight(editor);
            } else {
                editor.setElement(rapport);
            }
        }
    }
}
