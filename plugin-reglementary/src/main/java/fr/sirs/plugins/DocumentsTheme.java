package fr.sirs.plugins;

import fr.sirs.Injector;
import fr.sirs.core.component.ObligationReglementaireRepository;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

/**
 * Panneau regroupant les fonctionnalités de suivi de documents.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class DocumentsTheme extends AbstractPluginsButtonTheme {
    private static final Image BUTTON_IMAGE = new Image(
            DocumentsTheme.class.getResourceAsStream("images/suivi_doc.png"));

    public DocumentsTheme() {
        super("Suivi des documents", "Suivi des documents", BUTTON_IMAGE);
    }

    @Override
    public Parent createPane() {
        final BorderPane borderPane = new BorderPane();
        
        ObligationReglementaire or = Injector.getSession().getElementCreator().createElement(ObligationReglementaire.class);

        or.setDesignation("jojo");
        ObligationReglementaireRepository orr = new ObligationReglementaireRepository(Injector.getSession().getConnector());
        
        orr.add(or);
        
        
//        PojoTable pt = new PojoTable(orr, "coucou");
        
        borderPane.setCenter(new Label(or.getId()+""+or.getDesignation()));
        
        return borderPane;
    }
}
