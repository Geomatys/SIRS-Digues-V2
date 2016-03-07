
package fr.sirs.plugin.lit.ui;

import static fr.sirs.CorePlugin.initTronconDigue;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.TronconLitRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Lit;
import fr.sirs.core.model.TronconLit;
import fr.sirs.digue.FXAbstractTronconTreePane;
import fr.sirs.index.ElasticSearchEngine;
import fr.sirs.theme.Theme;
import fr.sirs.util.SirsStringConverter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

/**
 *
 * @author guilhem
 */
public class SuiviLitPane extends FXAbstractTronconTreePane {

    private static final String[] SEARCH_CLASSES = new String[]{
        TronconLit.class.getCanonicalName(),
        Lit.class.getCanonicalName()
    };

    private final SirsStringConverter converter = new SirsStringConverter();

    private final Predicate<TronconLit> searchedPredicate = (TronconLit t) -> {
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

    public SuiviLitPane() {
        super("Lits");
        uiTree.setCellFactory((param) -> new CustomizedTreeCell());
        uiAdd.getItems().add(new NewLitMenuItem(null));
        updateTree();
    }

    @Override
    public void deleteSelection(ActionEvent event) {
        Object obj = uiTree.getSelectionModel().getSelectedItem();
        if(obj instanceof TreeItem){
            obj = ((TreeItem)obj).getValue();
        }

        if(obj instanceof Lit){
            final Lit lit = (Lit) obj;
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "La suppression du lit " + lit.getLibelle() + " ne supprimera pas les tronçons qui le composent, "
                   +"ceux ci seront déplacés dans le groupe 'Non classés. Confirmer la suppression ?",
                    ButtonType.YES, ButtonType.NO);
            alert.setResizable(true);
            final ButtonType res = alert.showAndWait().get();
            if (res == ButtonType.YES) {
                //on enleve la reference au lit dans les troncons
                final List<TronconLit> troncons = ((TronconLitRepository)session.getRepositoryForClass(TronconLit.class)).getByLit(lit);
                for (final TronconLit td : troncons) {
                    td.setLitId(null);
                    session.getRepositoryForClass(TronconLit.class).update(td);
                }
                //on supprime le lit
                session.getRepositoryForClass(Lit.class).remove(lit);
            }
        }else if(obj instanceof TronconLit){
            final TronconLit td = (TronconLit) obj;
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Confirmer la suppression du tronçon de lit" + td.getLibelle() + " ?",
                    ButtonType.YES, ButtonType.NO);
            alert.setResizable(true);
            final ButtonType res = alert.showAndWait().get();
            if (res == ButtonType.YES) {
                session.getRepositoryForClass(TronconLit.class).remove(td);
            }
        }
    }

    @Override
    public final Task updateTree() {

        return Injector.getSession().getTaskManager().submit("Mise à jour de l'arbre des lits", () -> {
            Platform.runLater(() -> uiSearch.setGraphic(searchRunning));

            //on stoque les noeuds ouverts
            final Set extendeds = new HashSet();
            searchExtended(uiTree.getRoot(), extendeds);

            //creation des filtres
            Predicate<TronconLit> filter = searchedPredicate;
            if(!uiArchived.isSelected()) {
                filter = filter.and(nonArchivedPredicate);
            }

            //creation de l'arbre
            final TreeItem treeRootItem = new TreeItem("root");

            //on recupere tous les elements
            final Iterable<Lit> lits = session.getRepositoryForClass(Lit.class).getAllStreaming();
            final Set<TronconLit> tronconLits = new HashSet<>(session.getRepositoryForClass(TronconLit.class).getAll());
            final Set<TronconLit> tronconFound = new HashSet<>();

            for (Lit lit : lits) {
                final TreeItem litItem = new TreeItem(lit);
                treeRootItem.getChildren().add(litItem);
                litItem.setExpanded(extendeds.contains(lit));

                final List<TronconLit> tronconIds = ((TronconLitRepository) session.getRepositoryForClass(TronconLit.class)).getByLit(lit);

                for(TronconLit trl : tronconLits){
                    if(!tronconIds.contains(trl)) continue;
                    tronconFound.add(trl);

                    if (filter == null || filter.test(trl)) {
                        final TreeItem tronconItem = new TreeItem(trl);
                        litItem.getChildren().add(tronconItem);
                    }
                }
            }

            //on place toute les tronçons non trouvé dans un group a part
            tronconLits.removeAll(tronconFound);
            final TreeItem ncItem = new TreeItem("Non classés");
            ncItem.setExpanded(extendeds.contains(ncItem.getValue()));
            treeRootItem.getChildren().add(ncItem);

            for(final TronconLit trl : tronconLits){
                if (filter == null || filter.test(trl)) {
                    final TreeItem tronconItem = new TreeItem(trl);
                    ncItem.getChildren().add(tronconItem);
                }
            }

            Platform.runLater(() -> {
                uiTree.setRoot(treeRootItem);
                uiSearch.setGraphic(searchNone);
            });
        });
    }

    @Override
    public void documentCreated(Map<Class, List<Element>> candidate) {
        if(candidate.get(Lit.class) != null  ||
           candidate.get(TronconLit.class) != null) {
            updateTree();
        }
    }

    @Override
    public void documentChanged(Map<Class, List<Element>> candidate) {
        if(candidate.get(Lit.class) != null  ||
           candidate.get(TronconLit.class) != null) {
           updateTree();
        }
    }

    @Override
    public void documentDeleted(Set<String> candidate) {
        if(containsOne(candidate))
           updateTree();
    }

    private class NewTronconMenuItem extends MenuItem {

        public NewTronconMenuItem(TreeItem parent) {
            super("Créer un nouveau tronçon de lit",new ImageView(SIRS.ICON_ADD_WHITE));
            this.setOnAction((ActionEvent t) -> {
                final TronconLit troncon = session.getElementCreator().createElement(TronconLit.class);
                troncon.setLibelle("Tronçon de lit vide");
                final Lit lit = parent==null ? null : (Lit) parent.getValue();
                if (lit != null) {
                    troncon.setLitId(lit.getId());
                }
                session.getRepositoryForClass(TronconLit.class).add(troncon);
                initTronconDigue(troncon, session);
            });
        }
    }

    private class NewLitMenuItem extends MenuItem {

        public NewLitMenuItem(TreeItem parent) {
            super("Créer un nouveau lit",new ImageView(SIRS.ICON_ADD_WHITE));
            this.setOnAction((ActionEvent t) -> {
                final Lit lit = session.getElementCreator().createElement(Lit.class);
                lit.setLibelle("Lit vide");
                session.getRepositoryForClass(Lit.class).add(lit);
            });
        }
    }

    private class CustomizedTreeCell extends TreeCell {

        private final ContextMenu addMenu;

        public CustomizedTreeCell() {
            addMenu = new ContextMenu();
        }

        @Override
        protected void updateItem(Object obj, boolean empty) {
            super.updateItem(obj, empty);
            setContextMenu(null);

            if (obj instanceof TreeItem) {
                obj = ((TreeItem) obj).getValue();
            }

            if (obj instanceof Lit) {
                this.setText(new StringBuilder(converter.toString(obj)).append(" (").append(getTreeItem().getChildren().size()).append(")").toString());
                addMenu.getItems().clear();
                if(session.nonGeometryEditionProperty().get()){
                    addMenu.getItems().add(new NewTronconMenuItem(getTreeItem()));
                    setContextMenu(addMenu);
                }
            } else if (obj instanceof TronconLit) {
                this.setText(new StringBuilder(converter.toString(obj)).toString());
                setContextMenu(null);
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
