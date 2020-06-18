/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 *
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.map;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.LabelComparator;
import fr.sirs.util.SirsStringConverter;
import fr.sirs.util.ReferenceTableCell;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.util.FXTableCell;
import org.geotoolkit.gui.javafx.util.FXTableView;
import org.geotoolkit.gui.javafx.util.TaskManager;
import org.geotoolkit.util.StringUtilities;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Matthieu Bastianelli (Geomatys)
 * @param <T>
 */
public abstract class FXAbstractEditOnTronconPane <T extends Objet> extends BorderPane {

    @FXML TextField uiTronconLabel;
    @FXML ToggleButton uiPickTroncon;
    @FXML FXTableView<T> uiObjetTable;
    @FXML ToggleButton uiCreateObjet;
    @FXML Label typeNameLabel;

    final ObjectProperty<TronconDigue> tronconProp = new SimpleObjectProperty<>();
    final ObjectProperty<EditModeObjet> mode = new SimpleObjectProperty<>(EditModeObjet.NONE);
    final Session session;
    final FXMap map;

    /** A flag to indicate that selected {@link TronconDigue} must be saved. */
    final SimpleBooleanProperty saveTD = new SimpleBooleanProperty(false);

    final Class<T> editedClass;
    final AbstractSIRSRepository<T> repo;

    private final String defaultEmptyLibelle = "Aucun tronçon sélectionné";


    /**
     *
     * @param map
     * @param typeName
     * @param clazz
     * @param createNameColumn : indicates if the name column must be set.
     */
    public FXAbstractEditOnTronconPane(FXMap map, final String typeName, final Class clazz, final boolean createNameColumn) {
        SIRS.loadFXML(this);
        setTypeNameLabel(typeName);

        editedClass = clazz;
        this.map = map;
        session = Injector.getSession();

        repo = session.getRepositoryForClass(editedClass);

        uiPickTroncon.setGraphic(new ImageView(SIRS.ICON_CROSSHAIR_BLACK));

        uiObjetTable.setEditable(true);

        uiPickTroncon.setOnAction(this::startPickTroncon);
        uiCreateObjet.setOnAction(this::startCreateObjet);

        // Affichage du libellé du tronçon
        uiTronconLabel.textProperty().bind(Bindings.createStringBinding(()->tronconProp.get()==null?defaultEmptyLibelle:tronconProp.get().getLibelle(),tronconProp));

        //colonne de la table
        if (createNameColumn) {
            addColumToTable(new NameColumn(), true);
        }

        // Initialize event listeners
        tronconProp.addListener(this::tronconChanged);

    }

    final void setTypeNameLabel(final String name){
        typeNameLabel.setText(StringUtilities.firstToUpper(name)+ " :");
    }

    /**
     * Set the input Tablecolumn to the {@link #uiObjetTable}
     * and set it sortable if needed.
     * @param toAddColumn
     * @param toSort
     */
    final void addColumToTable(final TableColumn toAddColumn, final boolean toSort) {
            uiObjetTable.getColumns().add(toAddColumn);
            if (toSort)
                toAddColumn.setSortable(true);
    }

    public void reset(){
        mode.set(EditModeObjet.PICK_TRONCON);
        tronconProperty().set(null);
    }

    public ReadOnlyObjectProperty<EditModeObjet> modeProperty(){
        return mode;
    }

    ObjectProperty<EditModeObjet> getModeProperty(){
        return mode;
    }

    public ObjectProperty<TronconDigue> tronconProperty(){
        return tronconProp;
    }

    public TronconDigue getTronconFromProperty(){
        return tronconProp.get();
    }

    EditModeObjet getMode() {
        return mode.get();
    }

    public ObservableList<T> objetProperties(){
        return uiObjetTable.getSelectionModel().getSelectedItems();
    }

    public void save() {
        save(tronconProp.getValue());
    }

    private void save(final TronconDigue td) {
        final boolean mustSaveTd = saveTD.get();

        if (mustSaveTd) {
            saveTD.set(false);

            TaskManager.INSTANCE.submit("Sauvegarde...", () -> {
                if (td != null && mustSaveTd) {
                    ((AbstractSIRSRepository) session.getRepositoryForClass(td.getClass())).update(td);
                }
            });
        }
    }

    private void startPickTroncon(ActionEvent evt){
        mode.set(EditModeObjet.PICK_TRONCON);
    }

    /*
     * OBJET UTILITIES
     */

    /**
     * Constuit un composant graphique listant les éléments du tronçon.
     *
     * @param toExclude Liste des identifiants des éléments à exclure de la liste.
     * @return A list view of all bornes bound to currently selected troncon, or
     * null if no troncon is selected.
     */
    ListView<T> buildObjetList(final Set<String> toExclude) {

        // Construction du composant graphique.
        final ListView<T> elementsView = new ListView<>();
        elementsView.setItems(getObjectListFromTroncon(toExclude));
        elementsView.setCellFactory(TextFieldListCell.forListView(new SirsStringConverter()));
        elementsView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        return elementsView;
    }

    /**
     * Retourne la liste des éléments T de la base associés au tronçon sélectionné.
     *
     * @param toExclude
     * @return
     */
    ObservableList<T> getObjectListFromTroncon(final Set<String> toExclude) {

        final TronconDigue troncon = tronconProperty().get();
        if (troncon == null) return null;

//        // Construction de la liste définitive des éléments à afficher.
        final List<T> elements =repo.getAll().stream()
                    .filter(elt -> (troncon.getId().equals( ((Objet) elt).getForeignParentId())))
                    .collect(Collectors.toList());

        if (toExclude != null && !toExclude.isEmpty()) {
            elements.removeIf(elt -> toExclude.contains(elt.getId()));
        }
        return FXCollections.observableArrayList(elements);
    }

    /**
     * Ajout


    /**
     * Action du bouton de création.
     * @param evt
     */
    void startCreateObjet(ActionEvent evt){
        if(mode.get().equals(EditModeObjet.CREATE_OBJET)){
            //on retourne on mode edition
            mode.set(EditModeObjet.EDIT_OBJET);
        }else{
            mode.set(EditModeObjet.CREATE_OBJET);
        }
    }

    /**
     * Open a {@link ListView} to allow user to select one or more {@link BorneDigue}
     * to delete.
     *
     * Note : Once suppression is confirmed, we're forced to check all {@link SystemeReperage}
     * defined on the currently edited {@link TronconDigue}, and update them if
     * they use chosen bornes.
     *
     * @param e Event fired when deletion button has been fired.
     */
    @FXML
    abstract void deleteObjets(ActionEvent e);


    void tronconChanged(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) {

        if (oldValue != null) {
            save(oldValue);
        }

        if (newValue == null) {
            uiObjetTable.setItems(FXCollections.emptyObservableList());
            mode.set(EditModeObjet.NONE); //Todo?
        } else {
            final EditModeObjet current = getMode();
            if (current.equals(EditModeObjet.CREATE_OBJET) || current.equals(EditModeObjet.EDIT_OBJET)) {
                //do nothing
            } else {
                mode.set(EditModeObjet.EDIT_OBJET);
            }

            uiObjetTable.setItems(getObjectListFromTroncon(null));
        }

    }

    /*
     * TABLE UTILITIES
     */



    /**
     * Colonne d'affichage et de mise à jour du nom d'une borne.
     */
    private static class NameColumn extends TableColumn<Element,Element>{

        public NameColumn() {
            super("Nom");
            setSortable(false);

            setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Element, Element>, ObservableValue<Element>>() {
                @Override
                public ObservableValue<Element> call(TableColumn.CellDataFeatures<Element, Element> param) {
                    return new SimpleObjectProperty<>(param.getValue());
                }
            });

            final SirsStringConverter sirsStringConverter = new SirsStringConverter();
            setCellFactory((TableColumn<Element, Element> param) -> {
                final FXTableCell<Element, Element> tableCell = new FXTableCell<Element, Element>() {

                    @Override
                    protected void updateItem(Element item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setGraphic(new ImageView(ReferenceTableCell.ICON_LINK));
                            setText(sirsStringConverter.toString(item));
                        }
                    }

                };
                tableCell.setEditable(false);
                return tableCell;
            });

            setComparator(new LabelComparator());
        }
    }

}
