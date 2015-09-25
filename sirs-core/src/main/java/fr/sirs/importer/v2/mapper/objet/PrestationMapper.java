package fr.sirs.importer.v2.mapper.objet;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefPrestation;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import java.io.IOException;
import java.util.Optional;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class PrestationMapper extends AbstractObjetMapper<Prestation> {

    private final AbstractImporter<Marche> marcheImporter;
    private final AbstractImporter<RefPrestation> typePrestationImporter;
    private final AbstractImporter<RefCote> typeCoteImporter;

    private enum Columns {

        ID_MARCHE,
        REALISATION_INTERNE,
        ID_TYPE_PRESTATION,
        COUT_AU_METRE,
        COUT_GLOBAL,
        ID_TYPE_COTE,
        ID_TYPE_POSITION,
        ////        ID_INTERV_REALISATEUR, // Ne sert à rien : voir la table PRESTATION_INTERVENANT
        ID_SOURCE,
    };

    public PrestationMapper(Table table) {
        super(table);
        marcheImporter = context.importers.get(Marche.class);
        typePrestationImporter = context.importers.get(RefPrestation.class);
        typeCoteImporter = context.importers.get(RefCote.class);
    }

    @Override
    public void map(Row input, Prestation output) throws IllegalStateException, IOException, AccessDbImporterException {
        final Integer marcheId = input.getInt(Columns.ID_MARCHE.name());
        if (marcheId != null) {
            output.setMarcheId(marcheImporter.getImportedId(marcheId));
        }

        final Integer typePrestationId = input.getInt(Columns.ID_TYPE_PRESTATION.name());
        if (typePrestationId != null) {
            output.setTypePrestationId(typePrestationImporter.getImportedId(typePrestationId));
        }

        final Integer typeCoteId = input.getInt(Columns.ID_TYPE_COTE.name());
        if (typeCoteId != null) {
            output.setCoteId(typeCoteImporter.getImportedId(typeCoteId));
        }

        final Integer typePositionId = input.getInt(Columns.ID_TYPE_POSITION.name());
        if (typePositionId != null) {
            output.setPositionId(RefPositionImporter.getImportedId(typePositionId));
        }

        final Integer typeSourceId = input.getInt(Columns.ID_SOURCE.name());
        if (typeSourceId != null) {
            output.setSourceId(RefSourceImporter.getImportedId(typeSourceId));
        }

        final Boolean realisation = input.getBoolean(Columns.REALISATION_INTERNE.toString());
        if (realisation != null)
            output.setRealisationInterne(realisation);

        final Double coutMetre = input.getDouble(Columns.COUT_AU_METRE.toString());
        if (coutMetre != null) {
            output.setCoutMetre(coutMetre.floatValue());
        }

        final Double coutGlobal = input.getDouble(Columns.COUT_GLOBAL.toString());
        if (coutGlobal != null) {
            output.setCoutGlobal(coutGlobal.floatValue());
        }
    }

    public static class Spi implements MapperSpi<Prestation> {

        @Override
        public Optional<Mapper<Prestation>> configureInput(Table inputType) throws IllegalStateException {
            if (MapperSpi.checkColumns(inputType, Columns.values())) {
                return Optional.of(new PrestationMapper(inputType));
            }
            return Optional.empty();
        }

        @Override
        public Class<Prestation> getOutputClass() {
            return Prestation.class;
        }
    }
}
