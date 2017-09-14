package fr.sirs.plugins.synchro.common;

import fr.sirs.Session;
import fr.sirs.core.component.AbstractPositionableRepository;
import fr.sirs.core.model.AbstractPhoto;
import fr.sirs.core.model.AvecBornesTemporelles;
import fr.sirs.core.model.AvecPhotos;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Observation;
import fr.sirs.plugins.synchro.DocumentExportPane;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.ObjectProperty;

/**
 * Note : No computing is done here, we simply wrap operators in a stream. It
 * implies that fetching will be done when browsing objects. So, you should
 * avoid using this component inside rendering thread. The advantage of this
 * approach is that nothing will be loaded until you actually need it. The
 * second advantage is that resources are loaded as you browse it, and release
 * as you release them.
 *
 * @author Alexis Manin (Geomatys)
 */
public class PhotoFinder implements Supplier<Stream<AbstractPhoto>> {

    final Session session;
    final Collection<String> tronconIds;
    final LocalDate dateFilter;
    final int limitByDoc;

    public PhotoFinder(Session session, Collection<String> tronconIds, LocalDate dateFilter, int limitByDoc) {
        this.session = session;
        this.tronconIds = tronconIds;
        this.dateFilter = dateFilter;
        this.limitByDoc = limitByDoc;
    }

    @Override
    public Stream<AbstractPhoto> get() {
        if (tronconIds.isEmpty())
            return Stream.empty();
        return getPhotoContainers()
                .flatMap(this::filter);
    }

    private Stream<AvecPhotos> getPhotoContainers() {
        return Stream.of(session)
                .flatMap(source -> {
                    return Stream.concat(
                            getDisorderContainers(source),
                            getNativeContainers(source)
                    );
                });
    }

    private Stream<AvecPhotos> getNativeContainers(final Session source) {
        return source.getRepositoriesForClass(AvecPhotos.class).stream()
                .filter(AbstractPositionableRepository.class::isInstance)
                .map(AbstractPositionableRepository.class::cast)
                .flatMap(this::getForTroncons);
    }

    private Stream<AvecPhotos> getDisorderContainers(final Session source) {
        return source.getRepositoriesForClass(Desordre.class).stream()
                .filter(AbstractPositionableRepository.class::isInstance)
                .map(AbstractPositionableRepository.class::cast)
                .<Desordre>flatMap(this::getForTroncons)
                .map(DesordreWrapper::new);
    }

    private Stream getForTroncons(final AbstractPositionableRepository repo) {
        return tronconIds.stream()
                .flatMap(id -> repo.getByLinearId(id).stream());
    }

    private Stream<AbstractPhoto> filter(final AvecPhotos<AbstractPhoto> container) {
        Stream<AvecPhotos<AbstractPhoto>> source = Stream.of(container);
        if (dateFilter != null) {
            source = source
                    .filter(AvecBornesTemporelles.class::isInstance)
                    .filter(obj -> DocumentUtilities.intersectsDate((AvecBornesTemporelles) obj, dateFilter));
        }

        return source
                .flatMap(ap -> ap.getPhotos().stream())
                .filter(DocumentUtilities::isFileAvailable)
                .sorted(new DocumentExportPane.PhotoDateComparator())
                .limit(limitByDoc);
    }

    /**
     * Compare observations by date, descending order (most recent to oldest).
     */
    private static int compare(final Observation o1, final Observation o2) {
        final LocalDate date1 = o1.getDate();
        final LocalDate date2 = o2.getDate();

        if (date1 == date2)
            return 0;
        if (date1 == null)
            return 1;
        else if (date2 == null)
            return -1;

        return -date1.compareTo(date2);
    }

    private class DesordreWrapper implements AvecPhotos<AbstractPhoto>, AvecBornesTemporelles {

        public final Desordre source;

        public DesordreWrapper(Desordre source) {
            this.source = source;
        }

        @Override
        public List<AbstractPhoto> getPhotos() {
            return source.observations.stream()
                    .sorted(PhotoFinder::compare)
                    .flatMap(o -> o.getPhotos().stream())
                    .collect(Collectors.toList());
        }

        @Override
        public void setPhotos(List<AbstractPhoto> photos) {
            throw new UnsupportedOperationException("Read-only utility.");
        }

        @Override
        public ObjectProperty<LocalDate> date_debutProperty() {
            return source.date_debutProperty();
        }

        @Override
        public LocalDate getDate_debut() {
            return source.getDate_debut();
        }

        @Override
        public void setDate_debut(LocalDate date_debut) {
            throw new UnsupportedOperationException("Read-only utility.");
        }

        @Override
        public ObjectProperty<LocalDate> date_finProperty() {
            return source.date_finProperty();
        }

        @Override
        public LocalDate getDate_fin() {
            return source.getDate_fin();
        }

        @Override
        public void setDate_fin(LocalDate date_fin) {
            throw new UnsupportedOperationException("Read-only utility.");
        }
    }
}
