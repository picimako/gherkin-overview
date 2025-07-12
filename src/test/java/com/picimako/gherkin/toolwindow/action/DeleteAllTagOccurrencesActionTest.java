//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.action;

import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.tree.TreePath;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.TestActionEvent;
import com.picimako.gherkin.GherkinOverviewTestBase;
import com.picimako.gherkin.toolwindow.GherkinTagTree;
import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;
import com.picimako.gherkin.toolwindow.LayoutType;
import com.picimako.gherkin.toolwindow.ProjectSpecificGherkinTagTreeModel;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link DeleteAllTagOccurrencesAction}.
 */
final class DeleteAllTagOccurrencesActionTest extends GherkinOverviewTestBase {

    private GherkinTagTree tree;

    @BeforeEach
    void setUp() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.NO_GROUPING;
    }

    @AfterEach
    void tearDown() {
        tree = null;
    }

    //Tags

    @Test
    void deletesSingleTagFromSingleFile() {
        initBDDFileAndTreeWithSelectedNodeToDelete("delete_tag.feature", "vimeo");

        new DeleteAllTagOccurrencesAction(getProject()).actionPerformed(doTestActionEvent());

        getFixture().checkResult(
            """
                @e2e @regression @youtube @desktop @sitemap @JIRA-1234
                Feature: Videos

                  @tablet @youtube
                  Scenario: Video components
                """);
    }

    @Test
    void deletesMultipleTagsFromSingleFile() {
        initBDDFileAndTreeWithSelectedNodeToDelete("delete_tag.feature", "youtube");

        new DeleteAllTagOccurrencesAction(getProject()).actionPerformed(doTestActionEvent());

        getFixture().checkResult(
            """
                @e2e @regression  @desktop @sitemap @JIRA-1234
                Feature: Videos

                  @vimeo @tablet
                  Scenario: Video components
                """);
    }

    @Test
    void deletesMultipleTagsFromMultipleFiles() {
        VirtualFile file = getFixture().copyFileToProject("delete_tag_more.feature");
        var delete_tag_more = findPsiFile(file);
        initBDDFileAndTreeWithSelectedNodeToDelete("delete_tag.feature", "youtube");

        new DeleteAllTagOccurrencesAction(getProject()).actionPerformed(doTestActionEvent());

        getFixture().checkResult(
            """
                @e2e @regression  @desktop @sitemap @JIRA-1234
                Feature: Videos

                  @vimeo @tablet
                  Scenario: Video components
                """);
        assertThat(delete_tag_more.getText()).isEqualTo(
            """
                @e2e @regression  @desktop @sitemap @JIRA-1234
                Feature: Videos

                  @vimeo @tablet
                  Scenario: Video components
                """);
    }

    //Metas

    @Test
    void deletesSingleMetaKeyFromSingleFile() {
        initBDDFileAndTreeWithSelectedNodeToDelete("delete_tag.story", "Single");

        new DeleteAllTagOccurrencesAction(getProject()).actionPerformed(doTestActionEvent());

        getFixture().checkResult(
            """
                Meta:
                @Single key
                @Media youtube vimeo
                Scenario: scenario 1

                Meta:
                @Device tablet
                @Media youtube vimeo
                Scenario: scenario 1

                Meta:
                @Device tablet
                @Media youtube vimeo
                Scenario: scenario 1
                """);
    }

    @Test
    void deletesSingleMetaKeyAndTextFromSingleFile() {
        initBDDFileAndTreeWithSelectedNodeToDelete("delete_tag.story", "Single:key");

        new DeleteAllTagOccurrencesAction(getProject()).actionPerformed(doTestActionEvent());

        getFixture().checkResult(
            """
                Meta:
                @Single
                @Media youtube vimeo
                Scenario: scenario 1

                Meta:
                @Device tablet
                @Media youtube vimeo
                Scenario: scenario 1

                Meta:
                @Device tablet
                @Media youtube vimeo
                Scenario: scenario 1
                """);
    }

    @Test
    void deletesMultipleMetaKeyAndTextFromSingleFile() {
        initBDDFileAndTreeWithSelectedNodeToDelete("delete_tag.story", "Device:tablet");

        new DeleteAllTagOccurrencesAction(getProject()).actionPerformed(doTestActionEvent());

        getFixture().checkResult(
            """
                Meta:
                @Single
                @Single key
                @Media youtube vimeo
                Scenario: scenario 1

                Meta:
                @Media youtube vimeo
                Scenario: scenario 1

                Meta:
                @Media youtube vimeo
                Scenario: scenario 1
                """);
    }

    @Test
    void deletesMultipleMetasFromSingleFile() {
        initBDDFileAndTreeWithSelectedNodeToDelete("delete_tag.story", "Media:youtube vimeo");

        new DeleteAllTagOccurrencesAction(getProject()).actionPerformed(doTestActionEvent());

        getFixture().checkResult(
            """
                Meta:
                @Single
                @Single key
                Scenario: scenario 1

                Meta:
                @Device tablet
                Scenario: scenario 1

                Meta:
                @Device tablet
                Scenario: scenario 1
                """);
    }

    @Test
    void deletesMultipleMetasFromMultipleFiles() {
        VirtualFile file = getFixture().copyFileToProject("delete_tag_more.story");
        var delete_tag_more = findPsiFile(file);
        initBDDFileAndTreeWithSelectedNodeToDelete("delete_tag.story", "Media:youtube vimeo");

        new DeleteAllTagOccurrencesAction(getProject()).actionPerformed(doTestActionEvent());

        getFixture().checkResult(
            """
                Meta:
                @Single
                @Single key
                Scenario: scenario 1

                Meta:
                @Device tablet
                Scenario: scenario 1

                Meta:
                @Device tablet
                Scenario: scenario 1
                """);
        assertThat(delete_tag_more.getText()).isEqualTo(
            """
                Meta:
                @Single
                @Single key
                Scenario: scenario 1

                Meta:
                @Device tablet
                Scenario: scenario 1

                Meta:
                @Device tablet
                Scenario: scenario 1
                """);
    }

    //Helpers

    /**
     * Initializes the BDD resource {@code file} from which tags with {@code tagName} will be deleted.
     */
    private void initBDDFileAndTreeWithSelectedNodeToDelete(String file, String tagName) {
        getFixture().configureByFile(file);
        initGherkinTagTreeAndSetSelectionTo(tagName);
    }

    /**
     * Initializes the Gherkin tag tree and its underlying model, then select the tree node with
     * the provided {@code tagName}.
     * <p>
     * Tags with the selected name will be the ones removed from the target file.
     *
     * @param tagName the name of the tag node to select, and that will be removed
     */
    private void initGherkinTagTreeAndSetSelectionTo(String tagName) {
        var model = new ProjectSpecificGherkinTagTreeModel(getProject());
        model.buildModel();
        tree = new GherkinTagTree(model, getProject());
        tree.setSelectionPath(new TreePath(((ModelDataRoot) tree.getModel().getRoot()).getOther().get(tagName).get()));
    }

    private AnActionEvent doTestActionEvent() {
        return TestActionEvent.createTestEvent(dataId -> {
            if (CommonDataKeys.PROJECT.is(dataId)) return getProject();
            if (PlatformDataKeys.CONTEXT_COMPONENT.is(dataId)) return tree;
            return null;
        });
    }
}
