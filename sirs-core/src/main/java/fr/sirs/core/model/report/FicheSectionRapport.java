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
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.odftoolkit.simple.TextDocument;

/**
 * Detailed element printing.
 *
 * @author Alexis Manin (Geomatys)
 */
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
        if (modeleElementId.get() == null)
            throw new IllegalStateException("No model set for printing !");

        final SessionCore session = InjectorCore.getBean(SessionCore.class);
        ModeleElement model = session.getRepositoryForClass(ModeleElement.class).get(modeleElementId.get());
        byte[] odt = model.getOdt();
        if (odt == null || odt.length <= 0) {
            throw new IllegalStateException("No ODT template available.");
        }

        final PhotoComparator comparator = new PhotoComparator();
        try (final ByteArrayInputStream stream = new ByteArrayInputStream(odt);
                final TextDocument doc = TextDocument.loadDocument(stream)) {
            ctx.elements.forEach(next -> {
                // Fill section template
                try {
                    ODTUtils.fillTemplate(doc, next);
                    ODTUtils.append(ctx.target, doc);
                } catch (RuntimeException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new SirsCoreRuntimeException(ex);
                }

                // Print photographs
                final int nbPhotosToPrint = nbPhotos.get();
                if (nbPhotosToPrint > 0 && next instanceof AvecPhotos) {
                    List<? extends AbstractPhoto> photos = ((AvecPhotos<? extends AbstractPhoto>) next).getPhotos();
                    photos.sort(comparator);

                    for (int i = 0; i < nbPhotosToPrint && i < photos.size(); i++) {
                        ODTUtils.appendImage(ctx.target, null, photos.get(i), false);
                    }
                }
            });
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
