
package fr.sirs.plugin.berge.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.BergeRepository;
import fr.sirs.core.component.DocumentChangeEmiter;
import fr.sirs.core.component.DocumentListener;
import fr.sirs.core.model.Berge;
import fr.sirs.core.model.Element;
import fr.sirs.ui.calendar.CalendarView;
import fr.sirs.util.SimpleFXEditMode;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SuiviBergePane extends BorderPane {
    
    public SuiviBergePane() {
        Injector.injectDependencies(this);

        // Gestion du bouton consultation / édition pour la pojo table
        final Separator separator = new Separator();
        separator.setVisible(false);
        final SimpleFXEditMode editMode = new SimpleFXEditMode();
        final HBox topPane = new HBox(separator, editMode);
        HBox.setHgrow(separator, Priority.ALWAYS);
        final BergeRepository repo = Injector.getBean(BergeRepository.class);
        
        // Liste de toutes les obligations
        final ObservableList<Berge> all = FXCollections.observableList(repo.getAll());
        final BergeDocumentListener oblListener = new BergeDocumentListener(all);
        
        // Ajoute un listener sur tous les ajouts/suppression de berge pour mettre à jour la liste et donc la table.
        Injector.getBean(DocumentChangeEmiter.class).addListener(oblListener);
        final BergesPojoTable bergesPojoTable = new BergesPojoTable(Berge.class);
        bergesPojoTable.setTableItems(() -> (ObservableList)all);
        bergesPojoTable.editableProperty().bind(editMode.editionState());
        
        this.setCenter(new BorderPane(bergesPojoTable, topPane, null, null, null));

    }
    
    /**
     * Ecouteur d'ajouts et suppressions de berges sur la base, pour mettre à jour les vues
     * montrant ces objets.
     */
    private class BergeDocumentListener implements DocumentListener {
        private final ObservableList<Berge> list;

        public BergeDocumentListener(final ObservableList<Berge> list) {
            this.list = list;
        }

        /**
         * A la création de documents, mise à jour de la liste en conséquence.
         *
         * @param added Nouveaux éléments à ajouter.
         */
        @Override
        public void documentCreated(Map<Class, List<Element>> added) {
            final List addedObl = added.get(Berge.class);
            if (addedObl == null || addedObl.isEmpty()) {
                return;
            }
            // On enlève les éléments déjà présents dans la liste de base, pour ne garder que les nouveaux
            // et ne pas les ajouter plusieurs fois dans la liste.
            addedObl.removeAll(list);
            final Runnable addRun = () -> list.addAll(addedObl);
            if (!Platform.isFxApplicationThread()) {
                Platform.runLater(addRun);
            } else {
                addRun.run();
            }
        }

        @Override
        public void documentChanged(Map<Class, List<Element>> changed) {
        }

        /**
         * Suppression des objets dans la liste.
         *
         * @param deletedObject Liste d'éléments à supprimer de la liste.
         */
        @Override
        public void documentDeleted(Map<Class, List<Element>> deletedObject) {
            final List deletedObj = deletedObject.get(Berge.class);
            if (deletedObj == null || deletedObj.isEmpty()) {
                return;
            }
            final Runnable delRun = () -> list.removeAll(deletedObj);
            if (!Platform.isFxApplicationThread()) {
                Platform.runLater(delRun);
            } else {
                delRun.run();
            }
        }
    }
}
