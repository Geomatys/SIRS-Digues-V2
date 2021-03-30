/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.util;

import java.util.Comparator;

/**
 * This custom comparator is used to compare String that contains Double value.
 * With these following rules:
 *  - A string that represents a Double is always superior to a string which does not represent one
 *  - Use classic comparator in the case of two String or two Double
 *
 * @author maximegavens
 */
public class AlphaNumComparator implements Comparator<String> {
    @Override
    public int compare(String s1, String s2) {
        if (s1 == s2) return 0;
        if (s1 == null) return -1;
        if (s2 == null) return 1;
        try {
            final Double d1 = Double.parseDouble(s1);
            try {
                final Double d2 = Double.parseDouble(s2);
                return d1.compareTo(d2);
            } catch (NumberFormatException ex) {
                return 1;
            }
        } catch (NumberFormatException ex) {
            try {
                final double d2 = Double.parseDouble(s2);
                return -1;
            } catch (NumberFormatException ex2) {
                return s1.compareTo(s2);
            }
        }
    }
}
