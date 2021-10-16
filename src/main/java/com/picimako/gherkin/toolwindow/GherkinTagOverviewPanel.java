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

import java.awt.*;
import javax.swing.*;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiManager;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.JBScrollPane;

import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;

/**
 * A custom panel to display the Gherkin tags and associated data.
 * <p>
 * The underlying tree component is also extended with action listeners, so that e.g. hitting Enter or double-clicking on
 * Gherkin and Story file nodes open the selected file.
 */
public class GherkinTagOverviewPanel extends JPanel {

    private final TreeModelFactory treeModelFactory = new TreeModelFactory();
    private final Project project;
    private GherkinTagTree tree;
    private GherkinTagTreeModel model;

    public GherkinTagOverviewPanel(Project project) {
        this.project = project;
        buildGUI();
        PsiManager.getInstance(project).addPsiTreeChangeListener(new GherkinPsiChangeListener(tree, project), project);
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new FileAndFolderChangeListener(this::rebuildModel, project));
        new TreeSpeedSearch(tree);
    }

    public GherkinTagTree getTree() {
        return tree;
    }

    public ModelDataRoot modelDataRoot() {
        return (ModelDataRoot) model.getRoot();
    }

    /**
     * Updates the underlying model to display a different tree view in the tool window.
     * <p>
     * This is used when the layout type is changed in the tool window.
     * <p>
     * If a given layout has already been used before, this update will simply reuse the corresponding model data,
     * instead of scanning the project again.
     */
    public void updateModel() {
        model = treeModelFactory.createTreeModel(modelDataRoot(), project);
        LayoutType layout = GherkinTagsToolWindowSettings.getInstance(project).layout;
        if ((layout == LayoutType.NO_GROUPING && !modelDataRoot().isProjectDataInitialized())
            || (layout != LayoutType.NO_GROUPING && !modelDataRoot().isContentRootDataInitialized())) {
            model.buildModel();
        }
        tree.setModel(model);
    }

    /**
     * Used when either the application or project-level mappings are changed in the Settings,
     * and when the model has to be rebuilt due to file system changes.
     *
     * @see com.picimako.gherkin.settings.GherkinOverviewProjectConfigurable
     * @see FileAndFolderChangeListener
     */
    public void rebuildModel() {
        model.dispose();
        model.buildModel();
        tree.setModel(model);
        tree.updateUI();
    }

    private void buildGUI() {
        setLayout(new BorderLayout());
        model = treeModelFactory.createTreeModel(project);
        model.buildModel();
        tree = new GherkinTagTree(model);
        tree.addMouseListener(new MouseListeningGherkinFileOpener(project, tree));
        tree.addKeyListener(new KeyboardListeningGherkinFileOpener(project, tree));
        add(new JBScrollPane(tree));
    }
}
