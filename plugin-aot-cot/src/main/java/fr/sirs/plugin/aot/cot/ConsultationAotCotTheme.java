package fr.sirs.plugin.aot.cot;

import fr.sirs.Injector;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ObjetReseau;
import static fr.sirs.plugin.aot.cot.PluginAotCot.getConventionsForReseau;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import java.util.List;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

/**
 * Bouton de suivi d'AOT / COT.
 *
 * @author Cédric Briançon (Geomatys)
 * @author Samuel Andrés (Geomatys)
 */
public final class ConsultationAotCotTheme extends AbstractPluginsButtonTheme {
    private static final Image BUTTON_IMAGE = new Image(
            ConsultationAotCotTheme.class.getResourceAsStream("images/aot-objAssocies.png"));
    public ConsultationAotCotTheme() {
        super("Consulation AOT/COT", "Consultation AOT/COT", BUTTON_IMAGE);
    }
    
    private ObjetReseau reseauToConsultFromMap = null;

    public void setReseauToConsultFromMap(final ObjetReseau reseau){
        reseauToConsultFromMap = reseau;
    }
    
    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();
        
        // On commence par vérifier qu'aucun objet n'est sélectionné par la carte pour traitement par ce thème.
        if(reseauToConsultFromMap!=null){
            borderPane.setCenter(getConventionsForReseau(reseauToConsultFromMap));
            // Réinitialisation de l'objet courant à consulter.
            reseauToConsultFromMap=null;
        }
        // Sinon on recherche l'objet courant sélectionné dans l'interface qui est également l'élément sélectionné pour impression.
        else{
            final List<? extends Element> elements = Injector.getSession().getPrintManager().getElementsToPrint();

            final String msg;
            if(elements!=null && elements.size()==1 && elements.get(0) instanceof ObjetReseau){
                borderPane.setCenter(getConventionsForReseau((ObjetReseau) elements.get(0)));
                msg=null;
            } else if( elements ==null || elements.isEmpty()){
                msg="Aucun élément sélectionné.\nPour consulter une liste de conventions, veuillez consulter ou sélectionner un élement de réseau.";
            } else if (elements.size()>1){
                msg="Plusieurs éléments ont été sélectionnés.\nPour consulter une liste de conventions, veuillez consulter ou sélectionner un élement de réseau sans ambigüité.";
            } else if (!(elements.get(0) instanceof ObjetReseau)){
                msg="L'élément sélectionné n'est pas un élément de réseau.\nPour consulter une liste de conventions, veuillez consulter ou sélectionner un élement de réseau.";
            } else { // Normalement ce cas ne doit jamais se présenter car toutes les possibilités ont été épuisées.
                msg=null;
            }

            if(msg!=null){
                final Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.CLOSE);
                alert.setResizable(true);
                alert.showAndWait();
            }
        }

        return borderPane;
    }
}