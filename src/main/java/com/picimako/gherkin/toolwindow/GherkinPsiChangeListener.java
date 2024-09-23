//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static com.picimako.gherkin.toolwindow.GherkinTagToolWindowUtil.getGherkinTagsToolWindow;
import static com.picimako.gherkin.toolwindow.GherkinTagToolWindowUtil.getToolWindowHider;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;

import com.picimako.gherkin.BDDUtil;
import com.picimako.gherkin.JBehaveStoryService;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;

/**
 * Handles PSI changes in Gherkin files.
 * <p>
 * If a Gherkin/Story file changes, this listener updates the calls updates on the model data and UI of the Gherkin tag tool window
 * according to the changes in the file.
 */
final class GherkinPsiChangeListener extends PsiTreeChangeAdapter {

    private final GherkinTagTree tree;
    private final Project project;
    private final JBehaveStoryService storyService;

    GherkinPsiChangeListener(GherkinTagTree tree, Project project) {
        this.tree = tree;
        this.project = project;
        storyService = project.getService(JBehaveStoryService.class);
    }

    @Override
    public void childrenChanged(@NotNull final PsiTreeChangeEvent event) {
        updateGherkinTree(event);
    }

    @Override
    public void childAdded(@NotNull PsiTreeChangeEvent event) {
        updateGherkinTree(event);
    }

    @Override
    public void childMoved(@NotNull PsiTreeChangeEvent event) {
        updateGherkinTree(event);
    }

    @Override
    public void childRemoved(@NotNull PsiTreeChangeEvent event) {
        updateGherkinTree(event);
    }

    @Override
    public void childReplaced(@NotNull PsiTreeChangeEvent event) {
        updateGherkinTree(event);
    }

    @Override
    public void propertyChanged(@NotNull PsiTreeChangeEvent event) {
        updateGherkinTree(event);
    }

    /**
     * When the file still exists, the update call is made for the file itself, while when it is null, meaning
     * it has been deleted, the update call happens to the child of the event, which stores the file that has
     * been deleted.
     */
    private void updateGherkinTree(PsiTreeChangeEvent event) {
        PsiFile file = event.getFile();
        //file is null when the file has just been deleted
        if (file != null && BDDUtil.isABDDFile(file)) {
            updateModelAndToolWindow(file);
        } else if (file == null) {
            if (event.getChild() instanceof GherkinFile gherkinFile) {
                updateModelAndToolWindow(gherkinFile);
            } else if (event.getChild() instanceof PsiFile psiFile && storyService.isJBehaveStoryFile(psiFile)) {
                updateModelAndToolWindow(storyService.asStoryFile(psiFile));
            }
        }
    }

    /**
     * Model is updated only if the Gherkin tag tool window is actually available.
     */
    private void updateModelAndToolWindow(PsiFile file) {
        ToolWindow gherkinTagsToolWindow = getGherkinTagsToolWindow(project);
        if (gherkinTagsToolWindow != null) {
            ((GherkinTagTreeModel) tree.getModel()).updateModelForFile(file);

            ModelDataRoot modelRoot = (ModelDataRoot) tree.getModel().getRoot();
            modelRoot.sort();
            tree.updateUI();

            getToolWindowHider(gherkinTagsToolWindow).setContentVisibilityBasedOn(modelRoot);
        }
    }
}
