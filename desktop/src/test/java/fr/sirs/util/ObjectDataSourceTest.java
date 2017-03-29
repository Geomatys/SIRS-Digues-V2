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
import fr.sirs.core.model.Observation;
import fr.sirs.core.model.Photo;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Samuel Andrés (Geomatys) <samuel.andres at geomatys.com>
 */
public class ObjectDataSourceTest {
    
    @Test
    public void test_forceArialFontFace(){
        final String comment = "<html dir=\"ltr\">"
                + "<head></head>"
                + "<body contenteditable=\"true\">"
                + "<p><font face=\"Segoe UI\">Crue ponctuelle sur certains cours d'eau.</font></p>"
                + "<p><font face=\"Segoe UI\">Certains ouvrages ont pu être mis en charge ponctuellement</font></p>"
                + "</body>"
                + "</html>";
        final String result = ObjectDataSource.forceArialFontFace(comment);
        
        final String expected = "<html dir=\"ltr\">"
                + "<head></head>"
                + "<body contenteditable=\"true\">"
                + "<p><font face=\"Arial\">Crue ponctuelle sur certains cours d'eau.</font></p>"
                + "<p><font face=\"Arial\">Certains ouvrages ont pu être mis en charge ponctuellement</font></p>"
                + "</body>"
                + "</html>";
        
        Assert.assertEquals(expected, result);
    }
    
    @Test
    public void test_observationComparator(){
        final List<Observation> obs = new ArrayList<>();
        final Observation o01 = new Observation();
        o01.setDesignation("o01");
        o01.setDate(null);
        obs.add(o01);
        final Observation o1 = new Observation();
        o1.setDesignation("o1");
        o1.setDate(LocalDate.now());
        obs.add(o1);
        final Observation o02 = new Observation();
        o02.setDesignation("o02");
        o02.setDate(null);
        obs.add(o02);
        final Observation o2 = new Observation();
        o2.setDesignation("o2");
        o2.setDate(LocalDate.now().minusDays(1l));
        obs.add(o2);
        final Observation o3 = new Observation();
        o3.setDesignation("o3");
        o3.setDate(LocalDate.now().plusDays(1l));
        obs.add(o3);
        
        obs.sort(ObjectDataSource.OBSERVATION_COMPARATOR);
        
        assertTrue(obs.indexOf(o3)==0);
        assertTrue(obs.indexOf(o1)==1);
        assertTrue(obs.indexOf(o2)==2);
        assertTrue(obs.indexOf(o01)==3 || obs.indexOf(o01)==4);
        assertTrue(obs.indexOf(o02)==3 || obs.indexOf(o02)==4);
    }
    
    @Test
    public void test_photoComparator(){
        final List<Photo> obs = new ArrayList<>();
        final Photo o01 = new Photo();
        o01.setDesignation("o01");
        o01.setDate(null);
        obs.add(o01);
        final Photo o1 = new Photo();
        o1.setDesignation("o1");
        o1.setDate(LocalDate.now());
        obs.add(o1);
        final Photo o02 = new Photo();
        o02.setDesignation("o02");
        o02.setDate(null);
        obs.add(o02);
        final Photo o2 = new Photo();
        o2.setDesignation("o2");
        o2.setDate(LocalDate.now().minusDays(1l));
        obs.add(o2);
        final Photo o3 = new Photo();
        o3.setDesignation("o3");
        o3.setDate(LocalDate.now().plusDays(1l));
        obs.add(o3);
        
        obs.sort(ObjectDataSource.PHOTO_COMPARATOR);
        
        assertTrue(obs.indexOf(o3)==0);
        assertTrue(obs.indexOf(o1)==1);
        assertTrue(obs.indexOf(o2)==2);
        assertTrue(obs.indexOf(o01)==3 || obs.indexOf(o01)==4);
        assertTrue(obs.indexOf(o02)==3 || obs.indexOf(o02)==4);
    }
    
    @Test
    public void test_elementComparator(){
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
        
        
        final Desordre d3= new Desordre();
        d3.setLinearId("lin1");
        d3.setDesignation("d3");
        des.add(d3);
        final Desordre d4= new Desordre();
        d4.setLinearId("lin2");
        d4.setDesignation("d4");
        des.add(d4);
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
        final Desordre d5 = new Desordre();
        d5.setLinearId("lin3");
        d5.setDesignation("d5");
        des.add(d5);
        final Desordre d1 = new Desordre();
        d1.setLinearId("lin3");
        d1.setDesignation("d1");
        des.add(d1);
        final Desordre d2 = new Desordre();
        d2.setLinearId("lin3");
        d2.setDesignation("d2");
        des.add(d2);
        final Desordre d10 = new Desordre();
        d10.setLinearId("lin3");
        d10.setDesignation("d10");
        des.add(d10);
        
        des.sort(ObjectDataSource.ELEMENT_COMPARATOR);
        
        assertTrue(des.indexOf(d1)==0);
        assertTrue(des.indexOf(d10)==1);
        assertTrue(des.indexOf(d13)==2);
        assertTrue(des.indexOf(d2)==3);
        assertTrue(des.indexOf(d3)==4);
        assertTrue(des.indexOf(d4)==5);
        assertTrue(des.indexOf(d5)==6);
        assertTrue(des.indexOf(d6)==7);
        assertTrue(des.indexOf(d7)==8);
        
        assertTrue(des.indexOf(d8)==9);
        assertTrue(des.indexOf(d9)==10);
        assertTrue(des.indexOf(d11)==11 || des.indexOf(d11)==12);
        assertTrue(des.indexOf(d12)==11 || des.indexOf(d12)==12);
    }
}
