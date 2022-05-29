//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

/**
 * A type for anything whose underlying data structure can be sorted.
 */
public interface Sortable {

    /**
     * Sorts the underlying data structure by an arbitrary property/comparable defined by the concrete implementors.
     */
    default void sort() {
    }
}
