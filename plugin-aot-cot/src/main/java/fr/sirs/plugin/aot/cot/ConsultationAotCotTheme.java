package fr.sirs.plugin.aot.cot;

import fr.sirs.Injector;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Objet;
import static fr.sirs.plugin.aot.cot.PluginAotCot.getConventionsForObjet;
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
    
    private Objet toConsultFromMap = null;

    public void setObjetToConsultFromMap(final Objet objet){
        toConsultFromMap = objet;
    }

    /**
     * AOT/COT consultation must not be cached.
     * @return
     */
    @Override
    public boolean isCached(){return false;}
    
    @Override
    public Parent createPane() {
        final BorderPane borderPane;
        
        // On commence par vérifier qu'aucun objet n'est sélectionné par la carte pour traitement par ce thème.
        if(toConsultFromMap!=null){
            borderPane = new BorderPane();
            borderPane.setCenter(getConventionsForObjet(toConsultFromMap));
            // Réinitialisation de l'objet courant à consulter.
            toConsultFromMap=null;
        }
        // Sinon on recherche l'objet courant sélectionné dans l'interface qui est également l'élément sélectionné pour impression.
        else{
            final List<? extends Element> elements = Injector.getSession().getPrintManager().getElementsToPrint();

            if(elements!=null && elements.size()==1 && elements.get(0) instanceof Objet){
                borderPane = new BorderPane();
                borderPane.setCenter(getConventionsForObjet((Objet) elements.get(0)));
            } else {
                final String msg;
                borderPane = null;
                if( elements ==null || elements.isEmpty()){
                    msg="Aucun élément sélectionné.\nPour consulter une liste de conventions, veuillez consulter ou sélectionner un objet.";
                } else if (elements.size()>1){
                    msg="Plusieurs éléments ont été sélectionnés.\nPour consulter une liste de conventions, veuillez consulter ou sélectionner un objet sans ambigüité.";
                } else if (!(elements.get(0) instanceof Objet)){
                    msg="L'élément sélectionné n'est pas un objet.\nPour consulter une liste de conventions, veuillez consulter ou sélectionner un objet.";
                } else { // Normalement ce cas ne doit jamais se présenter car toutes les possibilités ont été épuisées.
                    msg=null;
                }

                if(msg!=null){
                    final Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.CLOSE);
                    alert.setResizable(true);
                    alert.showAndWait();
                }
            }
        }

        return borderPane;
    }
}