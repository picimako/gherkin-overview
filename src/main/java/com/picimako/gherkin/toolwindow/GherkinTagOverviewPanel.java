//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiManager;
import com.intellij.ui.TreeUIHelper;
import com.intellij.ui.components.JBScrollPane;
import com.picimako.gherkin.toolwindow.action.TagActionsGroup;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * A custom panel to display the Gherkin tags and associated data.
 * <p>
 * The underlying tree component is also extended with action listeners, so that e.g. hitting Enter or double-clicking on
 * Gherkin and Story file nodes open the selected file.
 */
public final class GherkinTagOverviewPanel extends JPanel {

    private static final String TAG_ACTIONS_GROUP = "gherkin.overview.tag.TagActionsGroup";
    private final TreeModelFactory treeModelFactory = new TreeModelFactory();
    private final Project project;
    @Getter
    private GherkinTagTree tree;
    private GherkinTagTreeModel model;

    public GherkinTagOverviewPanel(Project project) {
        this.project = project;
        buildGUI();
        //Since Project type objects are not allowed to be used as parent disposable, using a light service instead, which is disposed automatically
        //when implementing the Disposable interface.
        //see: https://plugins.jetbrains.com/docs/intellij/disposers.html#automatically-disposed-objects
        //see: https://plugins.jetbrains.com/docs/intellij/disposers.html#choosing-a-disposable-parent
        PsiManager.getInstance(project).addPsiTreeChangeListener(new GherkinPsiChangeListener(tree, project), OverviewPanelDisposalService.getInstance(project));
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new FileAndFolderChangeListener(this::rebuildModel, project));
        TreeUIHelper.getInstance().installTreeSpeedSearch(tree);
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
        if ((layout == LayoutType.NO_GROUPING && !modelDataRoot().isInitializedAsProjectData())
            || (layout != LayoutType.NO_GROUPING && !modelDataRoot().isInitializedAsContentRootData())) {
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
        tree = new GherkinTagTree(model, project);
        registerContextMenuActions();
        new MouseListeningGherkinFileOpener(project, tree).installOn(tree);
        tree.addKeyListener(new KeyboardListeningGherkinFileOpener(project, tree));
        add(new JBScrollPane(tree));
    }

    private void registerContextMenuActions() {
        ActionManager actionManager = ActionManager.getInstance();

        //List of action groups to register
        //Since ActionManager seems to be operating on application level, and not project level, this check makes sure
        //that in case multiple projects are open, this tool window doesn't try to register the same action group twice,
        //which would lead to exception thrown, and to a crashing tool window.
        if (actionManager.getAction(TAG_ACTIONS_GROUP) == null) {
            actionManager.registerAction(TAG_ACTIONS_GROUP, new TagActionsGroup());
        }
        //Add action popup menu to the tree component
        var actionPopupMenu = actionManager
            .createActionPopupMenu("GherkinTagToolWindow", (DefaultActionGroup) actionManager.getAction(TAG_ACTIONS_GROUP));
        actionPopupMenu.setTargetComponent(tree);
        final var actionPopupMenus = Map.of("Tag", actionPopupMenu);

        tree.addMouseListener(new MouseListeningPopupMenuInvoker(new ToolWindowPopupMenuInvoker(tree, actionPopupMenus)));
    }
}
