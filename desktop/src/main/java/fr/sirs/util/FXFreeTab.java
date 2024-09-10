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
package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.ui.Growl;
import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.function.Supplier;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXFreeTab extends Tab implements FXTextAbregeable {

    private static boolean DEFAULT_ABREGEABLE = true;
    private static int DEFAULT_NB_AFFICHABLE = 25;

    private static final String UNBIND = "Détacher";
    private static final String BIND = "Rattacher";
    private static final String CLOSE_ALL = "Fermer tous les onglets";

    public ChangeListener<String> hack;
    /**
     * Last pane this tab has been bound to.
     */
    private WeakReference<TabPane> previous;
    private final MenuItem bindAction;
    private final MenuItem closeAllTabsAction;

    /**
     * We use a supplier to get the content of the tab. It allows us to lazily
     * load it. I.e. we set tab content only first time it's focused.
     */
    private final SimpleObjectProperty<Supplier<Node>> contentSupplier = new SimpleObjectProperty<>();

    private FXFreeTab(String text, boolean abregeable, int nbAffichable) {
        super();
        setAbregeable(abregeable);
        setNbAffichable(nbAffichable);
        setTextAbrege(text);

        closeAllTabsAction = new MenuItem();
        closeAllTabsAction.setText(CLOSE_ALL);
        closeAllTabsAction.setOnAction(event -> Injector.getSession().getFrame().closeAllTabs(event));

        bindAction = new MenuItem();

        setContextMenu(new ContextMenu(bindAction, closeAllTabsAction));
        getContextMenu().setOnShowing(evt -> {
            final TabPane tp = previous == null? null : previous.get();
            if (tp == null || tp.equals(getTabPane())) {
                bindAction.setText(UNBIND);
                bindAction.setOnAction(this::unbind);
                closeAllTabsAction.setVisible(this.isClosable());
            } else {
                bindAction.setText(BIND);
                bindAction.setOnAction(this::bind);
                closeAllTabsAction.setVisible(false);
            }
        });

        contentSupplier.addListener(this::supplierChanged);
        setOnSelectionChanged(this::selectionChanged);

        tabPaneProperty().addListener(this::parentChanged);
    }

    public FXFreeTab(String text, int nbAffichable) {
        this(text, DEFAULT_ABREGEABLE, nbAffichable);
    }

    public FXFreeTab(String text, boolean abregeable) {
        this(text, abregeable, DEFAULT_NB_AFFICHABLE);
    }

    public FXFreeTab(String text) {
        this(text, DEFAULT_ABREGEABLE);
    }

    public FXFreeTab(){
        this(null, DEFAULT_ABREGEABLE);
    }

    private BooleanProperty abregeableProperty;

    @Override
    public final BooleanProperty abregeableProperty(){
        if(abregeableProperty==null){
            abregeableProperty=new SimpleBooleanProperty(this, "abregeable");
        }
        return abregeableProperty;
    }
    @Override
    public final boolean isAbregeable(){return abregeableProperty().get();}
    @Override
    public final void setAbregeable(final boolean abregeable){abregeableProperty().set(abregeable);}

    private IntegerProperty nbAffichableProperty;

    @Override
    public final IntegerProperty nbAffichableProperty(){
        if(nbAffichableProperty==null){
            nbAffichableProperty = new SimpleIntegerProperty(this, "nbAffichable");
        }
        return nbAffichableProperty;
    }
    @Override
    public final int getNbAffichable(){return nbAffichableProperty().get();}
    @Override
    public final void setNbAffichable(final int nbAffichable){nbAffichableProperty().set(nbAffichable);}

    public final void setTextAbrege(final String text){
        if(text!=null
            && isAbregeable()
            && text.length()>getNbAffichable()){
            setText(text.substring(0, getNbAffichable())+"...");
            setTooltip(new Tooltip(text));
        }
        else{
            setText(text);
        }
    }

    private void unbind(final ActionEvent evt) {
        final TabPane tabPane = this.getTabPane();

        final Stage stage = new Stage(StageStyle.DECORATED);
        stage.initModality(Modality.NONE);
        stage.initOwner(null);
        stage.getIcons().add(SIRS.ICON);
        stage.titleProperty().bind(textProperty());

        tabPane.getTabs().remove(this);

        final TabPane newPane = new TabPane(this);
        newPane.getStylesheets().add(SIRS.CSS_PATH);
        stage.setScene(new Scene(newPane));
        stage.sizeToScene();
        stage.show();
    }

    private void bind(final ActionEvent evt) {
        final TabPane oldPane = previous == null ? null : previous.get();
        if (oldPane == null) {
            new Growl(Growl.Type.WARNING, "Le panneau d'origine n'existe plus. Impossible de raccrocher l'onglet.").showAndFade();
        } else {
            this.getTabPane().getScene().getWindow().hide();
            this.getTabPane().getTabs().remove(FXFreeTab.this);
            oldPane.getTabs().add(FXFreeTab.this);
        }
    }

    private void supplierChanged(final ObservableValue<? extends Supplier<Node>> obs, final Supplier<Node> oldSupplier, final Supplier<Node> newSupplier) {
        setContent((Node)null);
        supplyContent();
    }

    private void selectionChanged(final Event evt) {
        supplyContent();
    }

    private void supplyContent() {
        if (isSelected()) {
            Node content = getContent();
            if (content == null) {
                Supplier<Node> supplier = contentSupplier.get();
                if (supplier != null) {
                    content = supplier.get();
                    setContent(content);
                }
            } else {
                // When focus on a tab containing a PojoTable, refresh the uiTable of the PojoTable to be up-to-date in case
                // some Element have been modified outside the pojoTable.
                if (PojoTable.class.isAssignableFrom(content.getClass())) {
                    final PojoTable pojoTable = (PojoTable) content;
                    (pojoTable).updateTableItems();
                } else if (BorderPane.class.isAssignableFrom(content.getClass())) {
                    int i = 0;
                    Node centre = ((BorderPane) content).getCenter();
                    // Arbitrary value 7, to avoid infinity loop.
                    while (centre != null && i < 7) {
                        if (TabPane.class.isAssignableFrom(centre.getClass())) {
                            // This happens when the Pane is a tabPane : FXOuvrageAssocieAmenagementHydrauliquePane for example.
                            final Optional<Tab> first = ((TabPane) centre).getTabs().stream().filter(t -> t.isSelected()).findFirst();
                            if (!first.isPresent()) break;
                            Node content2 = first.get().getContent();
                            while (content2 != null) {
                                content2 = checkNodeUpdateIfPojoOrAndGetNext(content2);
                            }
                            break;
                        } else {
                            centre = checkNodeUpdateIfPojoOrAndGetNext(centre);
                            i++;
                        }
                    }

                }
            }
        }
    }

    private Node checkNodeUpdateIfPojoOrAndGetNext(Node node) {
        if (!(BorderPane.class.isAssignableFrom(node.getClass()))) return null;
        if (node instanceof PojoTable) {
            ((PojoTable) node).updateTableItems();
            return null;
        }
        return ((BorderPane) node).getCenter();
    }

    /**
     * Affect a supplier to this tab so when it will be selected, the supplier is
     * called to fill tab's content.
     * @param contentSupplier Supplier giving the node to set as this tab's content.
     */
    public void setContent(final Supplier<Node> contentSupplier) {
        this.contentSupplier.set(contentSupplier);
    }

    public void parentChanged(final ObservableValue<? extends TabPane> obs, final TabPane oldValue, final TabPane newValue) {
        if (previous == null && oldValue != null) {
            previous = new WeakReference<>(oldValue);
        }
    }
}
