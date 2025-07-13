//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.psi.PsiElement;
import com.picimako.gherkin.toolwindow.BDDTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link DefaultJBehaveStoryService}.
 */
final class DefaultJBehaveStoryServiceTest extends GherkinOverviewTestBase {

    private JBehaveStoryService storyService;

    @BeforeEach
    void setUp() {
        storyService = new DefaultJBehaveStoryService(getProject());
    }

    //collectMetasFromFile

    @Test
    void collectMetasFromFile() {
        var storyFile = configureByText("story.story",
            """
                Meta:
                @Suite smoke
                @Browser firefox chrome
                @Jira

                Scenario:

                Meta:
                @Suite smoke

                Scenario:""");

        var metas = storyService.collectMetasFromFile(storyFile);

        assertThat(metas.entrySet()).hasSize(4);
    }

    @Test
    void collectNoMetasFromFileWithNoMetas() {
        var storyFile = configureByText("story.story", "Scenario:");
        var metas = storyService.collectMetasFromFile(storyFile);

        assertThat(metas.isEmpty()).isTrue();
    }

    //collectMetasFromFileAsList

    @Test
    void collectMetasFromFileAsList() {
        var storyFile = configureByText("story.story",
            """
                Meta:
                @Suite smoke
                @Browser firefox chrome
                @Jira

                Scenario:

                Meta:
                @Suite smoke

                Scenario:""");

        var metas = storyService.collectMetasFromFileAsList(storyFile);

        assertThat(metas).contains("Suite:smoke", "Browser:firefox chrome", "Jira", "Suite:smoke");
    }

    @Test
    void collectNoMetasFromFileWithNoMetasAsList() {
        var storyFile = configureByText("story.story", "Scenario:");
        var metas = storyService.collectMetasFromFileAsList(storyFile);

        assertThat(metas).isEmpty();
    }

    //collectMetaTextsForMetaKeyAsList

    @Test
    void collectsMetaTextsForMetaKeyAsList() {
        var storyFile = configureByFile("Another story.story");
        var metaTexts = storyService.collectMetaTextsForMetaKeyAsList(BDDTestSupport.getFirstMetaKeyForName(storyFile, "@Media"));

        assertThat(metaTexts).isNotEmpty()
            .extracting(PsiElement::getText).containsExactly("youtube", "vimeo");
    }

    @Test
    void returnsEmptyListIfThereIsNoMetaTextForMetaKey() {
        var storyFile = configureByFile("Story.story");
        var metaTexts = storyService.collectMetaTextsForMetaKeyAsList(BDDTestSupport.getFirstMetaKeyForName(storyFile, "@Disabled"));

        assertThat(metaTexts).isEmpty();
    }
}
