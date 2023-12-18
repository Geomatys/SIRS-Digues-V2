/**
 * This file is part of SIRS-Digues 2.
 * <p>
 * Copyright (C) 2016, FRANCE-DIGUES,
 * <p>
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.theme.ui;

import fr.sirs.core.model.Preview;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.TronconTheme;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXTronconThemePane extends FXAbstractTronconThemePane<TronconDigue> {
    // Préview spécifique pour l'affichage des objets n'ayant pas d'identifiant de tronçon.(SYM-1765)
    protected static final Preview EMPTY_PREVIEW = new Preview();

    static {
        EMPTY_PREVIEW.setElementClass(TronconDigue.class.getCanonicalName());
        EMPTY_PREVIEW.setLibelle("   Pas de tronçon de rattachement - objets orphelins   ");
    }

    public FXTronconThemePane(TronconTheme.ThemeManager... groups) {
        super(TronconDigue.class, groups);
    }

    @Override
    protected Preview getEmptyPreview() {
        return EMPTY_PREVIEW;
    }
}
