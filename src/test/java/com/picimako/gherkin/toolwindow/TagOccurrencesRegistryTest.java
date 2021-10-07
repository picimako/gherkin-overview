/*
 * Copyright 2021 Tamás Balog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.picimako.gherkin.toolwindow;

import static com.picimako.gherkin.SoftAsserts.assertSoftly;
import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;

/**
 * Unit test for {@link TagOccurrencesRegistry}.
 */
public class TagOccurrencesRegistryTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "testdata/features";
    }

    //calculateOccurrenceCounts

    public void testCalculatesCounts() {
        VirtualFile virtualFile = myFixture.configureByFile("A_gherkin.feature").getVirtualFile();
        VirtualFile virtualFile2 = myFixture.configureByFile("for_statistics.feature").getVirtualFile();

        TagOccurrencesRegistry registry = new TagOccurrencesRegistry(getProject());
        registry.init(2);
        registry.calculateOccurrenceCounts(virtualFile);
        registry.calculateOccurrenceCounts(virtualFile2);

        assertSoftly(
            softly -> softly.assertThat(registry.getCountFor(virtualFile.getPath(), "disabled")).isEqualTo(1),
            softly -> softly.assertThat(registry.getCountFor(virtualFile2.getPath(), "tablet")).isEqualTo(2),
            softly -> softly.assertThat(registry.getCountFor(virtualFile2.getPath(), "youtube")).isEqualTo(3)
        );
    }

    public void testCalculatesCountsForMetas() {
        VirtualFile virtualFile = myFixture.configureByFile("Story.story").getVirtualFile();
        VirtualFile virtualFile2 = myFixture.configureByFile("Another story.story").getVirtualFile();

        TagOccurrencesRegistry registry = new TagOccurrencesRegistry(getProject());
        registry.init(2);
        registry.calculateOccurrenceCounts(virtualFile);
        registry.calculateOccurrenceCounts(virtualFile2);

        assertSoftly(
            softly -> softly.assertThat(registry.getCountFor(virtualFile.getPath(), "Disabled")).isEqualTo(2),
            softly -> softly.assertThat(registry.getCountFor(virtualFile2.getPath(), "Device:tablet")).isEqualTo(2),
            softly -> softly.assertThat(registry.getCountFor(virtualFile2.getPath(), "Media:youtube vimeo")).isEqualTo(3)
        );
    }

    //updateOccurrenceCounts

    public void testUpdatesCounts() {
        PsiFile psiFile = myFixture.configureByFile("for_statistics.feature");
        VirtualFile virtualFile = psiFile.getVirtualFile();

        TagOccurrencesRegistry registry = new TagOccurrencesRegistry(getProject());
        registry.init(1);
        registry.calculateOccurrenceCounts(virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "youtube")).isEqualTo(3);

        GherkinTag tag = BDDTestSupport.getFirstGherkinTagForName(psiFile, "@youtube");
        WriteAction.run(() -> CommandProcessor.getInstance().executeCommand(getProject(), () -> tag.delete(), "Delete", "group.id"));
        registry.updateOccurrenceCounts(virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "youtube")).isEqualTo(2);
    }

    public void testUpdatesCountsForMetas() {
        PsiFile psiFile = myFixture.configureByFile("Story.story");
        VirtualFile virtualFile = psiFile.getVirtualFile();

        TagOccurrencesRegistry registry = new TagOccurrencesRegistry(getProject());
        registry.init(1);
        registry.calculateOccurrenceCounts(virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "Disabled")).isEqualTo(2);

        PsiElement meta = BDDTestSupport.getFirstMetaKeyForName(psiFile, "@Disabled");
        WriteAction.run(() -> CommandProcessor.getInstance().executeCommand(getProject(), () -> meta.delete(), "Delete", "group.id"));
        registry.updateOccurrenceCounts(virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "Disabled")).isEqualTo(1);
    }

    //getCountFor

    public void testReturnsCountFor() {
        myFixture.configureByFile("the_gherkin.feature");
        myFixture.configureByFile("A_gherkin.feature");
        VirtualFile virtualFile = myFixture.configureByFile("for_statistics.feature").getVirtualFile();

        TagOccurrencesRegistry registry = new TagOccurrencesRegistry(getProject());
        registry.init(2);
        registry.calculateOccurrenceCounts(virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "tablet")).isEqualTo(2);
    }

    public void testReturnsCountForMetas() {
        myFixture.configureByFile("Story.story");
        VirtualFile virtualFile = myFixture.configureByFile("Another story.story").getVirtualFile();

        TagOccurrencesRegistry registry = new TagOccurrencesRegistry(getProject());
        registry.init(1);
        registry.calculateOccurrenceCounts(virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "Media:youtube vimeo")).isEqualTo(3);
    }

    public void testReturnsZeroAsCountForNonMappedTag() {
        myFixture.configureByFile("the_gherkin.feature");
        myFixture.configureByFile("A_gherkin.feature");
        VirtualFile virtualFile = myFixture.configureByFile("for_statistics.feature").getVirtualFile();

        TagOccurrencesRegistry registry = new TagOccurrencesRegistry(getProject());
        registry.init(3);
        registry.calculateOccurrenceCounts(virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "landing")).isZero();
    }

    public void testReturnsZeroAsCountForNonMappedTagForMetas() {
        myFixture.configureByFile("Story.story");
        VirtualFile virtualFile = myFixture.configureByFile("Another story.story").getVirtualFile();

        TagOccurrencesRegistry registry = new TagOccurrencesRegistry(getProject());
        registry.init(2);
        registry.calculateOccurrenceCounts(virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "landing")).isZero();
    }

    //remove

    public void testRemovesMappingForFilePath() {
        VirtualFile virtualFile = myFixture.configureByFile("A_gherkin.feature").getVirtualFile();
        VirtualFile virtualFile2 = myFixture.configureByFile("for_statistics.feature").getVirtualFile();

        TagOccurrencesRegistry registry = new TagOccurrencesRegistry(getProject());
        registry.init(2);
        registry.calculateOccurrenceCounts(virtualFile);
        registry.calculateOccurrenceCounts(virtualFile2);

        assertThat(registry.getTagOccurrences()).hasSize(2);

        registry.remove(virtualFile.getPath());

        assertThat(registry.getTagOccurrences()).hasSize(1);
    }

    public void testRemovesMappingForFilePathForMetas() {
        VirtualFile virtualFile = myFixture.configureByFile("Story.story").getVirtualFile();
        VirtualFile virtualFile2 = myFixture.configureByFile("Another story.story").getVirtualFile();

        TagOccurrencesRegistry registry = new TagOccurrencesRegistry(getProject());
        registry.init(2);
        registry.calculateOccurrenceCounts(virtualFile);
        registry.calculateOccurrenceCounts(virtualFile2);

        assertThat(registry.getTagOccurrences()).hasSize(2);

        registry.remove(virtualFile.getPath());

        assertThat(registry.getTagOccurrences()).hasSize(1);
    }
}
