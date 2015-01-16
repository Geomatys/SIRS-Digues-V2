/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package fr.sirs.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public interface FXTextAbregeable {
    
    BooleanProperty abregeableProperty();
    boolean isAbregeable();
    void setAbregeable(final boolean abregeable);
    
    IntegerProperty nbAffichableProperty();
    int getNbAffichable();
    void setNbAffichable(final int nbAffichable);
}
