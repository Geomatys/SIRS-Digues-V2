/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2;

import fr.sirs.core.model.Element;
import fr.sirs.importer.AccessDbImporterException;

/**
 * A class whose aim is to update an already existing document by putting id of
 * another already existing document into it.
 *
 * @author Alexis Manin (Geomatys)
 * @param <U> Type of object which will contain the reference.
 */
public interface Linker<U extends Element> {

    /**
     * @return Type of the object which will contain the link.
     */
    Class<U> getHolderClass();

    /**
     *
     * @param accessContainerId Id of the target object in source MS-access database.
     * @param container the target object which will be modified to hold link.
     */
    void link(final Integer accessContainerId, final U container) throws AccessDbImporterException;

}
