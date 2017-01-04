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
package fr.sirs.digue;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractTronconDigueRepository;
import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.Theme;
import fr.sirs.util.DefaultElementComparator;
import fr.sirs.util.SirsStringConverter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

public class FXDiguesPane extends FXAbstractTronconTreePane {

    private final SirsStringConverter converter = new SirsStringConverter();

    public FXDiguesPane() {
        super("Systèmes d'endiguement");
        uiTree.setCellFactory((param) -> new CustomizedTreeCell());
        uiAdd.getItems().add(new NewSystemeMenuItem(null));
        uiAdd.getItems().add(new NewDigueMenuItem(null));
        updateTree();
    }

    @Override
    public void deleteSelection(ActionEvent event) {
        Object obj = uiTree.getSelectionModel().getSelectedItem();
        if(obj instanceof TreeItem){
            obj = ((TreeItem)obj).getValue();
        }

        if(obj instanceof SystemeEndiguement){
            final SystemeEndiguement se = (SystemeEndiguement) obj;

            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "La suppression de la digue "+se.getLibelle()+" ne supprimera pas les digues qui la compose, "
                   +"celles ci seront déplacées dans le groupe 'Non classés. Confirmer la suppression ?",
                    ButtonType.YES, ButtonType.NO);
            alert.setResizable(true);
            final ButtonType res = alert.showAndWait().get();
            if (res == ButtonType.YES) {
                session.getRepositoryForClass(SystemeEndiguement.class).remove(se);
            }

        }else if(obj instanceof Digue){
            final Digue digue = (Digue) obj;
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "La suppression de la digue "+digue.getLibelle()+" ne supprimera pas les tronçons qui la compose, "
                   +"ceux ci seront déplacés dans le groupe 'Non classés. Confirmer la suppression ?",
                    ButtonType.YES, ButtonType.NO);
            alert.setResizable(true);
            final ButtonType res = alert.showAndWait().get();
            if (res == ButtonType.YES) {
                //on enleve la reference a la digue dans les troncons
                final List<TronconDigue> troncons = ((AbstractTronconDigueRepository) session.getRepositoryForClass(TronconDigue.class)).getByDigue(digue);
                for(final TronconDigue td : troncons){
                    td.setDigueId(null);
                    session.getRepositoryForClass(TronconDigue.class).update(td);
                }
                //on supprime la digue
                session.getRepositoryForClass(Digue.class).remove(digue);
            }
        }else if(obj instanceof TronconDigue){
            final TronconDigue td = (TronconDigue) obj;
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Confirmer la suppression du tronçon "+td.getLibelle()+" ?",
                    ButtonType.YES, ButtonType.NO);
            alert.setResizable(true);
            final ButtonType res = alert.showAndWait().get();
            if (res == ButtonType.YES) {
                session.getRepositoryForClass(TronconDigue.class).remove(td);
            }
        }
    }

    @Override
    public final Task updateTree() {
        TreeItem<? extends Element> selectedItem = uiTree.getSelectionModel().getSelectedItem();
        final Element lastSelected;
        if (selectedItem != null && selectedItem.getValue() instanceof Element) {
            lastSelected = selectedItem.getValue();
        } else {
            lastSelected = null;
        }
        return Injector.getSession().getTaskManager().submit("Mise à jour de l'arbre des digues", () -> {
            Platform.runLater(() -> uiSearch.setGraphic(searchRunning));

            //on stoque les noeuds ouverts
            final Set extendeds = new HashSet();
            searchExtended(uiTree.getRoot(), extendeds);

            //creation des filtres
            Predicate<? super TronconDigue> filter = getFilter();

            //creation de l'arbre
            final TreeItem treeRootItem = new TreeItem("root");

            //on recupere tous les elements
            final Comparator<Element> comparator = new DefaultElementComparator();
            final List<SystemeEndiguement> sds = FXCollections.observableArrayList(session.getRepositoryForClass(SystemeEndiguement.class).getAll()).sorted((Comparator)comparator);
            final List<Digue> digues = new ArrayList<>(session.getRepositoryForClass(Digue.class).getAll());
            Collections.sort(digues, comparator);
            final List<TronconDigue> troncons = new ArrayList<>(((TronconDigueRepository) session.getRepositoryForClass(TronconDigue.class)).getAll());
            Collections.sort(troncons, comparator);
            final Set<Digue> diguesFound = new HashSet<>();
            final Set<TronconDigue> tronconsFound = new HashSet<>();

            for(final SystemeEndiguement sd : sds) {
                final TreeItem sdItem = new TreeItem(sd);
                treeRootItem.getChildren().add(sdItem);
                sdItem.setExpanded(extendeds.contains(sd));

                final List<Digue> digueIds = ((DigueRepository) session.getRepositoryForClass(Digue.class)).getBySystemeEndiguement(sd);
                for(Digue digue : digues) {
                    if(!digueIds.contains(digue)) continue;
                    diguesFound.add(digue);
                    final TreeItem digueItem = toNode(digue, troncons, tronconsFound, filter);
                    digueItem.setExpanded(extendeds.contains(digue));
                    sdItem.getChildren().add(digueItem);
                }
            }

            //on place toute les digues et troncons non trouvé dans un group a part
            digues.removeAll(diguesFound);
            final TreeItem ncItem = new TreeItem("Non classés");
            ncItem.setExpanded(extendeds.contains(ncItem.getValue()));
            treeRootItem.getChildren().add(ncItem);

            for(final Digue digue : digues){
                final TreeItem digueItem = toNode(digue, troncons, tronconsFound, filter);
                ncItem.getChildren().add(digueItem);
                digueItem.setExpanded(extendeds.contains(digue));
            }
            troncons.removeAll(tronconsFound);
            for(final TronconDigue tc : troncons){
                if (filter == null || filter.test(tc)) {
                    ncItem.getChildren().add(new TreeItem(tc));
                }
            }

            final Optional<TreeItem> toSelect = searchItem(lastSelected, treeRootItem);
            Platform.runLater(() -> {
                uiTree.setRoot(treeRootItem);
                if (toSelect.isPresent()) {
                    uiTree.getSelectionModel().select(toSelect.get());
                }
                uiSearch.setGraphic(searchNone);
            });
        });
    }

    private static TreeItem toNode(final Digue digue, final Collection<TronconDigue> troncons,
            final Set<TronconDigue> tronconsFound, final Predicate<? super TronconDigue> filter){
        final TreeItem digueItem = new TreeItem(digue);
        for(final TronconDigue td : troncons){
            if(td.getDigueId()==null || !td.getDigueId().equals(digue.getDocumentId())) continue;
            tronconsFound.add(td);
            if(filter==null || filter.test(td)){
                final TreeItem tronconItem = new TreeItem(td);
                digueItem.getChildren().add(tronconItem);
            }
        }
        return digueItem;
    }

    @Override
    public void documentCreated(Map<Class, List<Element>> candidate) {
        if(candidate.get(SystemeEndiguement.class) != null  ||
           candidate.get(Digue.class) != null ||
           candidate.get(TronconDigue.class) != null) {
            updateTree();
        }
    }

    @Override
    public void documentChanged(Map<Class, List<Element>> candidate) {
        if(candidate.get(SystemeEndiguement.class) != null  ||
           candidate.get(Digue.class) != null ||
           candidate.get(TronconDigue.class) != null) {
           updateTree();
        }
    }

    @Override
    public void documentDeleted(final Set<String> candidate) {
        if (containsOne(candidate))
            updateTree();
    }

    private class NewDigueMenuItem extends MenuItem {

        public NewDigueMenuItem(TreeItem parent) {
            super("Créer une nouvelle digue",new ImageView(SIRS.ICON_ADD_WHITE));
            this.setOnAction((ActionEvent t) -> {
                final Digue digue = session.getElementCreator().createElement(Digue.class);
                digue.setLibelle("Digue vide");

                if(parent!=null){
                    final SystemeEndiguement se = (SystemeEndiguement) parent.getValue();
                    digue.setSystemeEndiguementId(se.getId());
                }
                session.getRepositoryForClass(Digue.class).add(digue);

            });
        }
    }

    private class NewSystemeMenuItem extends MenuItem {

        public NewSystemeMenuItem(TreeItem parent) {
            super("Créer un nouveau système d'endiguement",new ImageView(SIRS.ICON_ADD_WHITE));
            this.setOnAction((ActionEvent t) -> {
                final SystemeEndiguement systemeEndiguement = session.getElementCreator().createElement(SystemeEndiguement.class);
                systemeEndiguement.setLibelle("Système vide");
                session.getRepositoryForClass(SystemeEndiguement.class).add(systemeEndiguement);
            });
        }
    }

    private class CustomizedTreeCell extends TreeCell {

        private final ContextMenu addMenu;

        public CustomizedTreeCell() {
            addMenu = new ContextMenu();
            setContextMenu(addMenu);
        }

        @Override
        protected void updateItem(Object obj, boolean empty) {
            super.updateItem(obj, empty);

            addMenu.getItems().clear();

            if (obj instanceof TreeItem) {
                obj = ((TreeItem) obj).getValue();
            }

            final boolean isSE = (obj instanceof SystemeEndiguement);
            final boolean isDigue = obj instanceof Digue;
            if (isSE || isDigue) {
                this.setText(new StringBuilder(converter.toString(obj)).append(" (").append(getTreeItem().getChildren().size()).append(")").toString());
                if (isSE) {
                    addMenu.getItems().add(new NewDigueMenuItem(getTreeItem()));
                    setContextMenu(addMenu);
                }
            } else if (obj instanceof TronconDigue) {
                this.setText(new StringBuilder(converter.toString(obj)).toString());
            } else if (obj instanceof Theme) {
                setText(((Theme) obj).getName());
            } else if( obj instanceof String){
                setText((String)obj);
            } else {
                setText(null);
            }
        }
    }
}
