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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

/**
 * Handles PSI changes in Gherkin files.
 * <p>
 * If a Gherkin file changes, this listener updates the calls updates on the model data and UI of the Gherkin tag tool window
 * according to the changes in the file.
 */
public class GherkinPsiChangeListener extends PsiTreeChangeAdapter {

    private final GherkinTagTree tree;
    private final Project project;

    public GherkinPsiChangeListener(GherkinTagTree tree, Project project) {
        this.tree = tree;
        this.project = project;
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
     * When the file still exists the update call is made for the file itself, while when it is null, meaning
     * it has been deleted, the update call happens to the child of the event, which stores the file that has
     * been deleted.
     */
    private void updateGherkinTree(PsiTreeChangeEvent event) {
        PsiFile file = event.getFile();
        //file is null when the file has just been deleted
        if (file != null && file.getFileType() == GherkinFileType.INSTANCE) {
            updateModelAndToolWindow(file);
        } else if (file == null && event.getChild() instanceof GherkinFile) {
            updateModelAndToolWindow((GherkinFile) event.getChild());
        }
    }

    private void updateModelAndToolWindow(PsiFile file) {
        ToolWindow gherkinTagsToolWindow = ToolWindowManager.getInstance(project).getToolWindow("Gherkin Tags");
        if (gherkinTagsToolWindow != null) {
            ((GherkinTagTreeModel) tree.getModel()).updateModelForFile(file);

            ModelDataRoot modelRoot = (ModelDataRoot) tree.getModel().getRoot();
            modelRoot.sort();
            tree.updateUI();

            ((GherkinTagToolWindowHider) gherkinTagsToolWindow
                .getContentManager()
                .getContent(0) //This works as long as "Gherkin Tags" is the first content in the tool window
                .getComponent()
            ).setContentVisibilityBasedOn(modelRoot);
        }
    }
}
