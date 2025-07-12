//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.action;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.TestActionEvent;
import com.picimako.gherkin.GherkinOverviewTestBase;
import com.picimako.gherkin.ToolWindowTestSupport;
import com.picimako.gherkin.toolwindow.GherkinTagOverviewPanel;
import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;
import com.picimako.gherkin.toolwindow.LayoutType;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link SelectFocusedTagAction}.
 */
final class SelectFocusedTagActionNoGroupingTest extends GherkinOverviewTestBase {

    //Returning a new project descriptor, so that a new Project is created for each
    // test method, thus having a clean data setup (i.e. Tags tool window) for each test.
    public SelectFocusedTagActionNoGroupingTest() {
        super(new LightProjectDescriptor());
    }

    //Availability

    @Test
    void notAvailableForNoEditorOpen() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.NO_GROUPING;

        var event = updateActionButton();

        assertThat(event.getPresentation().isEnabled()).isFalse();
    }

//    @Test
//    void notAvailableForNoCaret() {
//    }

    @Test
    void notAvailableForMoreThanOneCaret() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.NO_GROUPING;

        configureByFile("A_gherkin.feature");
        //Add a second caret
        invokeAndWait(() ->
            FileEditorManager.getInstance(getProject()).getSelectedTextEditor()
                .getCaretModel()
                .addCaret(new VisualPosition(3, 4)));

        var event = updateActionButton();

        assertThat(event.getPresentation().isEnabled()).isFalse();
    }

    @Test
    void notAvailableForNonGherkinTagUnderTheCaret() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.NO_GROUPING;

        configureByText(
            "non_tag_under_caret.feature",
            "Feature: Some featur<caret>e");

        var event = updateActionButton();

        assertThat(event.getPresentation().isEnabled()).isFalse();
    }

    @Test
    void availableForGherkinTag() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.NO_GROUPING;

        configureByText(
            "tag_under_caret.feature",
            "@some<caret>Tag\n" +
                "Feature: Some feature");

        var event = updateActionButton();

        assertThat(event.getPresentation().isEnabled()).isTrue();
    }

    //Performing the action

    @Test
    void selectsTagInCustomCategoryInProject() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.NO_GROUPING;

        validateTagSelection(
            "@vimeo @smo<caret>ke @JIRA-1234\n" +
                "Feature: Vimeo feature",
            "[Gherkin Tags, Test Suite, smoke]"
        );
    }

    @Test
    void selectsTagInOtherInProject() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.NO_GROUPING;

        validateTagSelection(
            "@vimeo @smoke @unma<caret>pped\n" +
                "Feature: Vimeo feature",
            "[Gherkin Tags, Other, unmapped]"
        );
    }

    @Test
    void selectsRegexBasedTagInProject() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.NO_GROUPING;

        validateTagSelection(
            "@vimeo @smoke @JIRA-1<caret>234\n" +
                "Feature: Vimeo feature",
            "[Gherkin Tags, Jira, JIRA-1234]"
        );
    }

    @Test
    void selectsTagInCustomCategoryInContentRoot() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;

        validateTagSelection(
            "@vimeo @smo<caret>ke @JIRA-1234\n" +
                "Feature: Vimeo feature",
            "[Gherkin Tags, light_idea_test_case, Test Suite, smoke]"
        );
    }

    @Test
    void selectsTagInOtherInContentRoot() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;

        validateTagSelection(
            "@vimeo @smoke @unma<caret>pped\n" +
                "Feature: Vimeo feature",
            "[Gherkin Tags, light_idea_test_case, Other, unmapped]"
        );
    }

    @Test
    void selectsRegexBasedTagInContentRoot() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;

        validateTagSelection(
            "@vimeo @smoke @JIRA-1<caret>234\n" +
                "Feature: Vimeo feature",
            "[Gherkin Tags, light_idea_test_case, Jira, JIRA-1234]"
        );
    }

    //Helpers

    private AnActionEvent updateActionButton() {
        var event = doTestActionEvent();
        new SelectFocusedTagAction().updateButton(event);
        return event;
    }

    private AnActionEvent doTestActionEvent() {
        return TestActionEvent.createTestEvent(dataId -> {
            if (CommonDataKeys.PROJECT.is(dataId)) return getProject();
            return null;
        });
    }

    private void validateTagSelection(String featureFileText, String selectionPathString) {
        configureByText("the_gherkin.feature", featureFileText);
        var gherkinTagsPanel = new GherkinTagOverviewPanel(getProject());
        ToolWindowTestSupport.registerToolWindow(gherkinTagsPanel, getProject());

        var event = doTestActionEvent();
        invokeAndWait(() -> new SelectFocusedTagAction().actionPerformed(event));

        var selectionPath = gherkinTagsPanel.getTree().getSelectionPath();
        assertThat(selectionPath).hasToString(selectionPathString);
    }
}
