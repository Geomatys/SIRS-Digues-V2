/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.mapper.document.profil;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.mapper.AbstractMapper;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import java.io.IOException;
import java.util.Optional;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ProfilTraversMapper extends AbstractMapper<ProfilTravers> {

    private enum Columns {
        NOM
    }

    public ProfilTraversMapper(Table table) {
        super(table);
    }

    @Override
    public void map(Row input, ProfilTravers output) throws IllegalStateException, IOException, AccessDbImporterException {
        String name = input.getString(Columns.NOM.name());
        if (name != null) {
            output.setLibelle(name);
        }
    }

    public static class Spi implements MapperSpi<ProfilTravers> {

        @Override
        public Optional<Mapper<ProfilTravers>> configureInput(Table inputType) throws IllegalStateException {
            if (MapperSpi.checkColumns(inputType, Columns.values())) {
                return Optional.of(new ProfilTraversMapper(inputType));
            }
            return Optional.empty();
        }

        @Override
        public Class<ProfilTravers> getOutputClass() {
            return ProfilTravers.class;
        }

    }
}
