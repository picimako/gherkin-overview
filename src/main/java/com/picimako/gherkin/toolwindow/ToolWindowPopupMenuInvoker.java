//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static com.picimako.gherkin.toolwindow.TreeNodeTypeIdentifier.identifyTreeNodeTypeForClickLocation;

import com.intellij.openapi.actionSystem.ActionPopupMenu;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Map;

/**
 * This class takes in the {@link GherkinTagTree} instance and a set of {@link ActionPopupMenu}s identified by their node
 * types, and for any selected node, it displays the proper action popup menu.
 */
public final class ToolWindowPopupMenuInvoker {
    private final GherkinTagTree tree;
    private final Map<String, ActionPopupMenu> actionPopupMenus;

    public ToolWindowPopupMenuInvoker(@NotNull GherkinTagTree tree, @NotNull Map<String, ActionPopupMenu> actionPopupMenus) {
        this.tree = tree;
        this.actionPopupMenus = actionPopupMenus;
    }

    //Invokes the context menu only when it is initiated on the proper node type
    public void invokePopup(Component comp, int x, int y) {
        String nodeTypeToSelect = identifyTreeNodeTypeForClickLocation(tree, x, y);
        if (nodeTypeToSelect != null) {
            actionPopupMenus.get(nodeTypeToSelect).getComponent().show(comp, x, y);
        }
    }
}
