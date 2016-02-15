package fr.sirs.digue;

import static fr.sirs.CorePlugin.initTronconDigue;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractTronconDigueRepository;
import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.index.ElasticSearchEngine;
import fr.sirs.theme.Theme;
import fr.sirs.util.SirsStringConverter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import org.elasticsearch.index.query.QueryBuilders;

public class FXDiguesPane extends FXAbstractTronconTreePane {

    private static final String[] SEARCH_CLASSES = new String[]{
        TronconDigue.class.getCanonicalName(),
        Digue.class.getCanonicalName(),
        SystemeEndiguement.class.getCanonicalName()
    };

    private final SirsStringConverter converter = new SirsStringConverter();

    private final Predicate<TronconDigue> searchedPredicate = (TronconDigue t) -> {
        final String str = currentSearch.get();
        if (str != null && !str.isEmpty()) {
            final ElasticSearchEngine searchEngine = Injector.getElasticSearchEngine();
            HashMap<String, HashSet<String>> foundClasses = searchEngine.searchByClass(QueryBuilders.queryString(str));
            final HashSet resultSet = new HashSet();
            HashSet tmp;
            for (final String className : SEARCH_CLASSES) {
                tmp = foundClasses.get(className);
                if (tmp != null && !tmp.isEmpty()) {
                    resultSet.addAll(tmp);
                }
            }
            return resultSet.contains(t.getDocumentId());
        }
        else return true;
    };

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
        if (selectedItem != null) {
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
            Predicate<TronconDigue> filter = searchedPredicate;
            if(!uiArchived.isSelected()) {
                filter = filter.and(nonArchivedPredicate);
            }

            //creation de l'arbre
            final TreeItem treeRootItem = new TreeItem("root");

            //on recupere tous les elements
            final Iterable<SystemeEndiguement> sds = session.getRepositoryForClass(SystemeEndiguement.class).getAllStreaming();
            final Set<Digue> digues = new HashSet<>(session.getRepositoryForClass(Digue.class).getAll());
            final Set<TronconDigue> troncons = new HashSet<>(((TronconDigueRepository) session.getRepositoryForClass(TronconDigue.class)).getAll());
            final Set<Digue> diguesFound = new HashSet<>();
            final Set<TronconDigue> tronconsFound = new HashSet<>();

            for(final SystemeEndiguement sd : sds){
                final TreeItem sdItem = new TreeItem(sd);
                treeRootItem.getChildren().add(sdItem);
                sdItem.setExpanded(extendeds.contains(sd));

                final List<Digue> digueIds = ((DigueRepository) session.getRepositoryForClass(Digue.class)).getBySystemeEndiguement(sd);
                for(Digue digue : digues){
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
                ncItem.getChildren().add(new TreeItem(tc));
            }

            Optional<TreeItem> toSelect = find(treeRootItem, lastSelected);
            Platform.runLater(() -> {
                uiTree.setRoot(treeRootItem);
                if (toSelect.isPresent()) {
                    uiTree.getSelectionModel().select(toSelect.get());
                }
                uiSearch.setGraphic(searchNone);
            });
        });
    }

    /**
     * Try to find a tree item containing the given element in inpur item and its
     * children. Reecursive, depth-first.
     * @param root Item to search into.
     * @param toFind Element to find an item for.
     * @return The tree item containing input element, or an empty optional otherrwise.
     */
    private static Optional<TreeItem> find(final TreeItem<? extends Element> root, final Element toFind) {
        if (toFind == null)
            return Optional.empty();

        if (toFind.equals(root.getValue())) {
            return Optional.of(root);
        }

        Optional found;
        for (final TreeItem child : root.getChildren()) {
            found = find(child, toFind);
            if (found.isPresent()) {
                return found;
            }
        }

        return Optional.empty();
    }

    private static TreeItem toNode(final Digue digue, final Set<TronconDigue> troncons,
            final Set<TronconDigue> tronconsFound, final Predicate<TronconDigue> filter){
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
    public void documentDeleted(Map<Class, List<Element>> candidate) {
        if(candidate.get(SystemeEndiguement.class) != null  ||
           candidate.get(Digue.class) != null ||
           candidate.get(TronconDigue.class) != null) {
           updateTree();
        }
    }

    private class NewTronconMenuItem extends MenuItem {

        public NewTronconMenuItem(TreeItem parent) {
            super("Créer un nouveau tronçon",new ImageView(SIRS.ICON_ADD_WHITE));
            this.setOnAction((ActionEvent t) -> {
                final TronconDigue troncon = session.getElementCreator().createElement(TronconDigue.class);
                troncon.setLibelle("Tronçon vide");
                final Digue digue = parent==null ? null : (Digue) parent.getValue();
                if(digue!=null){
                    troncon.setDigueId(digue.getId());
                }
                session.getRepositoryForClass(TronconDigue.class).add(troncon);
                initTronconDigue(troncon, session);
            });
        }
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
                if(session.nonGeometryEditionProperty().get()){
                    addMenu.getItems().add(isSE? new NewDigueMenuItem(getTreeItem()) : new NewTronconMenuItem(getTreeItem()));
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
