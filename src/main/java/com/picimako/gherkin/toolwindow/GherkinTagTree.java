//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static com.picimako.gherkin.GherkinUtil.isGherkinFile;
import static com.picimako.gherkin.toolwindow.nodetype.NodeType.asContentRoot;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.util.PlatformIcons;
import com.picimako.gherkin.JBehaveStoryService;
import icons.CucumberIcons;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import com.picimako.gherkin.toolwindow.nodetype.Category;
import com.picimako.gherkin.toolwindow.nodetype.ContentRoot;
import com.picimako.gherkin.toolwindow.nodetype.FeatureFile;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import com.picimako.gherkin.toolwindow.nodetype.Tag;

/**
 * A custom JTree implementation for rendering the elements of the Gherkin tag tree.
 */
public final class GherkinTagTree extends JTree {

    public GherkinTagTree(TreeModel model, Project project) {
        super(model);
        setCellRenderer(new GherkinTagsNodeRenderer(project));
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setExpandsSelectedPaths(true);
    }

    /**
     * Node renderer for the Gherkin tag tree.
     * <p>
     * Configures the icons of nodes according to their node types.
     */
    @RequiredArgsConstructor
    static final class GherkinTagsNodeRenderer extends NodeRenderer {
        private final transient Project project;

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
                setIcon(isGherkinFile(((FeatureFile) value).getFile()) ? CucumberIcons.Cucumber : project.getService(JBehaveStoryService.class).getJBehaveIcon());
            }
        }
    }
}
