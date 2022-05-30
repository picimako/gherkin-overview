//Copyright 2022 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.action;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.TestActionEvent;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.picimako.gherkin.toolwindow.GherkinTagTree;
import com.picimako.gherkin.toolwindow.ProjectSpecificGherkinTagTreeModel;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;

import javax.swing.tree.TreePath;

/**
 * Functional test for {@link DeleteAllTagOccurrencesAction}.
 */
public class DeleteAllTagOccurrencesActionTest extends BasePlatformTestCase {

    private GherkinTagTree tree;

    @Override
    protected String getTestDataPath() {
        return "testdata/features";
    }

    //Tags

    public void testDeletesSingleTagFromSingleFile() {
        myFixture.configureByFile("delete_tag.feature");

        initGherkinTagTreeAndSetSelectionTo("vimeo");
        new DeleteAllTagOccurrencesAction(getProject()).actionPerformed(doTestActionEvent());
        myFixture.checkResult(
            "@e2e @regression @youtube @desktop @youtube @sitemap @JIRA-1234\n" +
                "Feature: Videos\n" +
                "\n" +
                "  @tablet\n" +
                "  Scenario: Video components");
    }

    public void testDeletesMultipleTagsFromSingleFile() {
        myFixture.configureByFile("delete_tag.feature");

        initGherkinTagTreeAndSetSelectionTo("youtube");
        new DeleteAllTagOccurrencesAction(getProject()).actionPerformed(doTestActionEvent());
        myFixture.checkResult(
            "@e2e @regression  @desktop @sitemap @JIRA-1234\n" +
                "Feature: Videos\n" +
                "\n" +
                "  @vimeo @tablet\n" +
                "  Scenario: Video components\n");
    }

    public void testDeletesMultipleTagsFromMultipleFiles() {
        myFixture.configureByFile("delete_tag.feature");
        var delete_tag_more = PsiManager.getInstance(getProject()).findFile(myFixture.copyFileToProject("delete_tag_more.feature"));

        initGherkinTagTreeAndSetSelectionTo("youtube");
        new DeleteAllTagOccurrencesAction(getProject()).actionPerformed(doTestActionEvent());
        myFixture.checkResult(
            "@e2e @regression  @desktop @sitemap @JIRA-1234\n" +
                "Feature: Videos\n" +
                "\n" +
                "  @vimeo @tablet\n" +
                "  Scenario: Video components\n");
        assertThat(delete_tag_more.getText()).isEqualTo(
            "@e2e @regression  @desktop @sitemap @JIRA-1234\n" +
                "Feature: Videos\n" +
                "\n" +
                "  @vimeo @tablet\n" +
                "  Scenario: Video components\n");
    }

    //Metas

    public void testDeletesSingleMetaKeyFromSingleFile() {
        myFixture.configureByFile("delete_tag.story");

        initGherkinTagTreeAndSetSelectionTo("Single");
        new DeleteAllTagOccurrencesAction(getProject()).actionPerformed(doTestActionEvent());
        myFixture.checkResult(
            "Meta:\n" +
                "@Single key\n" +
                "@Media youtube vimeo\n" +
                "Scenario: scenario 1\n" +
                "\n" +
                "Meta:\n" +
                "@Device tablet\n" +
                "@Media youtube vimeo\n" +
                "Scenario: scenario 1\n" +
                "\n" +
                "Meta:\n" +
                "@Device tablet\n" +
                "@Media youtube vimeo\n" +
                "Scenario: scenario 1\n");
    }

    public void testDeletesSingleMetaKeyAndTextFromSingleFile() {
        myFixture.configureByFile("delete_tag.story");

        initGherkinTagTreeAndSetSelectionTo("Single:key");
        new DeleteAllTagOccurrencesAction(getProject()).actionPerformed(doTestActionEvent());
        myFixture.checkResult(
            "Meta:\n" +
                "@Single\n" +
                "@Media youtube vimeo\n" +
                "Scenario: scenario 1\n" +
                "\n" +
                "Meta:\n" +
                "@Device tablet\n" +
                "@Media youtube vimeo\n" +
                "Scenario: scenario 1\n" +
                "\n" +
                "Meta:\n" +
                "@Device tablet\n" +
                "@Media youtube vimeo\n" +
                "Scenario: scenario 1\n");
    }

    public void testDeletesMultipleMetaKeyAndTextFromSingleFile() {
        myFixture.configureByFile("delete_tag.story");

        initGherkinTagTreeAndSetSelectionTo("Device:tablet");
        new DeleteAllTagOccurrencesAction(getProject()).actionPerformed(doTestActionEvent());
        myFixture.checkResult(
            "Meta:\n" +
                "@Single\n" +
                "@Single key\n" +
                "@Media youtube vimeo\n" +
                "Scenario: scenario 1\n" +
                "\n" +
                "Meta:\n" +
                "@Media youtube vimeo\n" +
                "Scenario: scenario 1\n" +
                "\n" +
                "Meta:\n" +
                "@Media youtube vimeo\n" +
                "Scenario: scenario 1\n");
    }

    public void testDeletesMultipleMetasFromSingleFile() {
        myFixture.configureByFile("delete_tag.story");

        initGherkinTagTreeAndSetSelectionTo("Media:youtube vimeo");
        new DeleteAllTagOccurrencesAction(getProject()).actionPerformed(doTestActionEvent());
        myFixture.checkResult(
            "Meta:\n" +
                "@Single\n" +
                "@Single key\n" +
                "Scenario: scenario 1\n" +
                "\n" +
                "Meta:\n" +
                "@Device tablet\n" +
                "Scenario: scenario 1\n" +
                "\n" +
                "Meta:\n" +
                "@Device tablet\n" +
                "Scenario: scenario 1\n");
    }

    public void testDeletesMultipleMetasFromMultipleFiles() {
        myFixture.configureByFile("delete_tag.story");
        var delete_tag_more = PsiManager.getInstance(getProject()).findFile(myFixture.copyFileToProject("delete_tag_more.story"));

        initGherkinTagTreeAndSetSelectionTo("Media:youtube vimeo");
        new DeleteAllTagOccurrencesAction(getProject()).actionPerformed(doTestActionEvent());
        myFixture.checkResult(
            "Meta:\n" +
                "@Single\n" +
                "@Single key\n" +
                "Scenario: scenario 1\n" +
                "\n" +
                "Meta:\n" +
                "@Device tablet\n" +
                "Scenario: scenario 1\n" +
                "\n" +
                "Meta:\n" +
                "@Device tablet\n" +
                "Scenario: scenario 1\n");
        assertThat(delete_tag_more.getText()).isEqualTo(
            "Meta:\n" +
                "@Single\n" +
                "@Single key\n" +
                "Scenario: scenario 1\n" +
                "\n" +
                "Meta:\n" +
                "@Device tablet\n" +
                "Scenario: scenario 1\n" +
                "\n" +
                "Meta:\n" +
                "@Device tablet\n" +
                "Scenario: scenario 1\n");
    }

    private void initGherkinTagTreeAndSetSelectionTo(String tagName) {
        var model = new ProjectSpecificGherkinTagTreeModel(getProject());
        model.buildModel();
        tree = new GherkinTagTree(model);
        tree.setSelectionPath(new TreePath(((ModelDataRoot) tree.getModel().getRoot()).getOther().get(tagName).get()));
    }

    private TestActionEvent doTestActionEvent() {
        return new TestActionEvent(dataId -> {
            if (CommonDataKeys.PROJECT.is(dataId)) return getProject();
            if (PlatformDataKeys.CONTEXT_COMPONENT.is(dataId)) return tree;
            return null;
        });
    }
}
