package fr.sirs.core.model.report;

import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCoreRuntimeException;
import fr.sirs.core.model.AbstractPhoto;
import fr.sirs.core.model.AvecPhotos;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.util.odt.ODTUtils;
import fr.sirs.util.property.Reference;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.odftoolkit.simple.TextDocument;
import org.opengis.feature.Feature;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Text;

/**
 * Detailed element printing.
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FicheSectionRapport extends AbstractSectionRapport {

    private final SimpleIntegerProperty nbPhotos = new SimpleIntegerProperty(0);

    /**
     *
     * @return The number of photos to print for a given element.
     */
    public IntegerProperty nbPhotosProperty() {
        return nbPhotos;
    }

    public int getNbPhotos() {
        return nbPhotos.get();
    }

    public void setNbPhotos(int newValue) {
        nbPhotos.set(newValue);
    }

    private final SimpleStringProperty modeleElementId = new SimpleStringProperty();

    /**
     *
     * @return ODT template used to print each element given in this section.
     */
    @Reference(ref=ModeleElement.class)
    public StringProperty ModeleElementIdProperty() {
        return modeleElementId;
    }

    public String getModeleElementId() {
        return modeleElementId.get();
    }

    public void setModeleElementId(final String newValue) {
        modeleElementId.set(newValue);
    }

    @Override
    public Element copy() {
        final FicheSectionRapport rapport = ElementCreator.createAnonymValidElement(FicheSectionRapport.class);
        super.copy(rapport);

        rapport.setNbPhotos(getNbPhotos());
        rapport.setModeleElementId(getModeleElementId());

        return rapport;
    }

    @Override
    public boolean removeChild(Element toRemove) {
        return false;
    }

    @Override
    public boolean addChild(Element toAdd) {
        return false;
    }

    @Override
    public Element getChildById(String toSearch) {
        if (toSearch != null && toSearch.equals(getId()))
            return this;
        return null;
    }

    @Override
    public void printSection(final PrintContext ctx) throws Exception {
        if (ctx.elements == null && ctx.filterValues == null) {
            return;
        }

        // Find ODT template
        if (modeleElementId.get() == null)
            throw new IllegalStateException("No model set for printing !");

        final SessionCore session = InjectorCore.getBean(SessionCore.class);
        ModeleElement model = session.getRepositoryForClass(ModeleElement.class).get(modeleElementId.get());
        byte[] odt = model.getOdt();
        if (odt == null || odt.length <= 0) {
            throw new IllegalStateException("No ODT template available.");
        }

        final Iterator iterator;
        if (ctx.elements != null) {
            // Print only elements managed by underlying model.
            final String targetClass = model.getTargetClass();
            if (targetClass != null && !targetClass.isEmpty()) {
                final Class tmpClass = Thread.currentThread().getContextClassLoader().loadClass(targetClass);
                iterator = ctx.elements.filter(input -> tmpClass.isAssignableFrom(input.getClass())).iterator();
            } else {
                iterator = ctx.elements.iterator();
            }
        } else {
            // No elements available. Print filter values.
            iterator = ctx.filterValues.iterator();
        }

        /**
         * If we've got elements to print, section template is read, then we fill
         * it for each element, and append it to context target.
         */
        if (iterator.hasNext()) {
            final Object first = iterator.next();

            try (final ByteArrayInputStream stream = new ByteArrayInputStream(odt);
                    final TextDocument doc = TextDocument.loadDocument(stream)) {

                final boolean isElement = first instanceof Element;
                if (first instanceof Element) {
                    ODTUtils.fillTemplate(doc, (Element)first);
                } else if (first instanceof Feature) {
                    ODTUtils.fillTemplate(doc, (Feature)first);
                } else throw new IllegalArgumentException("Unknown object given for printing !");

                // Forced to do it to avoid variable erasing at concatenation
                final Map<String, List<Text>> replaced = ODTUtils.replaceUserVariablesWithText(doc);
                final int nbPhotosToPrint = nbPhotos.get();

                ODTUtils.append(ctx.target, doc);
                
                if (isElement) {
                    printPhotos(ctx.target, (Element)first, nbPhotosToPrint);
                }

                // For next elements, we replace directly text attributes we've put instead of variables, to avoid reloading original template.
                iterator.forEachRemaining(next -> {
                    try {
                        if (isElement) {
                            ODTUtils.replaceTextContent((Element)next, (Map)replaced);
                        } else {
                            ODTUtils.replaceTextContent((Feature)next, (Map)replaced);
                        }

                        ODTUtils.append(ctx.target, doc);
                        if (isElement) {
                            printPhotos(ctx.target, (Element)next, nbPhotosToPrint);
                        }
                    } catch (RuntimeException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        throw new SirsCoreRuntimeException(ex);
                    }
                });
            }
        }
    }

    private static void printPhotos(final TextDocument holder, final Element source, int nbPhotosToPrint) {
        if (nbPhotosToPrint > 0 && source instanceof AvecPhotos) {
            final List<? extends AbstractPhoto> photos = ((AvecPhotos<? extends AbstractPhoto>) source).getPhotos();
            photos.sort(new PhotoComparator());

            AbstractPhoto photo;
            for (int i = 0; i < nbPhotosToPrint && i < photos.size(); i++) {
                photo = photos.get(i);
                try {
                    ODTUtils.appendImage(holder, null, photo, false);
                } catch (IllegalArgumentException e) {
                    holder.addParagraph("Impossible de retrouver l'image ".concat(photo.getChemin()));
                }
            }
        }
    }

    private static class PhotoComparator implements Comparator<AbstractPhoto> {

        @Override
        public int compare(AbstractPhoto o1, AbstractPhoto o2) {
            if (o1 == null)
                return 1;
            else if (o2 == null)
                return -1;

            LocalDate date1 = o1.getDate();
            LocalDate date2 = o2.getDate();

            if (date1 == null)
                return 1;
            else if (date2 == null)
                return -1;
            // We want early dates first
            else return -date1.compareTo(date2);
        }

    }
}
