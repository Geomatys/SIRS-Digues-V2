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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class StructureImporter extends GenericImporter {

    private Map<Integer, List<Structure>> structures = null;
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
    
    public Map<Integer, List<Structure>> getStructuresByTronconId() throws IOException, AccessDbImporterException{
        if(structures == null){
            structures = new HashMap<>();
            final Map<Integer, List<Crete>> cretes = creteImporter.getCretesByTronconId();
            for(final Integer key : cretes.keySet()){
                if(structures.get(key)==null)structures.put(key, new ArrayList<>());
                structures.get(key).addAll(cretes.get(key));
            }
            
            final Map<Integer, List<Desordre>> desordres = desordreImporter.getDesordresByTronconId();
            for(final Integer key : desordres.keySet()){
                if(structures.get(key)==null)structures.put(key, new ArrayList<>());
                structures.get(key).addAll(desordres.get(key));
            }
            
            final Map<Integer, List<PiedDigue>> piedsDigue = piedDigueImporter.getPiedsDigueByTronconId();
            for(final Integer key : piedsDigue.keySet()){
                if(structures.get(key)==null)structures.put(key, new ArrayList<>());
                structures.get(key).addAll(piedsDigue.get(key));
            }
        }
        return structures;
    }

//    public List<Structure> getStructureList(final Integer accessTronconId) throws IOException, AccessDbImporterException {
//
//        if (structures == null) {
//            structures = new ArrayList<>();
//        }
//        List<Crete> cretes = creteImporter.getCretesByTronconId().get(accessTronconId);
//        if (cretes != null) {
//            structures.addAll(cretes);
//            System.out.println(structures);
//        }
//        List<Desordre> desordres = desordreImporter.getDesordresByTronconId().get(accessTronconId);
//        if (desordres != null) {
//            structures.addAll(desordres);
//        }
//        List<PiedDigue> piedsDigue = piedDigueImporter.getPiedsDigueByTronconId().get(accessTronconId);
//        if (piedsDigue != null) {
//            structures.addAll(piedsDigue);
//        }
//        return structures;
//    }

}
