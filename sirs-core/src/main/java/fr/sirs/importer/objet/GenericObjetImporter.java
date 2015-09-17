package fr.sirs.importer.objet;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefFonction;
import fr.sirs.core.model.RefMateriau;
import fr.sirs.core.model.RefNature;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.v2.AbstractPositionableImporter;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
public abstract class GenericObjetImporter<T extends Objet> extends AbstractPositionableImporter<T> {

    private enum Columns {
        ID_TRONCON_GESTION,
        DATE_DEBUT_VAL,
        DATE_FIN_VAL,
        DATE_DERNIERE_MAJ,
        COMMENTAIRE
    }

    protected Map<Integer, List<T>> objetsByTronconId = null;

    protected AbstractImporter<TronconDigue> tronconGestionDigueImporter;

    protected AbstractImporter<RefSource> sourceInfoImporter;
    protected AbstractImporter<RefCote> typeCoteImporter;
    protected AbstractImporter<RefPosition> typePositionImporter;
    protected AbstractImporter<RefMateriau> typeMateriauImporter;
    protected AbstractImporter<RefNature> typeNatureImporter;
    protected AbstractImporter<RefFonction> typeFonctionImporter;

    protected AbstractSIRSRepository<TronconDigue> tronconRepo;
    protected AbstractSIRSRepository<BorneDigue> borneRepo;

    /**
     *
     * @return A map containing all T instances accessibles from the
     * internal database identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, T> getById() throws IOException, AccessDbImporterException {
        if (objets == null) {
            compute();
        }
        return objets;
    }

    /**
     *
     * @return A map containing all T instances accessibles from the
     * internal database <em>TronconDigue</em> identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, List<T>> getByTronconId() throws IOException, AccessDbImporterException {
        if (objetsByTronconId == null) {
            compute();
        }
        return objetsByTronconId;
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();
        // TODO : If one of those parameters is null, report an error now.
        this.tronconGestionDigueImporter = context.importers.get(TronconDigue.class);
        this.sourceInfoImporter = context.importers.get(RefSource.class);
        this.typeCoteImporter = context.importers.get(RefCote.class);
        this.typePositionImporter = context.importers.get(RefPosition.class);
        this.typeMateriauImporter = context.importers.get(RefMateriau.class);
        this.typeNatureImporter = context.importers.get(RefNature.class);
        this.typeFonctionImporter = context.importers.get(RefFonction.class);

        tronconRepo = session.getRepositoryForClass(TronconDigue.class);
        borneRepo = session.getRepositoryForClass(BorneDigue.class);
    }

    @Override
    protected void postCompute() {
        super.postCompute();

        this.tronconGestionDigueImporter = null;
        this.sourceInfoImporter = null;
        this.typeCoteImporter = null;
        this.typePositionImporter = null;
        this.typeMateriauImporter = null;
        this.typeNatureImporter = null;
        this.typeFonctionImporter = null;
    }

    @Override
    public T importRow(Row row, T output) throws IOException, AccessDbImporterException {
        output = super.importRow(row, output);


        String importedId = tronconGestionDigueImporter.getImportedId(row.getInt(Columns.ID_TRONCON_GESTION.name()));
        if (importedId == null) {
            throw new AccessDbImporterException("An object cannot be imported : No troncon specified.");
        }

        final Date dateDebut = row.getDate(Columns.DATE_DEBUT_VAL.toString());
        if (dateDebut != null) {
            output.setDate_debut(context.convertData(dateDebut, LocalDate.class));
        }

        final Date dateFin = row.getDate(Columns.DATE_FIN_VAL.toString());
        if (dateFin != null) {
            output.setDate_fin(context.convertData(dateFin, LocalDate.class));
        }

        final Date dateMaj = row.getDate(Columns.DATE_DERNIERE_MAJ.toString());
        if (dateMaj != null) {
            output.setDateMaj(context.convertData(dateMaj, LocalDate.class));
        }

        output.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));

        // TODO : optimize geometry computing (find a way to cache SegmentInfo[] object computed from troncon geometry).
        final TronconDigue troncon = tronconRepo.get(importedId);
        output.setGeometry(LinearReferencingUtilities.buildGeometry(troncon.getGeometry(), output, borneRepo));

        return output;
    }


}
