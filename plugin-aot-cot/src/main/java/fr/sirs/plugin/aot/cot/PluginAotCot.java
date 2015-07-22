package fr.sirs.plugin.aot.cot;

import fr.sirs.FXMainFrame;
import fr.sirs.Injector;
import fr.sirs.Plugin;
import fr.sirs.SIRS;
import fr.sirs.core.component.ConventionRepository;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.theme.ui.PojoTable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

/**
 * Plugin correspondant au module AOT COT.
 *
 * @author Alexis Manin (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class PluginAotCot extends Plugin {
    private static final String NAME = "plugin-aot-cot";
    private static final String TITLE = "Module AOT COT";
    
    private final ConsultationAotCotTheme consultationAotCotTheme;

    public PluginAotCot() {
        name = NAME;
        loadingMessage.set("module AOT COT");
        themes.add(new CreateAotCotTheme());
        themes.add(new SuiviAotCotTheme());
        consultationAotCotTheme = new ConsultationAotCotTheme();
        themes.add(consultationAotCotTheme);
        themes.add(new AssociatedDocumentsAotCotTheme());
    }

    @Override
    public void load() throws Exception {
        getConfiguration();
    }

    @Override
    public CharSequence getTitle() {
        return TITLE;
    }

    @Override
    public Image getImage() {
        // TODO: choisir une image pour ce plugin
        return null;
    }
    
    

    @Override
    public List<MenuItem> getMapActions(Object obj) {
        final List<MenuItem> lst = new ArrayList<>();
        
        if(obj instanceof ObjetReseau) {
            lst.add(new ViewFormItem((ObjetReseau) obj));
        }
        
        return lst;
    }
    
    private class ViewFormItem extends MenuItem {

        public ViewFormItem(ObjetReseau candidate) {
            setText("Consulter les conventions de "+getSession().generateElementTitle(candidate));
            setOnAction((ActionEvent event) -> {
                consultationAotCotTheme.setReseauToConsultFromMap(candidate);
                getSession().getFrame().addTab(getSession().getOrCreateThemeTab(consultationAotCotTheme));
            });
        }
    }
    
    public static PojoTable getConventionsForReseau(final ObjetReseau reseau){
        final ConventionRepository conventionRepo = (ConventionRepository) Injector.getSession().getRepositoryForClass(Convention.class);
                
        final List<Convention> conventionsLiees = conventionRepo.getByReseau(reseau);

        final PojoTable table = new PojoTable(Convention.class, "Conventions de l'élément de réseau "+reseau.getDesignation());
        table.setTableItems(() -> (ObservableList) FXCollections.observableList(conventionsLiees));
        table.editableProperty().set(false);
        table.fichableProperty().set(false);
        return table;
    }
}
