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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Samuel Andrés (Geomatys) <samuel.andres at geomatys.com>
 */
public class ReseauHydrauliqueFermeDataSourceTest {
    
    @Test
    public void test_observationComparator(){
        final List<JRDesordreTableRow> obs = new ArrayList<>();
        final JRDesordreTableRow oB2 = new JRDesordreTableRow(LocalDate.now().minusDays(1), "d2", "", "", "");
        obs.add(oB2);
        final JRDesordreTableRow oA01 = new JRDesordreTableRow(null, "d1", "", "", "");
        obs.add(oA01);
        final JRDesordreTableRow oA1 = new JRDesordreTableRow(LocalDate.now(), "d1", "", "", "");
        obs.add(oA1);
        final JRDesordreTableRow oB01 = new JRDesordreTableRow(null, "d2", "", "", "");
        obs.add(oB01);
        final JRDesordreTableRow oB02 = new JRDesordreTableRow(null, "d2", "", "", "");
        obs.add(oB02);
        final JRDesordreTableRow oA4 = new JRDesordreTableRow(LocalDate.now().plusDays(2), "d1", "", "", "");
        obs.add(oA4);
        final JRDesordreTableRow oA02 = new JRDesordreTableRow(null, "d1", "", "", "");
        obs.add(oA02);
        final JRDesordreTableRow oA2 = new JRDesordreTableRow(LocalDate.now().minusDays(1), "d1", "", "", "");
        obs.add(oA2);
        final JRDesordreTableRow oB3 = new JRDesordreTableRow(LocalDate.now().plusDays(1), "d2", "", "", "");
        obs.add(oB3);
        final JRDesordreTableRow oA3 = new JRDesordreTableRow(LocalDate.now().plusDays(1), "d1", "", "", "");
        obs.add(oA3);
        final JRDesordreTableRow oB4 = new JRDesordreTableRow(LocalDate.now().plusDays(2), "d2", "", "", "");
        obs.add(oB4);
        final JRDesordreTableRow oB1 = new JRDesordreTableRow(LocalDate.now(), "d2", "", "", "");
        obs.add(oB1);
        
        obs.sort(ReseauHydrauliqueFermeDataSource.DESORDRE_RESEAU_COMPARATOR);
        
        assertTrue(obs.indexOf(oA4)==0);
        assertTrue(obs.indexOf(oA3)==1);
        assertTrue(obs.indexOf(oA1)==2);
        assertTrue(obs.indexOf(oA2)==3);
        assertTrue(obs.indexOf(oA01)==4 || obs.indexOf(oA01)==5);
        assertTrue(obs.indexOf(oA02)==4 || obs.indexOf(oA02)==5);
        
        assertTrue(obs.indexOf(oB4)==6);
        assertTrue(obs.indexOf(oB3)==7);
        assertTrue(obs.indexOf(oB1)==8);
        assertTrue(obs.indexOf(oB2)==9);
        assertTrue(obs.indexOf(oB01)==10 || obs.indexOf(oB01)==11);
        assertTrue(obs.indexOf(oB02)==10 || obs.indexOf(oB02)==11);
    }
    
    @Test
    public void test_observationDateComparator(){
        final List<JRDesordreTableRow> obs = new ArrayList<>();
        final JRDesordreTableRow oB2 = new JRDesordreTableRow(LocalDate.now().minusDays(1), "d2", "", "", "");
        obs.add(oB2);
        final JRDesordreTableRow oA01 = new JRDesordreTableRow(null, "d1", "", "", "");
        obs.add(oA01);
        final JRDesordreTableRow oA1 = new JRDesordreTableRow(LocalDate.now(), "d1", "", "", "");
        obs.add(oA1);
        final JRDesordreTableRow oB01 = new JRDesordreTableRow(null, "d2", "", "", "");
        obs.add(oB01);
        final JRDesordreTableRow oB02 = new JRDesordreTableRow(null, "d2", "", "", "");
        obs.add(oB02);
        final JRDesordreTableRow oA4 = new JRDesordreTableRow(LocalDate.now().plusDays(2), "d1", "", "", "");
        obs.add(oA4);
        final JRDesordreTableRow oA02 = new JRDesordreTableRow(null, "d1", "", "", "");
        obs.add(oA02);
        final JRDesordreTableRow oA2 = new JRDesordreTableRow(LocalDate.now().minusDays(1), "d1", "", "", "");
        obs.add(oA2);
        final JRDesordreTableRow oB3 = new JRDesordreTableRow(LocalDate.now().plusDays(1), "d2", "", "", "");
        obs.add(oB3);
        final JRDesordreTableRow oA3 = new JRDesordreTableRow(LocalDate.now().plusDays(1), "d1", "", "", "");
        obs.add(oA3);
        final JRDesordreTableRow oB4 = new JRDesordreTableRow(LocalDate.now().plusDays(2), "d2", "", "", "");
        obs.add(oB4);
        final JRDesordreTableRow oB1 = new JRDesordreTableRow(LocalDate.now(), "d2", "", "", "");
        obs.add(oB1);
        
        obs.sort(ReseauHydrauliqueFermeDataSource.DESORDRE_RESEAU_DATE_COMPARATOR);
        
        assertTrue(obs.indexOf(oB4)==0 || obs.indexOf(oB4)==1);
        assertTrue(obs.indexOf(oA4)==0 || obs.indexOf(oA4)==1);
        assertTrue(obs.indexOf(oB3)==2 || obs.indexOf(oB3)==3);
        assertTrue(obs.indexOf(oA3)==2 || obs.indexOf(oA3)==3);
        assertTrue(obs.indexOf(oA1)==4 || obs.indexOf(oA1)==5);
        assertTrue(obs.indexOf(oB1)==4 || obs.indexOf(oB1)==5);
        assertTrue(obs.indexOf(oB2)==6 || obs.indexOf(oB2)==7);
        assertTrue(obs.indexOf(oA2)==6 || obs.indexOf(oA2)==7);
        assertTrue(obs.indexOf(oA01)==8 || obs.indexOf(oA01)==9 || obs.indexOf(oA01)==10 || obs.indexOf(oA01)==11);
        assertTrue(obs.indexOf(oB01)==8 || obs.indexOf(oB01)==9 || obs.indexOf(oB01)==10 || obs.indexOf(oB01)==11);
        assertTrue(obs.indexOf(oA02)==8 || obs.indexOf(oA02)==9 || obs.indexOf(oA02)==10 || obs.indexOf(oA02)==11);
        assertTrue(obs.indexOf(oB02)==8 || obs.indexOf(oB02)==9 || obs.indexOf(oB02)==10 || obs.indexOf(oB02)==11);
    }
}
