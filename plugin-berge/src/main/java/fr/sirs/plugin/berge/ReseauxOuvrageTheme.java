
package fr.sirs.plugin.berge;

import fr.sirs.core.model.EchelleLimnimetrique;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.StationPompage;
import fr.sirs.plugin.berge.ui.AbstractDescriptionPane;
import fr.sirs.plugin.berge.util.TabContent;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ReseauxOuvrageTheme extends AbstractDescriptionTheme {

    public ReseauxOuvrageTheme() {
        super("Réseaux et ouvrages", "Réseaux et ouvrages");
    }
    
    @Override
    public Parent createPane() {
        List<TabContent> content = new ArrayList<>();
        content.add(new TabContent("Station pompage", "Tableau des stations de pompage", StationPompage.class));
        content.add(new TabContent("Réseau hydrau. fermé", "Tableau des réseaux hydrauliques fermés", ReseauHydrauliqueFerme.class));
        content.add(new TabContent("Ouvrage hydrau. associé", "Tableau des ouvrages hydrauliques associés", OuvrageHydrauliqueAssocie.class));
        content.add(new TabContent("Réseau télécom/énergie", "Tableau des réseaux télécom/énérgie", ReseauTelecomEnergie.class));
        content.add(new TabContent("Ouvrage télécom/énergie", "Tableau des ouvrages télécom/énérgie", OuvrageTelecomEnergie.class));
        content.add(new TabContent("Réseau hydrau. ciel ouvert", "Tableau des réseaux hydrauliques a ciel ouvert", ReseauHydrauliqueCielOuvert.class));
        content.add(new TabContent("Ouvrage part.", "Tableau des ouvrages particulier", OuvrageParticulier.class));
        content.add(new TabContent("Echelles limnimétriques", "Tableau des échelles limnimétriques", EchelleLimnimetrique.class));
        
        final BorderPane borderPane = new AbstractDescriptionPane(content);
        return borderPane;
    }
}
