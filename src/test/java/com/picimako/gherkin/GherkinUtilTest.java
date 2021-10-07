/*
 *  Copyright 2021 Tamás Balog
 *  
 *  Licensed under the Apache License, Version 2.0 \(the "License"\);
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
            "@smoke\n" +
                "Feature: A feature\n" +
                "\n" +
                "\t@regression\n" +
                "\tScenario:\n" +
                "\n" +
                "\t@jira @trello @regression\n" +
                "\tScenario:");

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
