
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.DesordreLit;
import fr.sirs.core.model.RefCategorieDesordre;
import fr.sirs.core.model.RefTypeDesordre;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

/**
 * Surcharge du controlleur nécessaire pour prendre en compte les catégories de désordres ajoutées dans le noyau.
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXDesordreLitPane extends FXDesordreLitPaneStub {

    public FXDesordreLitPane(final DesordreLit desordreLit){
        super(desordreLit);
        
        // Update available types according to chosen category.
        final AbstractSIRSRepository<RefTypeDesordre> typeRepo = Injector.getSession().getRepositoryForClass(RefTypeDesordre.class);
        ui_categorieDesordreId.getSelectionModel().selectedItemProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {

            final ObservableList<RefTypeDesordre> typeList = SIRS.observableList(typeRepo.getAll());
            if (newValue instanceof RefCategorieDesordre) {
                final FilteredList fList = typeList.filtered(type -> type.getCategorieId().equals(((RefCategorieDesordre) newValue).getId()));
                ui_typeDesordreId.setItems(fList);
            } else {
                ui_typeDesordreId.setItems(typeList);
            }
        });
    }
}
