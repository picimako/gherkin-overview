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

import java.util.Collection;
import java.util.List;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import com.picimako.gherkin.toolwindow.BDDTestSupport;

/**
 * Unit test for {@link DefaultJBehaveStoryService}.
 */
public class DefaultJBehaveStoryServiceTest extends BasePlatformTestCase {

    private JBehaveStoryService storyService;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        storyService = new DefaultJBehaveStoryService(getProject());
    }

    @Override
    protected String getTestDataPath() {
        return "testdata/features";
    }

    //collectMetasFromFile

    public void testCollectMetasFromFile() {
        PsiFile storyFile = myFixture.configureByText("story.story",
            "Meta:\n" +
                "@Suite smoke\n" +
                "@Browser firefox chrome\n" +
                "@Jira\n" +
                "\n" +
                "Scenario:\n" +
                "\n" +
                "Meta:\n" +
                "@Suite smoke\n" +
                "\n" +
                "Scenario:");

        var metas = storyService.collectMetasFromFile(storyFile);

        assertThat(metas.entrySet()).hasSize(4);
    }

    public void testCollectNoMetasFromFileWithNoMetas() {
        PsiFile storyFile = myFixture.configureByText("story.story", "Scenario:");

        var metas = storyService.collectMetasFromFile(storyFile);

        assertThat(metas.isEmpty()).isTrue();
    }

    //collectMetasFromFileAsList

    public void testCollectMetasFromFileAsList() {
        PsiFile storyFile = myFixture.configureByText("story.story",
            "Meta:\n" +
                "@Suite smoke\n" +
                "@Browser firefox chrome\n" +
                "@Jira\n" +
                "\n" +
                "Scenario:\n" +
                "\n" +
                "Meta:\n" +
                "@Suite smoke\n" +
                "\n" +
                "Scenario:");

        List<String> metas = storyService.collectMetasFromFileAsList(storyFile);

        assertThat(metas).contains("Suite:smoke", "Browser:firefox chrome", "Jira", "Suite:smoke");
    }

    public void testCollectNoMetasFromFileWithNoMetasAsList() {
        PsiFile storyFile = myFixture.configureByText("story.story", "Scenario:");

        List<String> metas = storyService.collectMetasFromFileAsList(storyFile);

        assertThat(metas).isEmpty();
    }

    //collectMetaTextsForMetaKeyAsList

    public void testCollectsMetaTextsForMetaKeyAsList() {
        PsiFile storyFile = myFixture.configureByFile("Another story.story");
        Collection<PsiElement> metaTexts = storyService.collectMetaTextsForMetaKeyAsList(
            BDDTestSupport.getFirstMetaKeyForName(storyFile, "@Media"));

        assertThat(metaTexts).isNotEmpty();
        assertThat(metaTexts).extracting(PsiElement::getText).containsExactly("youtube", "vimeo");
    }

    public void testReturnsEmptyListIfThereIsNoMetaTextForMetaKey() {
        PsiFile storyFile = myFixture.configureByFile("Story.story");
        Collection<PsiElement> metaTexts = storyService.collectMetaTextsForMetaKeyAsList(
            BDDTestSupport.getFirstMetaKeyForName(storyFile, "@Disabled"));

        assertThat(metaTexts).isEmpty();
    }
}
