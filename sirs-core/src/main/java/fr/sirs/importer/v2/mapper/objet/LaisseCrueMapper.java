package fr.sirs.importer.v2.mapper.objet;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.LaisseCrue;
import fr.sirs.core.model.RefReferenceHauteur;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class LaisseCrueMapper extends AbstractObjetMapper<LaisseCrue> {

    private final AbstractImporter<Contact> contactImporter;
    private final AbstractImporter<EvenementHydraulique> hydroImporter;
    private final AbstractImporter<RefReferenceHauteur> typeHauteurImporter;

    private enum Columns {
//        ID_LAISSE_CRUE,

        ID_SOURCE,
        ID_EVENEMENT_HYDRAU,
        ID_TYPE_REF_HEAU,
        ID_INTERV_OBSERVATEUR,
        DATE,
        HAUTEUR_EAU,
        POSITION
    };

    public LaisseCrueMapper(Table table) {
        super(table);
        contactImporter = context.importers.get(Contact.class);
        hydroImporter = context.importers.get(EvenementHydraulique.class);
        typeHauteurImporter = context.importers.get(RefReferenceHauteur.class);
    }

    @Override
    public void map(Row input, LaisseCrue output) throws IllegalStateException, IOException, AccessDbImporterException {
        final Object hydroId = input.get(Columns.ID_EVENEMENT_HYDRAU.name());
        if (hydroId != null) {
            output.setEvenementHydrauliqueId(hydroImporter.getImportedId(hydroId));
        }

        Integer typeHauteur = input.getInt(Columns.ID_TYPE_REF_HEAU.name());
        if (typeHauteur != null) {
            output.setReferenceHauteurId(typeHauteurImporter.getImportedId(typeHauteur));
        }

        final Object contactId = input.get(Columns.ID_INTERV_OBSERVATEUR.name());
        if (contactId != null) {
            output.setObservateurId(contactImporter.getImportedId(contactId));
        }

        final Object typePositionId = input.get(Columns.POSITION.name());
        if (typePositionId != null) {
            output.setPositionLaisse(RefPositionImporter.getImportedId(typePositionId));
        }

        final Object typeSourceId = input.get(Columns.ID_SOURCE.name());
        if (typeSourceId != null) {
            output.setSourceId(RefSourceImporter.getImportedId(typeSourceId));
        }

        final String position = input.getString(Columns.POSITION.toString());
        if (position != null)
            output.setPositionLaisse(position);

        final Double hauteur = input.getDouble(Columns.HAUTEUR_EAU.toString());
        if (hauteur != null) {
            output.setHauteur(hauteur.floatValue());
        }

        final Date date = input.getDate(Columns.DATE.toString());
        if (date != null) {
            output.setDate(context.convertData(date, LocalDateTime.class));
        }
    }

    public static class Spi implements MapperSpi<LaisseCrue> {

        @Override
        public Optional<Mapper<LaisseCrue>> configureInput(Table inputType) throws IllegalStateException {
            if (MapperSpi.checkColumns(inputType, Columns.values())) {
                return Optional.of(new LaisseCrueMapper(inputType));
            }
            return Optional.empty();
        }

        @Override
        public Class<LaisseCrue> getOutputClass() {
            return LaisseCrue.class;
        }
    }
}
