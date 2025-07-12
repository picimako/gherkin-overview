//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.picimako.gherkin.toolwindow.TagNameUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;

/**
 * Unit test for {@link TagNameUtil}.
 */
public class TagNameUtilTest extends GherkinOverviewTestBase {
    
    private JBehaveStoryService storyService;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        storyService = new DefaultJBehaveStoryService(getProject());
    }

    //tagNameFrom

    public void testReturnTagName() {
        PsiFile gherkinFile = myFixture.configureByText("gherkin.feature",
            "@smoke\n" +
                "Feature: A feature");

        GherkinTag tag = PsiTreeUtil.findChildOfType(gherkinFile, GherkinTag.class);

        assertThat(TagNameUtil.tagNameFrom(tag)).isEqualTo("smoke");
    }

    //metaNameFrom

    public void testReturnMetaNameForKeyAndText() {
        PsiFile storyFile = myFixture.configureByText("story.story",
            """
                Meta:
                @Suite smoke
                Scenario:""");

        var meta = new ArrayList<>(storyService.collectMetasFromFile(storyFile).entrySet());
        assertThat(meta.getFirst().getValue()).hasSize(1);

        String metaName = TagNameUtil.metaNameFrom(meta.getFirst().getKey(), new ArrayList<>(meta.getFirst().getValue()));

        assertThat(metaName).isEqualTo("Suite:smoke");
    }

    public void testReturnMetaNameForKeyOnly() {
        PsiFile storyFile = myFixture.configureByText("story.story",
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


    public void testDeterminesTagName() {
        PsiFile gherkinFile = myFixture.configureByText("gherkin.feature",
            "@smoke\n" +
                "Feature: A feature");

        GherkinTag tag = PsiTreeUtil.findChildOfType(gherkinFile, GherkinTag.class);

        String tagName = TagNameUtil.determineTagOrMetaName(tag);

        assertThat(tagName).isEqualTo("smoke");

    }

    public void testDeterminesMetaName() {
        PsiFile storyFile = myFixture.configureByText("story.story",
            """
                Meta:
                @Suite smoke regression
                Scenario:""");

        var meta = new ArrayList<>(storyService.collectMetasFromFile(storyFile).entrySet());

        String metaName = TagNameUtil.determineTagOrMetaName(meta.getFirst().getKey());

        assertThat(metaName).isEqualTo("Suite:smoke regression");
    }

    public void testReturnsNullForNonTagAndNonMetaElement() {
        PsiFile storyFile = myFixture.configureByText("story.story", "");

        String metaName = TagNameUtil.determineTagOrMetaName(storyFile);

        assertThat(metaName).isNull();
    }
}
