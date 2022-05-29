//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

import com.intellij.openapi.Disposable;

/**
 * Node type for the elements of the Gherkin tree.
 */
public interface NodeType extends Sortable, Disposable {

    /**
     * Returns the argument object as a {@link ContentRoot}.
     */
    static ContentRoot asContentRoot(Object node) {
        return (ContentRoot) node;
    }

    /**
     * Returns the argument object as a {@link Category}.
     */
    static Category asCategory(Object node) {
        return (Category) node;
    }

    /**
     * Returns the argument object as a {@link Tag}.
     */
    static Tag asTag(Object node) {
        return (Tag) node;
    }

    @Override
    default void dispose() {
    }
}
