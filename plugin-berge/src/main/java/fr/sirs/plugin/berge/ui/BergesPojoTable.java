
package fr.sirs.plugin.berge.ui;

import fr.sirs.theme.ui.PojoTable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.geotoolkit.filter.DefaultPropertyIsLike;
import org.geotoolkit.filter.DefaultPropertyName;
import org.geotoolkit.filter.binarylogic.DefaultAnd;
import org.opengis.filter.Filter;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class BergesPojoTable extends PojoTable {

    private final TextField uiKeywordSearch = new TextField();
    
     /**
     * Création de la table présentant les obligations réglementaires.
     *
     * @param clazz   Classe d'objets affichés par cette table
     */
    public BergesPojoTable(final Class clazz) {
        super(clazz, "Liste des berges");

        if (getFilterUI() instanceof VBox) {
            final VBox vbox = (VBox) getFilterUI();
            final HBox hbox = new HBox();
            hbox.setSpacing(20);
            final Label label = new Label("Recherche par mots clés");
            label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            hbox.getChildren().add(label);
            hbox.getChildren().add(uiKeywordSearch);
            vbox.getChildren().add(vbox.getChildren().size() - 1, hbox);
        }
    }
    
    @Override
    public Filter getFilter() {
        if (uiKeywordSearch == null || !uiKeywordSearch.getText().isEmpty()) {
            return super.getFilter();
        }

        final Filter filterKWSearch = new DefaultPropertyIsLike(new DefaultPropertyName("libellé"), uiKeywordSearch.getText() + "*", "*", "?", "\\", false);
        final Filter filter = super.getFilter();
        if (filter == null) {
            return filterKWSearch;
        }

        return new DefaultAnd(filterKWSearch, filter);
    }
    
    @Override
    public void resetFilter(final VBox filterContent) {
        super.resetFilter(filterContent);

        uiKeywordSearch.setText("");
    }
}
