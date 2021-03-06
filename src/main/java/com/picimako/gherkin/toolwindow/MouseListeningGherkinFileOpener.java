/*
 * Copyright 2021 Tamás Balog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.picimako.gherkin.toolwindow;

import static javax.swing.SwingUtilities.isLeftMouseButton;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.tree.TreePath;

import com.intellij.openapi.project.Project;
import com.picimako.gherkin.toolwindow.nodetype.FeatureFile;

/**
 * Listens to mouse events, so that in case of the proper button combinations, the Gherkin file selected in the tool
 * window would open.
 * <p>
 * Currently the open action is bound to the double-click event.
 */
public class MouseListeningGherkinFileOpener extends MouseAdapter {

    private final Project project;
    private final GherkinTagTree tree;

    public MouseListeningGherkinFileOpener(Project project, GherkinTagTree tree) {
        this.project = project;
        this.tree = tree;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (isLeftMouseButton(e) && e.getClickCount() == 2 && isGherkinFileAtClickLocation(tree, e.getX(), e.getY())) {
            FileOpener.openFile(((FeatureFile) tree.getLastSelectedPathComponent()).getFile(), project);
        }
    }

    /**
     * Gets whether the currently selected node (after selecting the node) in the tool window is a FeatureFile.
     *
     * @param tree the tree component in the tool window
     * @param x    the x coordinate where the click happened in the tree
     * @param y    the y coordinate where the click happened in the tree
     * @return true if the selected item is a FeatureFile, false otherwise
     */
    private boolean isGherkinFileAtClickLocation(GherkinTagTree tree, int x, int y) {
        TreePath path = tree.getPathForLocation(x, y);
        if (path != null) {
            tree.setSelectionPath(path);
            int selectionRow = tree.getRowForLocation(x, y);
            if (selectionRow > -1) {
                tree.setSelectionRow(selectionRow);
            }
            return path.getLastPathComponent() instanceof FeatureFile;
        }
        return false;
    }
}
