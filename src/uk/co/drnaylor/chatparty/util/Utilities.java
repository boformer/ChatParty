/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.drnaylor.chatparty.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Daniel
 */
public final class Utilities {

    private Utilities() {
    }
    
    /**
     * Sorts a list. See http://stackoverflow.com/a/740351/3032166.
     * @param <T> Type of collection to sort.
     * @param c The collection.
     * @return A sorted list.
     */
    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<T>(c);
        java.util.Collections.sort(list);
        return list;
    }

}
