//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Unit test for {@link GherkinUtil}.
 */
public class GherkinUtilTest extends BasePlatformTestCase {
    @Override
    protected String getTestDataPath() {
        return "testdata/features";
    }

    //collectGherkinTagsFromFile

    public void testCollectGherkinTagsFromFile() {
        PsiFile gherkinFile = myFixture.configureByText("gherkin.feature",
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

    public void testCollectNoGherkinTagsFromFileWithNoTags() {
        PsiFile gherkinFile = myFixture.configureByText("gherkin.feature", "Feature: A feature");

        List<String> tags = GherkinUtil.collectGherkinTagsFromFile(gherkinFile);

        assertThat(tags).isEmpty();
    }

    //isGherkinFile

    public void testIsAGherkinPsiFile() {
        PsiFile gherkinFile = myFixture.configureByText("gherkin.feature", "");

        assertThat(GherkinUtil.isGherkinFile(gherkinFile)).isTrue();
    }

    public void testIsNotAGherkinPsiFile() {
        PsiFile storyFile = myFixture.configureByText("story.story", "");

        assertThat(GherkinUtil.isGherkinFile(storyFile)).isFalse();
    }

    public void testIsAGherkinVirtualFile() {
        PsiFile gherkinFile = myFixture.configureByText("gherkin.feature", "");

        assertThat(GherkinUtil.isGherkinFile(gherkinFile.getVirtualFile())).isTrue();
    }

    public void testIsNotAGherkinVirtualFile() {
        PsiFile storyFile = myFixture.configureByText("story.story", "");

        assertThat(GherkinUtil.isGherkinFile(storyFile.getVirtualFile())).isFalse();
    }
}
