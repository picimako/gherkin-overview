//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;

import com.picimako.gherkin.toolwindow.TagNameUtil;

/**
 * Unit test for {@link TagNameUtil}.
 */
public class TagNameUtilTest extends BasePlatformTestCase {
    
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
            "Meta:\n" +
                "@Suite smoke\n" +
                "Scenario:");

        var meta = new ArrayList<>(storyService.collectMetasFromFile(storyFile).entrySet());
        assertThat(meta.get(0).getValue()).hasSize(1);

        String metaName = TagNameUtil.metaNameFrom(meta.get(0).getKey(), new ArrayList<>(meta.get(0).getValue()));

        assertThat(metaName).isEqualTo("Suite:smoke");
    }

    public void testReturnMetaNameForKeyOnly() {
        PsiFile storyFile = myFixture.configureByText("story.story",
            "Meta:\n" +
                "@Jira\n" +
                "Scenario:");

        var meta = new ArrayList<>(storyService.collectMetasFromFile(storyFile).entrySet());
        assertThat(meta.get(0).getValue()).isEmpty();

        String metaName = TagNameUtil.metaNameFrom(meta.get(0).getKey(), null);
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
            "Meta:\n" +
                "@Suite smoke regression\n" +
                "Scenario:");

        var meta = new ArrayList<>(storyService.collectMetasFromFile(storyFile).entrySet());

        String metaName = TagNameUtil.determineTagOrMetaName(meta.get(0).getKey());

        assertThat(metaName).isEqualTo("Suite:smoke regression");
    }

    public void testReturnsNullForNonTagAndNonMetaElement() {
        PsiFile storyFile = myFixture.configureByText("story.story", "");

        String metaName = TagNameUtil.determineTagOrMetaName(storyFile);

        assertThat(metaName).isNull();
    }
}
