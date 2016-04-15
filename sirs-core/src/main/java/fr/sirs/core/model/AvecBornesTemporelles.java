/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.core.model;

import java.time.LocalDate;
import javafx.beans.property.ObjectProperty;

/**
 * Spécifie un interval de validité temporelle, borné par une date de début et une 
 * date de fin.
 * 
 * @author Alexis Manin (Geomatys)
 */
public interface AvecBornesTemporelles {
             
    public ObjectProperty<LocalDate> date_debutProperty();

    public LocalDate getDate_debut();

    public void setDate_debut(LocalDate date_debut);

    public ObjectProperty<LocalDate> date_finProperty();

    public LocalDate getDate_fin();

    public void setDate_fin(LocalDate date_fin);
}
