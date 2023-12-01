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
package fr.sirs.util;

import org.geotoolkit.style.RandomStyleBuilder;

import java.awt.*;

public enum UrgenceLayerColors {
    REF1("RefUrgence:1", new Color(255, 255, 0)),
    REF2("RefUrgence:2", new Color(255, 150, 0)),
    REF3("RefUrgence:3", new Color(255, 0, 0)),
    REF4("RefUrgence:4", new Color(180, 0, 250)),
    REF99("RefUrgence:99", new Color(255, 255, 255));

    private String refId;
    private Color color;

    UrgenceLayerColors(String refId, Color color) {
        this.refId = refId;
        this.color = color;
    }

    public static Color getColorByRefId(final String refId) {
        for (UrgenceLayerColors e : UrgenceLayerColors.values()) {
            if (e.getRefId().equals(refId)) {
                return e.getColor();
            }
        }
        return RandomStyleBuilder.randomColor();
    }

    public String getRefId() {
        return refId;
    }

    public Color getColor() {
        return color;
    }
}
