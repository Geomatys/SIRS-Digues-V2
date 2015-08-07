/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.util;

import org.geotoolkit.util.collection.CloseableIterator;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public interface StreamingIterable<T> extends Iterable<T> {

    @Override
    public CloseableIterator<T> iterator();
}
