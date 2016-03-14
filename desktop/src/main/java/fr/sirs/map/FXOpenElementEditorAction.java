package fr.sirs.map;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.model.Element;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.display.VisitFilter;
import org.geotoolkit.display2d.canvas.AbstractGraphicVisitor;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.gui.javafx.render2d.AbstractNavigationHandler;
import org.geotoolkit.gui.javafx.render2d.FXCanvasHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXMapAction;
import org.geotoolkit.gui.javafx.render2d.FXPanMouseListen;
import org.geotoolkit.gui.javafx.render2d.navigation.AbstractMouseHandler;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class FXOpenElementEditorAction extends FXMapAction {

    /**
     * Half a side of the square to search in when user click on the map.
     */
    private static final double POINT_RADIUS = 1;

    public FXOpenElementEditorAction(final FXMap map) {
        this(map, "informations sur l'élément", "Ouvre la fiche du tronçon/structure.");
    }
    
    public FXOpenElementEditorAction(final FXMap map, final String shortText, final String longText) {
        super(map, shortText, longText, SIRS.ICON_INFO_BLACK_16);

        map.getHandlerProperty().addListener(new ChangeListener<FXCanvasHandler>() {
            @Override
            public void changed(ObservableValue<? extends FXCanvasHandler> observable, FXCanvasHandler oldValue, FXCanvasHandler newValue) {
                selectedProperty().set(newValue instanceof OpenElementEditorHandler);
            }
        });
    }

    @Override
    public void accept(ActionEvent event) {
        if (map != null) {
            map.setHandler(new OpenElementEditorHandler(map));
        }
    }

    private static class OpenElementEditorHandler extends AbstractNavigationHandler {

        private final AbstractMouseHandler mouseListener;

        public OpenElementEditorHandler(FXMap map) {
            super();
            mouseListener = new InfoMouseListener();
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public void install(final FXMap map) {
            super.install(map);
            map.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseListener);
            map.addEventHandler(ScrollEvent.SCROLL, mouseListener);
            map.setCursor(Cursor.DEFAULT);
            SIRS.LOGGER.log(Level.FINE, "Information handler installed.");
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public boolean uninstall(final FXMap component) {
            super.uninstall(component);
            component.removeEventHandler(MouseEvent.MOUSE_CLICKED, mouseListener);
            component.removeEventHandler(ScrollEvent.SCROLL, mouseListener);
            SIRS.LOGGER.log(Level.FINE, "Information handler UNinstalled.");
            return true;
        }


        private class InfoMouseListener extends FXPanMouseListen {

            final ContextMenu choice = new ContextMenu();

            public InfoMouseListener() {
                super(OpenElementEditorHandler.this);
                choice.setAutoHide(true);
            }

            @Override
            public void mouseClicked(MouseEvent me) {
                SIRS.LOGGER.log(Level.FINE, "Mouse click detected.");
                choice.getItems().clear();
                choice.hide();
                // Visitor which will perform action on selected elements.
                final AbstractGraphicVisitor visitor = new AbstractGraphicVisitor() {

                    final HashSet<Element> foundElements = new HashSet<>();

                    @Override
                    public void visit(ProjectedFeature feature, RenderingContext2D context, SearchAreaJ2D area) {
                        Object userData = feature.getCandidate().getUserData().get(BeanFeature.KEY_BEAN);
                        if (userData instanceof Element) {
                            foundElements.add((Element) userData);
                        }
                    }

                    @Override
                    public void visit(ProjectedCoverage coverage, RenderingContext2D context, SearchAreaJ2D area) {
                        SIRS.LOGGER.log(Level.FINE, "Coverage elements are not managed yet.");
                    }

                    @Override
                    public void endVisit() {
                        SIRS.LOGGER.log(Level.FINE, "End of visit.");
                        super.endVisit();
                        if (foundElements.size() == 1) {
                            displayElement(foundElements.iterator().next());
                        } else if (foundElements.size() > 1) {
                            final Session session = Injector.getSession();
                            final Iterator<Element> it = foundElements.iterator();
                            ObservableList<MenuItem> items = choice.getItems();
                            while (it.hasNext()) {
                                final Element current = it.next();
                                final MenuItem item = new MenuItem(session.generateElementTitle(current));
                                item.setOnAction((ActionEvent ae) -> displayElement(current));
                                items.add(item);
                            }
                            choice.show(map, me.getScreenX(), me.getScreenY());
                        }
                    }
                };

                Rectangle2D.Double searchArea = new Rectangle2D.Double(
                        getMouseX(me) - POINT_RADIUS, getMouseY(me) - POINT_RADIUS, POINT_RADIUS * 2, POINT_RADIUS * 2);
                map.getCanvas().getGraphicsIn(searchArea, visitor, VisitFilter.INTERSECTS);
            }
        }
    }

    private static void displayElement(Element e) {
        Injector.getSession().showEditionTab(e);
    }
}
