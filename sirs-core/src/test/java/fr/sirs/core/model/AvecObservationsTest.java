/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.core.model;

import java.time.LocalDate;
import java.time.Month;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author maximegavens
 */
public class AvecObservationsTest {

    @Test
    public void lastObservationPredicateTest() {
        Observation o1 = new Observation();
        o1.setDate(LocalDate.of(1912, 6, 23));
        Observation o2 = new Observation();
        o2.setDate(LocalDate.of(1955, 2, 24));

        Desordre d1 = new Desordre();
        d1.observations.add(o1);
        d1.observations.add(o2);

        AvecObservations.LastObservationPredicate lop = new AvecObservations.LastObservationPredicate(LocalDate.of(1900, Month.MARCH, 1), LocalDate.of(2000, Month.MARCH, 1));
        AvecObservations.LastObservationPredicate lop2 = new AvecObservations.LastObservationPredicate(LocalDate.of(1900, Month.MARCH, 1), LocalDate.of(1950, Month.MARCH, 1));

        Assert.assertTrue(lop.test(d1));
        Assert.assertFalse(lop2.test(d1));
    }
}
