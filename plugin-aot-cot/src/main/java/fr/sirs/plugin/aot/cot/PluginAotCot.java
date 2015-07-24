package fr.sirs.plugin.aot.cot;

import fr.sirs.Injector;
import fr.sirs.Plugin;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.ConventionRepository;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.ui.AlertItem;
import fr.sirs.ui.AlertManager;
import fr.sirs.util.SirsStringConverter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;

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
        themes.add(new SuiviAotCotTheme());
        consultationAotCotTheme = new ConsultationAotCotTheme();
        themes.add(consultationAotCotTheme);
    }

    @Override
    public void load() throws Exception {
        getConfiguration();
        showAlerts();
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
    
    /**
     * Récupère les alertes à afficher pour l'utilisateur, selon les dates fournies dans les obligations réglementaires
     * et la fréquence de rappel.
     */
    public static void showAlerts() {
        final List<AlertItem> alerts = new ArrayList<>();

        final AbstractSIRSRepository<Convention> orr = Injector.getSession().getRepositoryForClass(Convention.class);
        final List<Convention> obligations = orr.getAll();
        if (obligations.isEmpty()) {
            AlertManager.getInstance().addAlerts(alerts);
            return;
        }

        for (final Convention obligation : obligations) {
            if (obligation.getDate_fin()== null) {
                continue;
            }

            final StringBuilder sb = new StringBuilder();
            sb.append(new SirsStringConverter().toString(obligation));
            
//            if (obligation.getDate_fin().compareTo(LocalDate.now())<0)
//                alerts.add(new AlertItem(sb.toString(), obligation.getDate_fin(), AlertItem.AlertItemLevel.HIGHT));
//            else 
                if(obligation.getDate_fin().minusMonths(6).compareTo(LocalDate.now())<0
                        && obligation.getDate_fin().compareTo(LocalDate.now())>=0) // On ne veut pas d'alerte pour les conventions dont la date de fin est déjà dépassée
                alerts.add(new AlertItem(sb.toString(), obligation.getDate_fin()));
//            else 
//                alerts.add(new AlertItem(sb.toString(), obligation.getDate_fin(), AlertItem.AlertItemLevel.INFORMATION));
        }

        AlertManager.getInstance().addAlerts(alerts);
    }
}

