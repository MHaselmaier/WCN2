package de.hs_kl.blesensor.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

public class SortUtil
{
    public static <E> void sort(List<E> list, Comparator<E> comparator)
    {
        // Implementierung aus der Java-Bibliothek
        // Wurde hier selbst implementiert, da es so nur ab Java 1.8 verfügbar ist
        // Unsere min. API von 21 unterstützt das noch nicht
        Object[] a = list.toArray();
        Arrays.sort(a, (Comparator)comparator);
        ListIterator<E> i = list.listIterator();
        for (Object e : a) {
            i.next();
            i.set((E) e);
        }
    }
}