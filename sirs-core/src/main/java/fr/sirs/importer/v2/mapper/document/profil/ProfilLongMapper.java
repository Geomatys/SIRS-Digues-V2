package fr.sirs.importer.v2.mapper.document.profil;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.RefOrigineProfilLong;
import fr.sirs.core.model.RefPositionProfilLongSurDigue;
import fr.sirs.core.model.RefSystemeReleveProfil;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.ImportContext;
import fr.sirs.importer.v2.mapper.AbstractMapper;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ProfilLongMapper extends AbstractMapper<ProfilLong> {

    private final AbstractImporter<Organisme> orgImporter;
    private final AbstractImporter<SystemeReperage> srImporter;
    private final AbstractImporter<RefSystemeReleveProfil> typeSRPImporter;
    private final AbstractImporter<RefPositionProfilLongSurDigue> typePositionImporter;
    private final AbstractImporter<RefOrigineProfilLong> typeOrigineImporter;

    private enum Columns {
        NOM,
        DATE_LEVE,
        ID_ORG_CREATEUR,
        ID_TYPE_SYSTEME_RELEVE_PROFIL,
        REFERENCE_CALQUE,
        ID_TYPE_POSITION_PROFIL_EN_LONG,
        ID_TYPE_ORIGINE_PROFIL_EN_LONG,
        //        ID_DOC_RAPPORT_ETUDES, // Mettre une référence vers UN rapport d'études
        ID_SYSTEME_REP_DZ
    }

    public ProfilLongMapper(Table table) {
        super(table);

        orgImporter = context.importers.get(Organisme.class);
        srImporter = context.importers.get(SystemeReperage.class);
        typeSRPImporter = context.importers.get(RefSystemeReleveProfil.class);
        typePositionImporter = context.importers.get(RefPositionProfilLongSurDigue.class);
        typeOrigineImporter = context.importers.get(RefOrigineProfilLong.class);
    }

    @Override
    public void map(Row input, ProfilLong output) throws IllegalStateException, IOException, AccessDbImporterException {
        output.setLibelle(input.getString(Columns.NOM.toString()));

        final String refCalque = input.getString(Columns.REFERENCE_CALQUE.toString());
        if (refCalque != null) {
            output.setReferenceCalque(refCalque);
        }

        final Date date = input.getDate(Columns.DATE_LEVE.toString());
        if (date != null) {
            output.setDateLeve(context.convertData(date, LocalDate.class));
        }

        final Object orgId = input.get(Columns.ID_ORG_CREATEUR.toString());
        if (orgId != null) {
            final String newOrgId = orgImporter.getImportedId(orgId);
            if (newOrgId == null) {
                throw new AccessDbImporterException("no mapping found for organisme id " + orgId);
            }
            output.setOrganismeCreateurId(newOrgId);
        }

        final Integer typeSys = input.getInt(Columns.ID_TYPE_SYSTEME_RELEVE_PROFIL.toString());
        if (typeSys != null) {
            String importedId = typeSRPImporter.getImportedId(typeSys);
            if (importedId == null) {
                throw new AccessDbImporterException("no mapping found for " + Columns.ID_TYPE_SYSTEME_RELEVE_PROFIL.toString() + " with id " + typeSys);
            }
            output.setTypeSystemesReleveId(importedId);
        }

        final Integer srid = input.getInt(Columns.ID_SYSTEME_REP_DZ.toString());
        if (srid != null) {
            final String newSrid = srImporter.getImportedId(srid);
            if (srid == null) {
                throw new AccessDbImporterException("No SR imported for ID " + srid);
            }
            output.setSystemeRepDzId(newSrid);
        }

        final Integer refOrigin = input.getInt(Columns.ID_TYPE_ORIGINE_PROFIL_EN_LONG.toString());
        if (refOrigin != null) {
            final String newOrigin = typeOrigineImporter.getImportedId(refOrigin);
            if (newOrigin == null) {
                throw new AccessDbImporterException("No profile origin available for ID " + refOrigin);
            }
            output.setOrigineProfilLongId(newOrigin);
        }

        final Integer refPosition = input.getInt(Columns.ID_TYPE_POSITION_PROFIL_EN_LONG.toString());
        if (refPosition != null) {
            final String importedId = typePositionImporter.getImportedId(refPosition);
            if (importedId == null) {
                throw new AccessDbImporterException("No position reference found for ID " + refPosition);
            }
            output.setPositionProfilLongSurDigueId(importedId);
        }
    }

    @Component
    public static class Spi implements MapperSpi<ProfilLong> {

        @Override
        public Optional<Mapper<ProfilLong>> configureInput(Table inputType) throws IllegalStateException {
            for (final Columns c : Columns.values()) {
                if (!ImportContext.columnExists(inputType, c.name())) {
                    return Optional.empty();
                }
            }
            return Optional.of(new ProfilLongMapper(inputType));
        }

        @Override
        public Class<ProfilLong> getOutputClass() {
            return ProfilLong.class;
        }
    }
}
