//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.TestActionEvent;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.picimako.gherkin.ToolWindowTestSupport;
import com.picimako.gherkin.toolwindow.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link SelectFocusedTagAction}.
 */
public class SelectFocusedTagActionNoGroupingTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "testdata/features";
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        //Returning a new project descriptor, so that a new Project is created for each
        // test method, thus having a clean data setup (i.e. Tags tool window) for each test.
        return new LightProjectDescriptor();
    }

    //Availability

    public void testNotAvailableForNoEditorOpen() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.NO_GROUPING;

        var event = doTestActionEvent();
        new SelectFocusedTagAction().updateButton(event);

        assertThat(event.getPresentation().isEnabled()).isFalse();
    }

//    public void testNotAvailableForNoCaret() {
//    }

    public void testNotAvailableForMoreThanOneCaret() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.NO_GROUPING;

        myFixture.configureByFile("A_gherkin.feature");
        //Add a second caret
        FileEditorManager.getInstance(getProject()).getSelectedTextEditor().getCaretModel().addCaret(new VisualPosition(3, 4));

        var event = doTestActionEvent();
        new SelectFocusedTagAction().updateButton(event);

        assertThat(event.getPresentation().isEnabled()).isFalse();
    }

    public void testNotAvailableForNonGherkinTagUnderTheCaret() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.NO_GROUPING;

        myFixture.configureByText(
            "non_tag_under_caret.feature",
            "Feature: Some featur<caret>e");

        var event = doTestActionEvent();
        new SelectFocusedTagAction().updateButton(event);

        assertThat(event.getPresentation().isEnabled()).isFalse();
    }

    public void testAvailableForGherkinTag() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.NO_GROUPING;

        myFixture.configureByText(
            "tag_under_caret.feature",
            "@some<caret>Tag\n" +
                "Feature: Some feature");

        var event = doTestActionEvent();
        new SelectFocusedTagAction().updateButton(event);

        assertThat(event.getPresentation().isEnabled()).isTrue();
    }

    //Performing the action

    public void testSelectsTagInCustomCategoryInProject() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.NO_GROUPING;

        validateTagSelection(
            "@vimeo @smo<caret>ke @JIRA-1234\n" +
                "Feature: Vimeo feature",
            "[Gherkin Tags, Test Suite, smoke]"
        );
    }

    public void testSelectsTagInOtherInProject() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.NO_GROUPING;

        validateTagSelection(
            "@vimeo @smoke @unma<caret>pped\n" +
                "Feature: Vimeo feature",
            "[Gherkin Tags, Other, unmapped]"
        );
    }

    public void testSelectsRegexBasedTagInProject() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.NO_GROUPING;

        validateTagSelection(
            "@vimeo @smoke @JIRA-1<caret>234\n" +
                "Feature: Vimeo feature",
            "[Gherkin Tags, Jira, JIRA-1234]"
        );
    }

    public void testSelectsTagInCustomCategoryInContentRoot() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;

        validateTagSelection(
            "@vimeo @smo<caret>ke @JIRA-1234\n" +
                "Feature: Vimeo feature",
            "[Gherkin Tags, light_idea_test_case, Test Suite, smoke]"
        );
    }

    public void testSelectsTagInOtherInContentRoot() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;

        validateTagSelection(
            "@vimeo @smoke @unma<caret>pped\n" +
                "Feature: Vimeo feature",
            "[Gherkin Tags, light_idea_test_case, Other, unmapped]"
        );
    }

    public void testSelectsRegexBasedTagInContentRoot() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;

        validateTagSelection(
            "@vimeo @smoke @JIRA-1<caret>234\n" +
                "Feature: Vimeo feature",
            "[Gherkin Tags, light_idea_test_case, Jira, JIRA-1234]"
        );
    }

    //Helpers

    private AnActionEvent doTestActionEvent() {
        return TestActionEvent.createTestEvent(dataId -> {
            if (CommonDataKeys.PROJECT.is(dataId)) return getProject();
            return null;
        });
    }

    private void validateTagSelection(String featureFileText, String selectionPathString) {
        myFixture.configureByText("the_gherkin.feature", featureFileText);
        var gherkinTagsPanel = new GherkinTagOverviewPanel(getProject());
        ToolWindowTestSupport.registerToolWindow(gherkinTagsPanel, getProject());

        var event = doTestActionEvent();
        new SelectFocusedTagAction().actionPerformed(event);

        var selectionPath = gherkinTagsPanel.getTree().getSelectionPath();
        assertThat(selectionPath).hasToString(selectionPathString);
    }
}
