package fr.sirs.plugins.synchro;

import fr.sirs.SIRS;
import fr.sirs.core.SessionCore;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.AbstractPositionDocument;
import fr.sirs.core.model.AbstractPositionDocumentAssociable;
import fr.sirs.core.model.AvecBornesTemporelles;
import fr.sirs.core.model.LevePositionProfilTravers;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.SIRSFileReference;
import fr.sirs.core.model.TronconDigue;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.concurrent.Task;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class DocumentFinder extends Task<List<SIRSFileReference>> {

    final Class<?> targetType;
    final Collection<String> tronconIds;
    final LocalDate dateFilter;

    final SessionCore session;

    public DocumentFinder(Class<?> targetType, Collection<String> tronconIds, LocalDate dateFilter, final SessionCore session) {
        this.targetType = targetType;
        this.tronconIds = tronconIds;
        this.dateFilter = dateFilter;
        this.session = session;
    }

    @Override
    protected List<SIRSFileReference> call() throws Exception {
        updateTitle("Recherche de documents");
        updateMessage("Analyse des tronçons");
        final List<TronconDigue> selectedTroncons = session
                .getRepositoryForClass(TronconDigue.class)
                .get(tronconIds);

        Stream<TronconDigue> tdStream = selectedTroncons.stream();
        if (dateFilter != null) {
            tdStream = tdStream.filter(this::intersectsDate);
        }

        updateMessage("Fouille de la base de données");
        Stream<Preview> docPreviews = tdStream
                .flatMap(this::getDocumentPreviews);

        // Ignore type filtering if it's root class : all selected previews would match it.
        if (targetType != null && !targetType.equals(SIRSFileReference.class)) {
            final String wantedClass = targetType.getCanonicalName();
            docPreviews = docPreviews.filter(p -> wantedClass.equals(p.getElementClass()));
        }

        final Map<String, Set<String>> docsByType = docPreviews
                .collect(
                        Collectors.groupingBy(
                                Preview::getElementClass,
                                Collectors.mapping(Preview::getElementId, Collectors.toSet())
                        )
                );

        updateMessage("Collecte des documents");
        Stream<?> documents = docsByType.entrySet().stream()
                .flatMap(this::getDocuments);
        if (dateFilter != null) {
            documents = ((Stream<AvecBornesTemporelles>)documents
                    .filter(AvecBornesTemporelles.class::isInstance))
                    .filter(this::intersectsDate);
        }

        return ((Stream<SIRSFileReference>)documents)
                .filter(DocumentFinder::isFileAvailable)
                .collect(Collectors.toList());
    }

    public static boolean isFileAvailable(final SIRSFileReference fileRef) {
        final String chemin = fileRef.getChemin();
        return chemin != null &&
                !chemin.isEmpty() &&
                Files.isRegularFile(SIRS.getDocumentAbsolutePath(fileRef));
    }

    private boolean intersectsDate(final AvecBornesTemporelles target) {
        final LocalDate start = target.getDate_debut();
        final LocalDate end = target.getDate_fin();
        if (start == null && end == null)
            return false;
        else if (start == null) {
            return end.isEqual(dateFilter) || end.isAfter(dateFilter);
        } else if (end == null) {
            return start.isEqual(dateFilter) || start.isBefore(dateFilter);
        } else {
            return (start.isEqual(dateFilter) || start.isBefore(dateFilter)) &&
                    (end.isEqual(dateFilter) || end.isAfter(dateFilter));
        }
    }

    /**
     * Analyse all {@link AbstractPositionDocument} placed on input {@link TronconDigue}
     * to retrieve associated {@link SIRSFileReference}. The aim is to perform a minimum amount
     * of queries to avoid IO/CPU overhead.
     *
     * There's 3 cases here :
     * - {@link AbstractPositionDocumentAssociable} : document position contains
     * an id of another document which is the wanted {@link SIRSFileReference}
     * - {@link AbstractPositionDocument} which are {@link SIRSFileReference} themselves
     * (Ex: {@link ProfilLong}.
     * - {@link PositionProfilTravers}, which contains a link to multiple {@link LevePositionProfilTravers},
     * each of them being associatded to one wanted {@link SIRSFileReference}
     *
     * @param troncon The linear we have to find documents for.
     * @return a preview of each document found.
     */
    private Stream<Preview> getDocumentPreviews(final TronconDigue troncon) {
        final HashSet<String> docIds = new HashSet<>();
        // Store ids found for leves, to perform a single query to retrieve all.
        final HashSet<String> levePositionIds = new HashSet<>();
        final List<AbstractPositionDocument> docPositions = TronconUtils.getPositionDocumentList(troncon.getId());
        for (final AbstractPositionDocument docPosition : docPositions) {
            if (docPosition instanceof SIRSFileReference) {
                docIds.add(docPosition.getId());
            } else if (docPosition instanceof PositionProfilTravers) {
                levePositionIds.addAll(((PositionProfilTravers)docPosition).getLevePositionIds());
            } else if (docPosition instanceof AbstractPositionDocumentAssociable) {
                final String docId = ((AbstractPositionDocumentAssociable)docPosition).getSirsdocument();
                if (docId != null && !docId.isEmpty()) {
                    docIds.add(docId);
                }
            }
        }

        if (!levePositionIds.isEmpty()) {
            AbstractSIRSRepository<LevePositionProfilTravers> repo = session.getRepositoryForClass(LevePositionProfilTravers.class);
            docIds.addAll(repo.get(levePositionIds.toArray(new String[0])).stream().map(tmpLeve -> tmpLeve.getLeveId()).collect(Collectors.toSet()));
        }

        return session.getPreviews().get(docIds.toArray(new String[docIds.size()])).stream();
    }

    private <T> Stream<T> getDocuments(final Map.Entry<String, Set<String>> ids) {
        final AbstractSIRSRepository repo = session.getRepositoryForType(ids.getKey());
        if (repo == null) {
            return Stream.empty();
        }

        return repo.get(ids.getValue()).stream();
    }
}
