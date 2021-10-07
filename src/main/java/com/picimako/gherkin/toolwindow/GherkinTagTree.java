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

import static com.picimako.gherkin.GherkinUtil.isGherkinFile;
import static com.picimako.gherkin.toolwindow.nodetype.NodeType.asContentRoot;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import com.github.kumaraman21.intellijbehave.language.JBehaveIcons;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.util.PlatformIcons;

import com.picimako.gherkin.toolwindow.nodetype.Category;
import com.picimako.gherkin.toolwindow.nodetype.ContentRoot;
import com.picimako.gherkin.toolwindow.nodetype.FeatureFile;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import com.picimako.gherkin.toolwindow.nodetype.Tag;
import icons.CucumberIcons;
import org.jetbrains.annotations.NotNull;

/**
 * A custom JTree implementation for rendering the elements of the Gherkin tag tree.
 */
public class GherkinTagTree extends JTree {

    public GherkinTagTree(TreeModel model) {
        super(model);
        setCellRenderer(new GherkinTagsNodeRenderer());
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setExpandsSelectedPaths(true);
    }

    /**
     * Node renderer for the Gherkin tag tree.
     * <p>
     * Configures the icons of nodes according to their node types.
     */
    static final class GherkinTagsNodeRenderer extends NodeRenderer {
        @Override
        public void customizeCellRenderer(@NotNull JTree tree, @NlsSafe Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus);

            if (value instanceof ModelDataRoot) {
                setIcon(PlatformIcons.FOLDER_ICON);
            } else if (value instanceof ContentRoot) {
                setIcon(asContentRoot(value).getIcon());
            } else if (value instanceof Category) {
                setIcon(PlatformIcons.LIBRARY_ICON);
            } else if (value instanceof Tag) {
                setIcon(AllIcons.Gutter.ExtAnnotation);
            } else if (value instanceof FeatureFile) {
                setIcon(isGherkinFile(((FeatureFile) value).getFile()) ? CucumberIcons.Cucumber : JBehaveIcons.JB);
            }
        }
    }
}
