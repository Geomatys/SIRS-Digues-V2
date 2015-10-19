/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.mapper.document.profil;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.ParametreHydrauliqueProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.ImportContext;
import fr.sirs.importer.v2.mapper.AbstractMapper;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ParamHydroPTraversMapper extends AbstractMapper<ParametreHydrauliqueProfilTravers> {

    private final AbstractImporter<EvenementHydraulique> hydroImporter;

    private enum Columns {
        ID_EVENEMENT_HYDRAU,
        DEBIT_DE_POINTE_M3S,
        VITESSE_DE_POINTE_MS,
        COTE_EAU_NGF
    }

    public ParamHydroPTraversMapper(Table table) {
        super(table);
        hydroImporter = context.importers.get(EvenementHydraulique.class);
    }

    @Override
    public void map(Row input, ParametreHydrauliqueProfilTravers output) throws IllegalStateException, IOException, AccessDbImporterException {
        final Object idHydro = input.get(Columns.ID_EVENEMENT_HYDRAU.toString());
        if (idHydro != null) {
            output.setEvenementHydrauliqueId(hydroImporter.getImportedId(idHydro));
        }

        final Double debit = input.getDouble(Columns.DEBIT_DE_POINTE_M3S.toString());
        if (debit != null) {
            output.setDebitPointe(debit.floatValue());
        }

        final Double vitesse = input.getDouble(Columns.VITESSE_DE_POINTE_MS.toString());
        if (vitesse != null) {
            output.setVitessePointe(vitesse.floatValue());
        }

        final Double cote = input.getDouble(Columns.COTE_EAU_NGF.toString());
        if (cote != null) {
            output.setCoteEau(cote.floatValue());
        }

    }

    @Component
    public static class Spi implements MapperSpi<ParametreHydrauliqueProfilTravers> {

        @Override
        public Optional<Mapper<ParametreHydrauliqueProfilTravers>> configureInput(Table inputType) throws IllegalStateException {
            for (final Columns c : Columns.values()) {
                if (!ImportContext.columnExists(inputType, c.name())) {
                    return Optional.empty();
                }
            }
            return Optional.of(new ParamHydroPTraversMapper(inputType));
        }

        @Override
        public Class<ParametreHydrauliqueProfilTravers> getOutputClass() {
            return ParametreHydrauliqueProfilTravers.class;
        }
    }

}
