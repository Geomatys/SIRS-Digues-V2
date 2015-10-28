
package fr.sirs.theme.ui;

import static fr.sirs.CorePlugin.initComboTypeDesordre;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RefCategorieDesordre;
import fr.sirs.core.model.RefTypeDesordre;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXDesordrePane extends FXDesordrePaneStub {
    
    public FXDesordrePane(final Desordre desordre){
        super(desordre);

        ui_categorieDesordreId.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                final AbstractSIRSRepository<RefTypeDesordre> typeRepo = Injector.getSession().getRepositoryForClass(RefTypeDesordre.class);
                final List<RefTypeDesordre> typesDispos = typeRepo.getAll();

                if(newValue instanceof Preview){
                    final String traitementId = ((Preview) newValue).getElementId();
                    if(traitementId!=null){
                        typesDispos.removeIf((RefTypeDesordre st) -> !traitementId.equals(st.getCategorieId()));
                    }
                }
                else if (newValue==null){
                    typesDispos.removeIf((RefTypeDesordre st) -> st.getCategorieId()!=null);
                }
                SIRS.initCombo(ui_typeDesordreId, FXCollections.observableList(typesDispos), null);
            }
        });
    }     

    /**
     * Initialize fields at element setting.
     * @param observableElement
     * @param oldElement
     * @param newElement
     */
    @Override
    protected void initFields(ObservableValue<? extends Desordre > observableElement, Desordre oldElement, Desordre newElement) {
        super.initFields(observableElement, oldElement, newElement);


        // Initialisation des catégories
        final AbstractSIRSRepository<RefCategorieDesordre> repoCategories = Injector.getSession().getRepositoryForClass(RefCategorieDesordre.class);
        final Map<String, RefCategorieDesordre> categories = new HashMap<>();

        for(final RefCategorieDesordre categorie : repoCategories.getAll()){
            categories.put(categorie.getId(), categorie);
        }

        final List<Preview> allCategoriePreviews = previewRepository.getByClass(RefCategorieDesordre.class);
        final List<Preview> categoriePreviews = new ArrayList<>();

        for(final Preview categoriePreview : allCategoriePreviews){
            final String categorieId = categoriePreview.getElementId();
            if(categorieId!=null){
                final RefCategorieDesordre categorie = categories.get(categorieId);
                if(categorie!=null){
                    categoriePreviews.add(categoriePreview);
                }
            }
        }


        SIRS.initCombo(ui_categorieDesordreId, FXCollections.observableList(categoriePreviews),
            newElement.getCategorieDesordreId()== null? null : previewRepository.get(newElement.getCategorieDesordreId()));



        // Initialisation des types
        final AbstractSIRSRepository<RefTypeDesordre> repoTypes = Injector.getSession().getRepositoryForClass(RefTypeDesordre.class);
        final Map<String, RefTypeDesordre> types = new HashMap<>();

        for(final RefTypeDesordre type : repoTypes.getAll()){
            types.put(type.getId(), type);
        }

        final List<Preview> typePreviews = previewRepository.getByClass(RefTypeDesordre.class);

        initComboTypeDesordre(newElement.getCategorieDesordreId(), newElement.getTypeDesordreId(), typePreviews, types, ui_typeDesordreId);
    }
}
