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
package fr.sirs.util;

import fr.sirs.core.model.Desordre;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class PrinterUtilitiesTest {
    
    @Test
    public void test_objetComparator(){
        final List<Desordre> des = new ArrayList<>();
        
        final Desordre d11 = new Desordre();
        d11.setLinearId("lin2");
        d11.setDesignation(null);
        des.add(d11);
        final Desordre d12 = new Desordre();
        d12.setLinearId(null);
        d12.setDesignation(null);
        des.add(d12);
        final Desordre d13 = new Desordre();
        d13.setLinearId(null);
        d13.setDesignation("d13");
        des.add(d13);
        
        
        final Desordre d1 = new Desordre();
        d1.setLinearId("lin3");
        d1.setDesignation("d1");
        des.add(d1);
        final Desordre d2 = new Desordre();
        d2.setLinearId("lin3");
        d2.setDesignation("d2");
        des.add(d2);
        final Desordre d3= new Desordre();
        d3.setLinearId("lin1");
        d3.setDesignation("d3");
        des.add(d3);
        final Desordre d4= new Desordre();
        d4.setLinearId("lin2");
        d4.setDesignation("d4");
        des.add(d4);
        final Desordre d5 = new Desordre();
        d5.setLinearId("lin3");
        d5.setDesignation("d5");
        des.add(d5);
        final Desordre d6 = new Desordre();
        d6.setLinearId("lin2");
        d6.setDesignation("d6");
        des.add(d6);
        final Desordre d7 = new Desordre();
        d7.setLinearId("lin1");
        d7.setDesignation("d7");
        des.add(d7);
        final Desordre d8 = new Desordre();
        d8.setLinearId("lin3");
        d8.setDesignation("d8");
        des.add(d8);
        final Desordre d9 = new Desordre();
        d9.setLinearId("lin1");
        d9.setDesignation("d9");
        des.add(d9);
        final Desordre d10 = new Desordre();
        d10.setLinearId("lin3");
        d10.setDesignation("d10");
        des.add(d10);
        
        des.sort(PrinterUtilities.OBJET_COMPARATOR);
        
        assertTrue(des.indexOf(d3)==0);
        assertTrue(des.indexOf(d7)==1);
        assertTrue(des.indexOf(d9)==2);
        assertTrue(des.indexOf(d4)==3);
        assertTrue(des.indexOf(d6)==4);
        assertTrue(des.indexOf(d11)==5);
        assertTrue(des.indexOf(d1)==6);
        
        assertTrue(des.indexOf(d10)==7);
        assertTrue(des.indexOf(d2)==8);
        assertTrue(des.indexOf(d5)==9);
        assertTrue(des.indexOf(d8)==10);
        assertTrue(des.indexOf(d13)==11 || des.indexOf(d13)==12);
        assertTrue(des.indexOf(d12)==11 || des.indexOf(d12)==12);
    }
}
