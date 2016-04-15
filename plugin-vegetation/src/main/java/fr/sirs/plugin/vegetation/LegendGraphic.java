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
package fr.sirs.plugin.vegetation;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.logging.Level;
import static javax.swing.SwingConstants.CENTER;
import static javax.swing.SwingConstants.EAST;
import static javax.swing.SwingConstants.NORTH;
import static javax.swing.SwingConstants.NORTH_EAST;
import static javax.swing.SwingConstants.NORTH_WEST;
import static javax.swing.SwingConstants.SOUTH;
import static javax.swing.SwingConstants.SOUTH_EAST;
import static javax.swing.SwingConstants.SOUTH_WEST;
import static javax.swing.SwingConstants.WEST;
import org.geotoolkit.display.container.GraphicContainer;
import org.geotoolkit.display2d.canvas.J2DCanvas;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.container.ContextContainer2D;
import org.geotoolkit.display2d.ext.legend.GraphicLegendJ2D;
import org.geotoolkit.display2d.ext.legend.J2DLegendUtilities;
import org.geotoolkit.display2d.ext.legend.LegendTemplate;
import org.geotoolkit.map.MapContext;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class LegendGraphic extends GraphicLegendJ2D {

    private final LegendTemplate template;
    private final MapContext legendContext;

    public LegendGraphic(J2DCanvas canvas, LegendTemplate template, MapContext legendContext) {
        super(canvas, template);
        this.template = template;
        this.legendContext = legendContext;
    }

    protected void paint(final RenderingContext2D context, final int position, final int[] offset) {

        if(!isVisible()){
            return;
        }

        final GraphicContainer container = getCanvas().getContainer();
        if(!(container instanceof ContextContainer2D)) return;

        final Graphics2D g = context.getGraphics();
        context.switchToDisplayCRS();

        final Rectangle bounds = context.getCanvasDisplayBounds();
        Dimension maxSize = J2DLegendUtilities.estimate(g, legendContext, template, true);
        final int imgHeight = maxSize.height;
        final int imgWidth  = maxSize.width;
        int x = 0;
        int y = 0;

        switch(position){
            case NORTH :
                x = (bounds.width - imgWidth) / 2 + offset[0];
                y = offset[1];
                break;
            case NORTH_EAST :
                x = (bounds.width - imgWidth)  - offset[0];
                y = offset[1];
                break;
            case NORTH_WEST :
                x = offset[0];
                y = offset[1];
                break;
            case SOUTH :
                x = (bounds.width - imgWidth) / 2 + offset[0];
                y = (bounds.height - imgHeight) - offset[1];
                break;
            case SOUTH_EAST :
                x = (bounds.width - imgWidth) - offset[0];
                y = (bounds.height - imgHeight) - offset[1];
                break;
            case SOUTH_WEST :
                x = offset[0];
                y = (bounds.height - imgHeight) - offset[1];
                break;
            case CENTER :
                x = (bounds.width - imgWidth) / 2 + offset[0];
                y = (bounds.height - imgHeight) / 2 + offset[1];
                break;
            case EAST :
                x = (bounds.width - imgWidth) - offset[0];
                y = (bounds.height - imgHeight) / 2 + offset[1];
                break;
            case WEST :
                x = offset[0];
                y = (bounds.height - imgHeight) / 2 + offset[1];
                break;
        }
        try {
            //paint all labels, so that we avoid conflicts
            context.getLabelRenderer(true).portrayLabels();
        } catch (TransformException ex) {
            context.getMonitor().exceptionOccured(ex, Level.WARNING);
        }

        final Rectangle area = new Rectangle(x, y, imgWidth, imgHeight);
        J2DLegendUtilities.paintLegend(legendContext, g, area, template);
    }

}
