
package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.SirsCore;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Identifiable;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import java.util.HashSet;
import java.util.WeakHashMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.DocumentNotFoundException;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.util.FXTableCell;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Alexis Manin (Geomatys)
 * @param <S>
 */
public class ReferenceTableCell<S> extends FXTableCell<S, String> {

    public static final Image ICON_LINK = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_LINK,16,FontAwesomeIcons.DEFAULT_COLOR),null);

    private static final WeakHashMap<Object, String> CACHED_VALUES = new WeakHashMap<>();

    private final Class refClass;
    private final ComboBox editor = new ComboBox();

    public ReferenceTableCell(final Class referenceClass) {
        ArgumentChecks.ensureNonNull("Reference class", referenceClass);
        refClass = referenceClass;
        setAlignment(Pos.CENTER);
        setContentDisplay(ContentDisplay.LEFT);
        setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    public void terminateEdit() {
        Object newValue = editor.getValue();
        if (newValue == null) {
            commitEdit(null);
        } if (newValue instanceof Preview) {
            commitEdit(((Preview)newValue).getElementId());
        } else if (newValue instanceof Identifiable) {
            commitEdit(((Identifiable)newValue).getId());
        } else {
            cancelEdit();
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        updateItem(getItem(), false);
    }

    @Override
    public void startEdit() {
        // TODO : make search asynchronous ?
        final ObservableList items;
        final Previews previews = Injector.getSession().getPreviews();
        // First, we'll search for a set of possible values.
        if (SystemeReperageBorne.class.isAssignableFrom(refClass) || BorneDigue.class.isAssignableFrom(refClass)) {
            items = findBornes();
        } else {
            items = FXCollections.observableList(previews.getByClass(refClass));
        }

        // Analyze current item to determine default selection
        final String elementId = getItem();
        Object selected = null;
        if (elementId != null && !elementId.isEmpty()) {
            try {
                if (SystemeReperageBorne.class.isAssignableFrom(refClass) || BorneDigue.class.isAssignableFrom(refClass)) {
                    selected = Injector.getSession().getElement(elementId).orElse(null);
                } else {
                    selected = previews.get(elementId);
                }
            } catch (DocumentNotFoundException e) {
                SirsCore.LOGGER.fine("No document found for id " + elementId);
            }
        }

        if (items != null && !items.isEmpty()) {
            SIRS.initCombo(editor, items, selected);
            super.startEdit();
            setText(null);
            setGraphic(editor);
            editor.requestFocus();
        } else {
            cancelEdit();
        }
    }

    private ObservableList<SystemeReperageBorne> findSRBornes() {
        SystemeReperage sr = findSystemeReperage();
        if (sr != null) {
            return sr.systemeReperageBornes;
        } else {
            return FXCollections.emptyObservableList();
        }
    }

    private ObservableList<BorneDigue> findBornes() {
        SystemeReperage sr = findSystemeReperage();
        AbstractSIRSRepository<BorneDigue> repo = Injector.getSession().getRepositoryForClass(BorneDigue.class);
        if (sr == null) {
            return FXCollections.observableList(repo.getAll());
        } else {
            final HashSet<String> borneIds = new HashSet<>(sr.systemeReperageBornes.size());
            sr.systemeReperageBornes.stream().forEach(srb -> borneIds.add(srb.getBorneId()));
            return FXCollections.observableList(repo.get(borneIds.toArray(new String[0])));
        }
    }

    private SystemeReperage findSystemeReperage() {
        SystemeReperage toUse = null;
        String item = getItem();
        final Element element = Injector.getSession().getElement(item).orElse(null);
        if (element instanceof SystemeReperageBorne) {
            Element parent = ((SystemeReperageBorne)element).getParent();
            if (parent instanceof SystemeReperage) {
                toUse = (SystemeReperage) parent;
            }
        }

        // Cannot determine SR from cell value. We'll try from row value
        if (toUse == null) {
            if (getTableRow() != null && (getTableRow().getItem() instanceof Positionable)) {
                final Positionable tmpPos = (Positionable) getTableRow().getItem();
                final String srid = tmpPos.getSystemeRepId();
                if (srid != null && !srid.isEmpty()) {
                    try {
                        toUse = Injector.getSession().getRepositoryForClass(SystemeReperage.class).get(srid);
                    } catch (DocumentNotFoundException e) {
                        SirsCore.LOGGER.fine("No SystemeReperage for id "+srid);
                    }
                }
            }
        }
        return toUse;
    }

    @Override
    protected void updateItem(final String item, final boolean empty) {
        String text;
        if (empty || item == null || item.isEmpty()) {
            text = null;
        } else {
            text = CACHED_VALUES.get(item);
            // L'entrée nest pas dans le cache, on va chercher l'info en base.
            if (text == null) {
                // On essaye de récupérer le preview label, car l'objet en entrée doit être un ID.
                final Session session = Injector.getSession();
                final Previews previews = session.getPreviews();
                final Preview tmpPreview = previews.get((String) item);
                if (tmpPreview != null) {
                    text = tmpPreview.getLibelle();
                    CACHED_VALUES.put(item, text);
                }
            }
        }

        super.updateItem(item, empty);
        setGraphic(text == null? null : new ImageView(ICON_LINK));
        setText(text);
    }

}
