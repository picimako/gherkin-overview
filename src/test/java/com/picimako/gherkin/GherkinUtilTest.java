//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.intellij.psi.PsiFile;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link GherkinUtil}.
 */
final class GherkinUtilTest extends GherkinOverviewTestBase {

    //collectGherkinTagsFromFile

    @Test
    void collectGherkinTagsFromFile() {
        PsiFile gherkinFile = configureByText("gherkin.feature",
            """
                @smoke
                Feature: A feature

                \t@regression
                \tScenario:

                \t@jira @trello @regression
                \tScenario:""");

        List<String> tags = GherkinUtil.collectGherkinTagsFromFile(gherkinFile);

        assertThat(tags).containsExactly("smoke", "regression", "jira", "trello");
    }

    @Test
    void collectNoGherkinTagsFromFileWithNoTags() {
        PsiFile gherkinFile = configureByText("gherkin.feature", "Feature: A feature");

        List<String> tags = GherkinUtil.collectGherkinTagsFromFile(gherkinFile);

        assertThat(tags).isEmpty();
    }

    //isGherkinFile

    @Test
    void isAGherkinPsiFile() {
        PsiFile gherkinFile = configureEmptyFile("gherkin.feature");

        assertThat(GherkinUtil.isGherkinFile(gherkinFile)).isTrue();
    }

    @Test
    void isNotAGherkinPsiFile() {
        PsiFile storyFile = configureEmptyFile("story.story");

        assertThat(GherkinUtil.isGherkinFile(storyFile)).isFalse();
    }

    @Test
    void isAGherkinVirtualFile() {
        PsiFile gherkinFile = configureEmptyFile("gherkin.feature");

        assertThat(GherkinUtil.isGherkinFile(gherkinFile.getVirtualFile())).isTrue();
    }

    @Test
    void isNotAGherkinVirtualFile() {
        PsiFile storyFile = configureEmptyFile("story.story");

        assertThat(GherkinUtil.isGherkinFile(storyFile.getVirtualFile())).isFalse();
    }
}
