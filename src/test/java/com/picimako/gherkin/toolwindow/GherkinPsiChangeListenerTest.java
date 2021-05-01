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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.swing.*;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiTreeChangeEventImpl;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.ui.content.Content;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;

/**
 * Unit test for {@link GherkinPsiChangeListener}.
 */
public class GherkinPsiChangeListenerTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "testdata/features";
    }

    public void testUpdatesModel() {
        registerToolWindow();
        PsiFile gherkinFile = myFixture.configureByFile("the_gherkin.feature");
        GherkinTagTree tree = mock(GherkinTagTree.class);
        GherkinTagTreeModel model = mock(ContentRootBasedGherkinTagTreeModel.class);
        ModelDataRoot modelRoot = mock(ModelDataRoot.class);
        when(tree.getModel()).thenReturn(model);
        when(model.getRoot()).thenReturn(modelRoot);

        firePsiEvent(gherkinFile, tree);

        verify(model).updateModelForFile(gherkinFile);
        verify(modelRoot).sort();
        verify(tree).updateUI();
    }

    public void testDoesntUpdateModelForNoFile() {
        GherkinTagTree tree = mock(GherkinTagTree.class);

        firePsiEvent(null, tree);

        verify(tree, never()).getModel();
    }

    public void testDoesntUpdateModelForNonGherkinFile() {
        PsiFile jsFile = myFixture.configureByFile("some_js_file.js");
        GherkinTagTree tree = mock(GherkinTagTree.class);

        firePsiEvent(jsFile, tree);

        verify(tree, never()).getModel();
    }

    public void testDoesntUpdateModelWhenGherkinTagsToolWindowIsNotRegistered() {
        PsiFile gherkinFile = myFixture.configureByFile("the_gherkin.feature");
        GherkinTagTree tree = mock(GherkinTagTree.class);

        firePsiEvent(gherkinFile, tree);

        verify(tree, never()).getModel();
    }

    public void testUpdatesModelForDeletedFile() {
        registerToolWindow();
        PsiFile gherkinFile = myFixture.configureByFile("the_gherkin.feature");
        PsiFile childFile = myFixture.configureByFile("a_gherkin.feature");
        GherkinTagTree tree = mock(GherkinTagTree.class);
        GherkinTagTreeModel model = mock(ContentRootBasedGherkinTagTreeModel.class);
        ModelDataRoot modelRoot = mock(ModelDataRoot.class);
        when(tree.getModel()).thenReturn(model);
        when(model.getRoot()).thenReturn(modelRoot);

        WriteAction.run(() -> gherkinFile.delete());

        var listener = new GherkinPsiChangeListener(tree, getProject());
        var event = new PsiTreeChangeEventImpl(PsiManager.getInstance(getProject()));
        event.setChild(childFile);

        listener.childrenChanged(event);

        verify(model).updateModelForFile(childFile);
        verify(modelRoot).sort();
        verify(tree).updateUI();
    }

    private void firePsiEvent(PsiFile gherkinFile, GherkinTagTree tree) {
        var listener = new GherkinPsiChangeListener(tree, getProject());
        var event = new PsiTreeChangeEventImpl(PsiManager.getInstance(getProject()));
        event.setFile(gherkinFile);

        listener.childrenChanged(event);
    }

    private void registerToolWindow() {
        ToolWindow toolWindow = ToolWindowManager.getInstance(getProject())
            .registerToolWindow(
                new RegisterToolWindowTask("Gherkin Tags",
                    ToolWindowAnchor.LEFT,
                    new GherkinTagToolWindowHider(new JPanel(), getProject()),
                    true, true, false, true,
                    new GherkinTagToolWindowFactory(),
                    null, null));
        Content content = toolWindow.getContentManager().getFactory()
            .createContent(new GherkinTagToolWindowHider(new JPanel(), getProject()), "", true);
        toolWindow.getContentManager().addContent(content);
    }
}
