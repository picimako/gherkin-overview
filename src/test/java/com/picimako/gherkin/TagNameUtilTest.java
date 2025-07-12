//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin;

import static com.intellij.openapi.application.ReadAction.compute;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.picimako.gherkin.toolwindow.TagNameUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link TagNameUtil}.
 */
final class TagNameUtilTest extends GherkinOverviewTestBase {
    private JBehaveStoryService storyService;
    
    @BeforeEach
    void setUp() {
        storyService = new DefaultJBehaveStoryService(getProject());
    }

    //tagNameFrom

    @Test
    void returnTagName() {
        PsiFile gherkinFile = getFixture().configureByText("gherkin.feature",
            "@smoke\n" +
                "Feature: A feature");

        GherkinTag tag = compute(() -> PsiTreeUtil.findChildOfType(gherkinFile, GherkinTag.class));

        assertThat(TagNameUtil.tagNameFrom(tag)).isEqualTo("smoke");
    }

    //metaNameFrom

    @Test
    void returnMetaNameForKeyAndText() {
        PsiFile storyFile = getFixture().configureByText("story.story",
            """
                Meta:
                @Suite smoke
                Scenario:""");

        var meta = new ArrayList<>(storyService.collectMetasFromFile(storyFile).entrySet());
        assertThat(meta.getFirst().getValue()).hasSize(1);

        String metaName = TagNameUtil.metaNameFrom(meta.getFirst().getKey(), new ArrayList<>(meta.getFirst().getValue()));

        assertThat(metaName).isEqualTo("Suite:smoke");
    }

    @Test
    void returnMetaNameForKeyOnly() {
        PsiFile storyFile = getFixture().configureByText("story.story",
            """
                Meta:
                @Jira
                Scenario:""");

        var meta = new ArrayList<>(storyService.collectMetasFromFile(storyFile).entrySet());
        assertThat(meta.getFirst().getValue()).isEmpty();

        String metaName = TagNameUtil.metaNameFrom(meta.getFirst().getKey(), null);
        assertThat(metaName).isEqualTo("Jira");
    }

    //determineTagOrMetaName


    @Test
    void determinesTagName() {
        PsiFile gherkinFile = getFixture().configureByText("gherkin.feature",
            "@smoke\n" +
                "Feature: A feature");

        GherkinTag tag = compute(() -> PsiTreeUtil.findChildOfType(gherkinFile, GherkinTag.class));

        String tagName = TagNameUtil.determineTagOrMetaName(tag);

        assertThat(tagName).isEqualTo("smoke");

    }

    @Test
    void determinesMetaName() {
        PsiFile storyFile = getFixture().configureByText("story.story",
            """
                Meta:
                @Suite smoke regression
                Scenario:""");

        var meta = new ArrayList<>(storyService.collectMetasFromFile(storyFile).entrySet());

        String metaName = TagNameUtil.determineTagOrMetaName(meta.getFirst().getKey());

        assertThat(metaName).isEqualTo("Suite:smoke regression");
    }

    @Test
    void returnsNullForNonTagAndNonMetaElement() {
        PsiFile storyFile = getFixture().configureByText("story.story", "");

        String metaName = TagNameUtil.determineTagOrMetaName(storyFile);

        assertThat(metaName).isNull();
    }
}
