package fr.sirs.theme;

import fr.sirs.Injector;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.AbstractPositionDocumentAssociable;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.PositionDocument;
import fr.sirs.core.model.ReferenceType;
import fr.sirs.core.model.SIRSDocument;
import fr.sirs.theme.ui.FXTronconThemePane;
import java.util.Collection;

import javafx.scene.Parent;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class TronconTheme extends AbstractTheme {

    
    public TronconTheme(String name, Class... classes) {
        super(name, classes);
    }
    
    protected TronconTheme(String name, ThemeManager... managers) {
        super(name, managers);
    }
    
    @Override
    public Parent createPane(){
        return new FXTronconThemePane(managers);
    }

    @Override
    public void initThemeManager(final ThemeManager... managers) {
        if(managers.length>1){
            for(ThemeManager manager : managers){
                final Theme subtheme = new TronconTheme(manager.getName(), manager);
                getSubThemes().add(subtheme);
            }
        }
    }
}
