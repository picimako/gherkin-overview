//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

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

    public void testCollectNoMetasFromFileWithNoMetas() {
        PsiFile storyFile = myFixture.configureByText("story.story", "Scenario:");

        var metas = storyService.collectMetasFromFile(storyFile);

        assertThat(metas.isEmpty()).isTrue();
    }

    //collectMetasFromFileAsList

    public void testCollectMetasFromFileAsList() {
        PsiFile storyFile = myFixture.configureByText("story.story",
            """
                Meta:
                @Suite smoke
                @Browser firefox chrome
                @Jira

                Scenario:

                Meta:
                @Suite smoke

                Scenario:""");

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
