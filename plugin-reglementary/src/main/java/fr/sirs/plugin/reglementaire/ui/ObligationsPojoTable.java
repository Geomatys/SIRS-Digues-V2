package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.core.component.ObligationReglementaireRepository;
import fr.sirs.core.component.SystemeEndiguementRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.theme.ui.PojoTable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.gui.javafx.util.FXTableCell;
import org.geotoolkit.internal.GeotkFX;

/**
 * Table présentant les obligations réglementaires.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class ObligationsPojoTable extends PojoTable {
    /**
     * Nom de la colonne à ajouter pour la table.
     */
    private static final String CLASS_PROP_NAME = "Classe";

    private static final String PLANIF_PROP_NAME = "Planification";

    /**
     * Création de la table présentant les obligations réglementaires.
     */
    public ObligationsPojoTable() {
        super(ObligationReglementaire.class, "Liste des obligations réglementaires");

        // Ajout de la colonne de duplication
        getColumns().add(2, new DuplicateObligationColumn());

        final ObservableList<TableColumn<Element, ?>> cols = getColumns();
        for (final TableColumn col : cols) {
            if (PLANIF_PROP_NAME.equals(col.getText())) {
                cols.remove(col);
                break;
            }
        }

        getUiTable().getColumns().add(5, new SEClassTableColumn());
    }

    /**
     * Colonne représentant la classe du système d'endiguement.
     */
    private class SEClassTableColumn extends TableColumn<ObligationReglementaire, String> {
        public SEClassTableColumn() {
            super(CLASS_PROP_NAME);

            setCellValueFactory(param -> param.getValue().systemeEndiguementIdProperty());
            setCellFactory(param -> new SEClassCell());

            setEditable(false);
        }
    }

    /**
     * Cellule représentant la classe du système d'endiguement.
     */
    private class SEClassCell extends FXTableCell<ObligationReglementaire, String> {
        public SEClassCell() {
            super();
            setAlignment(Pos.CENTER);
        }

        /**
         * Garde la correspondance entre la classe du système d'endiguement affichée dans la cellule et la propriété
         * correspondante dans le système d'endiguement.
         *
         * @param item La propriété id du système d'endiguement
         * @param empty Vrai si la cellule est vide
         */
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (!empty && item != null) {
                textProperty().bind(Injector.getBean(SystemeEndiguementRepository.class).get(item).classementProperty());
            } else {
                textProperty().unbind();
                setText(null);
            }
        }
    }

    public static class DuplicateObligationColumn extends TableColumn {

        public DuplicateObligationColumn() {
            super("Dupliquer");
            setSortable(false);
            setResizable(false);
            setPrefWidth(24);
            setMinWidth(24);
            setMaxWidth(24);
            setGraphic(new ImageView(GeotkFX.ICON_DUPLICATE));

            setCellValueFactory(new Callback<CellDataFeatures, ObservableValue>() {

                @Override
                public ObservableValue call(TableColumn.CellDataFeatures param) {
                    return new SimpleObjectProperty(param.getValue());
                }
            });

            setCellFactory(new Callback<TableColumn, TableCell>() {

                @Override
                public TableCell call(TableColumn param) {
                    return new ButtonTableCell(
                            false, new ImageView(GeotkFX.ICON_DUPLICATE),
                            (Object t) -> t != null, (Object t) -> {
                        if (t instanceof ObligationReglementaire) {
                            final ObligationReglementaire initialObligation = (ObligationReglementaire) t;
                            final ObligationReglementaire newObligation = initialObligation.copy();
                            final ObligationReglementaireRepository orr = Injector.getBean(ObligationReglementaireRepository.class);
                            orr.add(newObligation);
                        }
                        return t;
                    });
                }
            });
        }
    }
}
