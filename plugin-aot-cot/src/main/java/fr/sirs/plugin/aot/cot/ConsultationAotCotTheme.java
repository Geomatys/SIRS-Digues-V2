package fr.sirs.plugin.aot.cot;

import fr.sirs.Injector;
import fr.sirs.core.component.ConventionRepository;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import fr.sirs.theme.ui.PojoTable;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;

/**
 * Bouton de suivi d'AOT / COT.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class ConsultationAotCotTheme extends AbstractPluginsButtonTheme {
    public ConsultationAotCotTheme() {
        super("Consulation AOT/COT", "Consultation AOT/COT", null);
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();
        
        final List<? extends Element> elements = Injector.getSession().getPrintManager().getElementsToPrint();
        
        if(elements==null || elements.isEmpty()){
            final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Aucun élément sélectionné.\nPour consulter une liste de conventions, veuillez consulter ou sélectionner un élement de réseau.", ButtonType.CLOSE);
            alert.setResizable(true);
            
        } 
        else if (elements.size()>1){
            final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Plusieurs éléments ont été sélectionnés.\nPour consulter une liste de conventions, veuillez consulter ou sélectionner un élement de réseau sans ambigüité.", ButtonType.CLOSE);
            alert.setResizable(true);
            
        }
        else {
            
            final Element element = elements.get(0);
            if (element instanceof ObjetReseau){
                
                final ConventionRepository conventionRepo = (ConventionRepository) Injector.getSession().getRepositoryForClass(Convention.class);
                
                final List<Convention> conventionsLiees = conventionRepo.getByReseau((ObjetReseau) element);
                
                final PojoTable table = new PojoTable(Convention.class, "Conventions de l'élément de réseau "+element.getDesignation());
                table.setTableItems(() -> (ObservableList) FXCollections.observableList(conventionsLiees));
                table.editableProperty().set(false);
                table.fichableProperty().set(false);
                
                borderPane.setCenter(table);
            } else {
                
                final Alert alert = new Alert(Alert.AlertType.INFORMATION, "L'élément sélectionné n'est pas un élément de réseau.\nPour consulter une liste de conventions, veuillez consulter ou sélectionner un élement de réseau.", ButtonType.CLOSE);
                alert.setResizable(true);
            }
        }

        return borderPane;
    }
}