
package fr.sirs.plugin.document.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.RapportModeleDocument;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.plugin.document.FileTreeItem;
import fr.sirs.plugin.document.ODTUtils;
import static fr.sirs.plugin.document.PropertiesFileUtilities.*;
import static fr.sirs.plugin.document.ui.DocumentsPane.DYNAMIC;
import static fr.sirs.plugin.document.ui.DocumentsPane.MODELE;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;
import org.apache.sis.util.logging.Logging;

/**
 *
 * @author guilhem
 */
public class GenerationPane extends GridPane {
    
    @FXML
    public ProgressIndicator uiProgress;

    @FXML
    public Button uiGenerateFinish;

    @FXML
    public Label uiProgressLabel;
    
    private static final Logger LOGGER = Logging.getLogger(GenerationPane.class);
    
    public GenerationPane() {
        SIRS.loadFXML(this, GenerationPane.class);
        Injector.injectDependencies(this);
        uiProgress.setVisible(false);
        uiGenerateFinish.setVisible(false);
    }
    
    public void generateDoc(final String docName, boolean onlySe, final RapportModeleDocument modele, final Collection<TronconDigue> troncons, final File seDir,
            final FileTreeItem root) {

        uiProgress.setVisible(true);
        try {
            if (onlySe) {
                final File docDir = new File(seDir, DocumentsPane.DOCUMENT_FOLDER);
                final File newDoc = new File(docDir, docName);
                
                Platform.runLater(()->uiProgressLabel.setText("Recherche des objets du rapport..."));
                Map<String, Objet> objects  = getElements(troncons);
                
                ODTUtils.write(modele, newDoc, objects, uiProgressLabel, "");
                setBooleanProperty(newDoc, DYNAMIC, true);
                setProperty(newDoc, MODELE, modele.getId());
            } else {
                final DigueRepository digueRepo = (DigueRepository) Injector.getSession().getRepositoryForClass(Digue.class);
                final int total = troncons.size();
                int i = 1;
                for (TronconDigue troncon : troncons) {
                    final String prefix = "Tronçon " + i + '/' + total + ": ";
                    final File digDir = getOrCreateDG(seDir, digueRepo.get(troncon.getDigueId()));
                    final File docDir = new File(getOrCreateTR(digDir, troncon), DocumentsPane.DOCUMENT_FOLDER);
                    final File newDoc = new File(docDir, docName);
                    Platform.runLater(()->uiProgressLabel.setText(prefix + "Recherche des objets du rapport..."));
                    Map<String, Objet> objects  = getElements(Collections.singleton(troncon));
                    
                    ODTUtils.write(modele, newDoc, objects, uiProgressLabel, prefix);
                    setBooleanProperty(newDoc, DYNAMIC, true);
                    setProperty(newDoc, MODELE, modele.getId());
                    i++;
                }
                Platform.runLater(()->uiProgressLabel.setText("Génération terminée"));
            }

            // reload tree
            root.update();
            uiProgress.setVisible(false);
            uiGenerateFinish.setVisible(true);

        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, null, ex);
            uiProgress.setVisible(false);
            uiGenerateFinish.setVisible(true);
            uiProgressLabel.setText("Erreur pendant la generation");
        }
    }
    
    
    public void reGenerateDoc(final RapportModeleDocument modele, final Collection<TronconDigue> troncons, final File item, final FileTreeItem root) {

        uiProgress.setVisible(true);
        try {
            Platform.runLater(() -> uiProgressLabel.setText("Recherche des objets du rapport..."));
            Map<String, Objet> objects = getElements(troncons);

            ODTUtils.write(modele, item, objects, uiProgressLabel, "");

            Platform.runLater(() -> uiProgressLabel.setText("Génération terminée"));

            // reload tree
            root.update();
            uiProgress.setVisible(false);
            uiGenerateFinish.setVisible(true);

        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, null, ex);
            uiProgress.setVisible(false);
            uiGenerateFinish.setVisible(true);
            uiProgressLabel.setText("Erreur pendant la generation");
        }
    }
    
    public void writeDoSynth(final FileTreeItem item, final File f) {
        uiProgress.setVisible(true);
        try {
            Platform.runLater(() -> uiProgressLabel.setText("Recherche des fichers a aggréger..."));
            
            ODTUtils.writeDoSynth(item, f, uiProgressLabel);
            
            uiProgress.setVisible(false);
            Platform.runLater(() -> uiProgressLabel.setText("Génération terminée"));
            uiGenerateFinish.setVisible(true);
            
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, null, ex);
            uiProgress.setVisible(false);
            uiGenerateFinish.setVisible(true);
            uiProgressLabel.setText("Erreur pendant la generation");
        }
    }
}
