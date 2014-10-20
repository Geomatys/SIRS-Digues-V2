/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer.structure;

import com.healthmarketscience.jackcess.Database;
import fr.sym.util.importer.AccessDbImporterException;
import fr.sym.util.importer.GenericImporter;
import fr.sym.util.importer.TronconGestionDigueImporter;
import fr.symadrem.sirs.core.model.Crete;
import fr.symadrem.sirs.core.model.Desordre;
import fr.symadrem.sirs.core.model.PiedDigue;
import fr.symadrem.sirs.core.model.Structure;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class StructureImporter extends GenericImporter {

    private CreteImporter creteImporter;
    private DesordreImporter desordreImporter;
    private PiedDigueImporter piedDigueImporter;

    private StructureImporter(Database accessDatabase) {
        super(accessDatabase);
    }

    public StructureImporter(final Database accessDatabase, final TronconGestionDigueImporter tronconGestionDigueImporter) {
        this(accessDatabase);
        this.creteImporter = new CreteImporter(accessDatabase, tronconGestionDigueImporter);
        this.desordreImporter = new DesordreImporter(accessDatabase, tronconGestionDigueImporter);
        this.piedDigueImporter = new PiedDigueImporter(accessDatabase, tronconGestionDigueImporter);
    }

    public List<Structure> getStructureList(final Integer accessTronconId) throws IOException, AccessDbImporterException {
        List<Structure> structures = new ArrayList<>();

        List<Crete> cretes = creteImporter.getCretesByTronconId().get(accessTronconId);
        if (cretes != null) {
            structures.addAll(cretes);
        }
        List<Desordre> desordres = desordreImporter.getDesordresByTronconId().get(accessTronconId);
        if (desordres != null) {
            structures.addAll(desordres);
        }
        List<PiedDigue> piedsDigue = piedDigueImporter.getPiedsDigueByTronconId().get(accessTronconId);
        if (piedsDigue != null) {
            structures.addAll(piedsDigue);
        }
        return structures;
    }

}
