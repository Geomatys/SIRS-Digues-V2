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
package org.geotoolkit.display2d.style.j2d;

import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

/**
 *
 * @author Samuel Andrés (Geomatys)
 *
 * Translation of PathWalker to support double precision.
 */
public class DoublePathWalker {

    private final PathIterator pathIterator;
    private final double lastPoint[] = new double[6];
    private final double currentPoint[] = new double[6];
    private double lastmoveToX = 0d;
    private double lastmoveToY = 0d;
    private double segmentStartX = 0d;
    private double segmentStartY = 0d;
    private double segmentEndX = 0d;
    private double segmentEndY = 0d;
    private double segmentLenght = 0d;
    private double remaining = 0d;
    private double angle = Double.NaN;
    private boolean finished = false;

    public DoublePathWalker(final PathIterator iterator) {
        this.pathIterator = iterator;

        //get the first segment
        boolean first = true;
        while (first && !pathIterator.isDone()) {
            final int type = pathIterator.currentSegment(currentPoint);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    System.arraycopy(currentPoint, 0, lastPoint, 0, 6);
                    segmentStartX = lastPoint[0];
                    segmentStartY = lastPoint[1];
                    segmentEndX = currentPoint[0];
                    segmentEndY = currentPoint[1];
                    //keep point for close instruction
                    lastmoveToX = currentPoint[0];
                    lastmoveToY = currentPoint[1];
                    break;

                case PathIterator.SEG_CLOSE:
                    currentPoint[0] = lastmoveToX;
                    currentPoint[1] = lastmoveToY;
                // Fall into....

                case PathIterator.SEG_LINETO:
                    segmentStartX = lastPoint[0];
                    segmentStartY = lastPoint[1];
                    segmentEndX = currentPoint[0];
                    segmentEndY = currentPoint[1];

                    segmentLenght = distance(segmentStartX, segmentStartY, segmentEndX, segmentEndY);
                    angle = Double.NaN;
                    remaining = segmentLenght;
                    first = false;
                    break;
            }
            System.arraycopy(currentPoint, 0, lastPoint, 0, 6);
            pathIterator.next();
        }

    }

    public boolean isFinished() {
        return finished; //|| (pathIterator.isDone() && remaining <= 0);
    }

    /**
     * Get the remaining distance until the current line segment end.
     * @return double
     */
    public double getSegmentLengthRemaining(){
        return remaining;
    }

    public void walk(double distance) {

        if (remaining > distance) {
            remaining -= distance;
        } else {
            distance -= remaining;
            remaining = 0;

            while (!pathIterator.isDone()) {
                final int type = pathIterator.currentSegment(currentPoint);
                switch (type) {
                    case PathIterator.SEG_MOVETO:
                        System.arraycopy(currentPoint, 0, lastPoint, 0, 6);
                        segmentStartX = lastPoint[0];
                        segmentStartY = lastPoint[1];
                        segmentEndX = currentPoint[0];
                        segmentEndY = currentPoint[1];
                        //keep point for close instruction
                        lastmoveToX = currentPoint[0];
                        lastmoveToY = currentPoint[1];
                        break;

                    case PathIterator.SEG_CLOSE:
                        currentPoint[0] = lastmoveToX;
                        currentPoint[1] = lastmoveToY;
                        // Fall into....

                    case PathIterator.SEG_LINETO:
                        segmentStartX = lastPoint[0];
                        segmentStartY = lastPoint[1];
                        segmentEndX = currentPoint[0];
                        segmentEndY = currentPoint[1];

                        segmentLenght = distance(segmentStartX, segmentStartY, segmentEndX, segmentEndY);
                        angle = Double.NaN;
                        remaining = segmentLenght;
                        break;
                }
                System.arraycopy(currentPoint, 0, lastPoint, 0, 6);
                pathIterator.next();

                if (remaining >= distance) {
                    remaining -= distance;
                    distance = 0;
                    return;
                } else {
                    distance -= remaining;
                    remaining = 0;
                }

            }

            //if we reach here, it means the iterator is finished and nothing left
            finished = true;
        }

    }

    public Point2D getPosition(final Point2D pt) {
        //bad geometries have overlaping points
        final double perc = (segmentLenght>0.0) ? (1d - remaining / segmentLenght) : 0.0;
        final double tlX = (segmentEndX - segmentStartX) * perc + segmentStartX;
        final double tlY = (segmentEndY - segmentStartY) * perc + segmentStartY;

        if(pt == null){
            return new Point2D.Double(tlX, tlY);
        }else{
            pt.setLocation(tlX, tlY);
            return pt;
        }
    }

    public double getRotation() {
        if(Double.isNaN(angle)){
            angle = angle(segmentStartX, segmentStartY, segmentEndX, segmentEndY);
        }
        return angle;
    }

    public static double distance(final double x1, final double y1, final double x2, final double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static double angle(final double x1, final double y1, final double x2, final double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.atan2(dy, dx);
    }

}
