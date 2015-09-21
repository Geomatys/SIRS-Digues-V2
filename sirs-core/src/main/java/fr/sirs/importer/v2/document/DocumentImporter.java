/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.document;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Element;
import fr.sirs.importer.v2.AbstractImporter;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public abstract class DocumentImporter<T extends Element> extends AbstractImporter<T> {

    @Override
    protected T getOrCreateElement(Row input) {
        return super.getOrCreateElement(input); //To change body of generated methods, choose Tools | Templates.
    }


}
