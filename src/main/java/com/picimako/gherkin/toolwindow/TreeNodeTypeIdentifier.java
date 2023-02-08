//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import com.picimako.gherkin.toolwindow.nodetype.Tag;

import javax.swing.tree.TreePath;

/**
 * Utility class to retrieve the node type selected in the Gherkin Tag tool window.
 */
final class TreeNodeTypeIdentifier {

    /**
     * Gets the node type as String, for the selected node in the Gherkin Tag tool window.
     *
     * @param tree the Gherkin Tg tree
     * @param x    the x coordinate where the click happened in the tree
     * @param y    the y coordinate where the click happened in the tree
     * @return "Tag" for tag nodes
     */
    static String identifyTreeNodeTypeForClickLocation(GherkinTagTree tree, int x, int y) {
        String itemToSelect = null;
        TreePath path = tree.getPathForLocation(x, y);
        if (path != null) {
            tree.setSelectionPath(path);
            int selectionRow = tree.getRowForLocation(x, y);
            if (selectionRow > -1) {
                tree.setSelectionRow(selectionRow);
            }
            //Indicates that this particular action popup should be displayed only for a Tag node in the tree
            if (path.getLastPathComponent() instanceof Tag) {
                itemToSelect = "Tag";
            }
        }
        return itemToSelect;
    }

    private TreeNodeTypeIdentifier() {
        //Utility class
    }
}
