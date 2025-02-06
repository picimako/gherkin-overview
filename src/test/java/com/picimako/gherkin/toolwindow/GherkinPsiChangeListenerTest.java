//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static com.picimako.gherkin.ToolWindowTestSupport.registerToolWindow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.intellij.openapi.application.WriteAction;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiTreeChangeEventImpl;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;

/**
 * Unit test for {@link GherkinPsiChangeListener}.
 */
public class GherkinPsiChangeListenerTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "testdata/features";
    }

    public void testUpdatesModelForGherkinFile() {
        registerToolWindow(getProject());
        PsiFile gherkinFile = myFixture.configureByFile("the_gherkin.feature");
        GherkinTagTree tree = mock();
        GherkinTagTreeModel model = mock();
        ModelDataRoot modelRoot = mock();
        when(tree.getModel()).thenReturn(model);
        when(model.getRoot()).thenReturn(modelRoot);

        firePsiEvent(gherkinFile, tree);

        verify(model).updateModelForFile(gherkinFile);
        verify(modelRoot).sort();
        verify(tree).updateUI();
    }

    public void testUpdatesModelForStoryFile() {
        registerToolWindow(getProject());
        PsiFile storyFile = myFixture.configureByText("story.story", "");
        GherkinTagTree tree = mock(GherkinTagTree.class);
        GherkinTagTreeModel model = mock(ContentRootBasedGherkinTagTreeModel.class);
        ModelDataRoot modelRoot = mock(ModelDataRoot.class);
        when(tree.getModel()).thenReturn(model);
        when(model.getRoot()).thenReturn(modelRoot);

        firePsiEvent(storyFile, tree);

        verify(model).updateModelForFile(storyFile);
        verify(modelRoot).sort();
        verify(tree).updateUI();
    }

    public void testDoesntUpdateModelForNoFile() {
        GherkinTagTree tree = mock(GherkinTagTree.class);

        firePsiEvent(null, tree);

        verify(tree, never()).getModel();
    }

    public void testDoesntUpdateModelForNonBDDFile() {
        PsiFile jsFile = myFixture.configureByFile("some_js_file.js");
        GherkinTagTree tree = mock(GherkinTagTree.class);

        firePsiEvent(jsFile, tree);

        verify(tree, never()).getModel();
    }

    public void testDoesntUpdateModelWhenGherkinTagsToolWindowIsNotRegistered() {
        myFixture.configureByFile("the_gherkin.feature");
        GherkinTagTree tree = mock(GherkinTagTree.class);

        verify(tree, never()).getModel();
    }

    public void testUpdatesModelForDeletedFile() {
        registerToolWindow(getProject());
        PsiFile gherkinFile = myFixture.configureByFile("the_gherkin.feature");
        PsiFile childFile = myFixture.configureByFile("a_gherkin.feature");
        GherkinTagTree tree = mock(GherkinTagTree.class);
        GherkinTagTreeModel model = mock(ContentRootBasedGherkinTagTreeModel.class);
        ModelDataRoot modelRoot = mock(ModelDataRoot.class);
        when(tree.getModel()).thenReturn(model);
        when(model.getRoot()).thenReturn(modelRoot);

        WriteAction.run(gherkinFile::delete);

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
}
