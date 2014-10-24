/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer.structure;

import com.healthmarketscience.jackcess.Database;
import fr.sym.util.importer.BorneDigueImporter;
import fr.sym.util.importer.GenericImporter;
import fr.sym.util.importer.SystemeReperageImporter;
import fr.sym.util.importer.TronconGestionDigueImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
abstract class GenericStructureImporter extends GenericImporter {

    protected TronconGestionDigueImporter tronconGestionDigueImporter;
    protected SystemeReperageImporter systemeReperageImporter;
    protected BorneDigueImporter borneDigueImporter;
    
    private GenericStructureImporter(Database accessDatabase) {
        super(accessDatabase);
    }
    
    public GenericStructureImporter(final Database accessDatabase, final TronconGestionDigueImporter tronconGestionDigueImporter, final SystemeReperageImporter systemeReperageImporter, final BorneDigueImporter borneDigueImporter) {
        this(accessDatabase);
        this.tronconGestionDigueImporter = tronconGestionDigueImporter;
        this.systemeReperageImporter = systemeReperageImporter;
        this.borneDigueImporter = borneDigueImporter;
    }
}
