//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static com.picimako.gherkin.SoftAsserts.assertSoftly;
import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.mock.MockVirtualFile;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
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

        var registry = initRegistryAndCalculateCounts(2, virtualFile, virtualFile2);

        assertSoftly(
            softly -> softly.assertThat(registry.getCountFor(virtualFile.getPath(), "disabled")).isEqualTo(1),
            softly -> softly.assertThat(registry.getCountFor(virtualFile2.getPath(), "tablet")).isEqualTo(2),
            softly -> softly.assertThat(registry.getCountFor(virtualFile2.getPath(), "youtube")).isEqualTo(3)
        );
    }

    public void testCalculatesCountsForMetas() {
        VirtualFile virtualFile = myFixture.configureByFile("Story.story").getVirtualFile();
        VirtualFile virtualFile2 = myFixture.configureByFile("Another story.story").getVirtualFile();

        var registry = initRegistryAndCalculateCounts(2, virtualFile, virtualFile2);

        assertSoftly(
            softly -> softly.assertThat(registry.getCountFor(virtualFile.getPath(), "Disabled")).isEqualTo(2),
            softly -> softly.assertThat(registry.getCountFor(virtualFile2.getPath(), "Device:tablet")).isEqualTo(2),
            softly -> softly.assertThat(registry.getCountFor(virtualFile2.getPath(), "Media:youtube vimeo")).isEqualTo(3)
        );
    }

    public void testDoesntCalculateCountsForNonExistentFile() {
        var nonExistentFile = new InvalidatableMockVirtualFile("some_non_existent_gherkin.feature", false, false);

        var registry = initRegistryAndCalculateCounts(2, nonExistentFile);

        assertThat(registry.getTagOccurrences().get(nonExistentFile.getPath())).isEmpty();
    }

    public void testDoesntCalculateCountsForInvalidFile() {
        var invalidFile = new InvalidatableMockVirtualFile("some_invalid_gherkin.feature", true, false);

        var registry = initRegistryAndCalculateCounts(2, invalidFile);

        assertThat(registry.getTagOccurrences().get(invalidFile.getPath())).isEmpty();
    }

    //updateOccurrenceCounts

    public void testUpdatesCounts() {
        PsiFile psiFile = myFixture.configureByFile("for_statistics.feature");
        VirtualFile virtualFile = psiFile.getVirtualFile();

        var registry = initRegistryAndCalculateCounts(1, virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "youtube")).isEqualTo(3);

        GherkinTag tag = BDDTestSupport.getFirstGherkinTagForName(psiFile, "@youtube");
        WriteAction.run(() -> CommandProcessor.getInstance().executeCommand(getProject(), () -> tag.delete(), "Delete", "group.id"));
        registry.updateOccurrenceCounts(virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "youtube")).isEqualTo(2);
    }

    public void testUpdatesCountsForMetas() {
        PsiFile psiFile = myFixture.configureByFile("Story.story");
        VirtualFile virtualFile = psiFile.getVirtualFile();

        var registry = initRegistryAndCalculateCounts(1, virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "Disabled")).isEqualTo(2);

        PsiElement meta = BDDTestSupport.getFirstMetaKeyForName(psiFile, "@Disabled");
        WriteAction.run(() -> CommandProcessor.getInstance().executeCommand(getProject(), () -> meta.delete(), "Delete", "group.id"));
        registry.updateOccurrenceCounts(virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "Disabled")).isOne();
    }

    public void testDoesntUpdateCountsForFileBecameNonExistent() {
        var toBecomeNonExistentFile = new InvalidatableMockVirtualFile("some_to_become_non_existent_gherkin.feature", true, true);

        var registry = initRegistryAndCalculateCounts(2, toBecomeNonExistentFile);

        toBecomeNonExistentFile.setExist(false);
        registry.updateOccurrenceCounts(toBecomeNonExistentFile);

        assertThat(registry.getTagOccurrences().get(toBecomeNonExistentFile.getPath())).isEmpty();
    }

    public void testDoesntUpdateCountsForFileBecameInvalid() {
        var toBecomeInvalidFile = new InvalidatableMockVirtualFile("some_to_become_invalid_gherkin.feature", true, true);

        var registry = initRegistryAndCalculateCounts(2, toBecomeInvalidFile);

        toBecomeInvalidFile.setValid(false);
        registry.updateOccurrenceCounts(toBecomeInvalidFile);

        assertThat(registry.getTagOccurrences().get(toBecomeInvalidFile.getPath())).isEmpty();
    }

    //getCountFor

    public void testReturnsCountFor() {
        myFixture.configureByFile("the_gherkin.feature");
        myFixture.configureByFile("A_gherkin.feature");
        VirtualFile virtualFile = myFixture.configureByFile("for_statistics.feature").getVirtualFile();

        var registry = initRegistryAndCalculateCounts(2, virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "tablet")).isEqualTo(2);
    }

    public void testReturnsCountForMetas() {
        myFixture.configureByFile("Story.story");
        VirtualFile virtualFile = myFixture.configureByFile("Another story.story").getVirtualFile();

        var registry = initRegistryAndCalculateCounts(1, virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "Media:youtube vimeo")).isEqualTo(3);
    }

    public void testReturnsZeroAsCountForNonMappedTag() {
        myFixture.configureByFile("the_gherkin.feature");
        myFixture.configureByFile("A_gherkin.feature");
        VirtualFile virtualFile = myFixture.configureByFile("for_statistics.feature").getVirtualFile();

        var registry = initRegistryAndCalculateCounts(3, virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "landing")).isZero();
    }

    public void testReturnsZeroAsCountForNonMappedTagForMetas() {
        myFixture.configureByFile("Story.story");
        VirtualFile virtualFile = myFixture.configureByFile("Another story.story").getVirtualFile();

        var registry = initRegistryAndCalculateCounts(2, virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "landing")).isZero();
    }

    public void testReturnsZeroAsCountForNonMappedFilePath() {
        PsiFile psiFile = myFixture.configureByFile("Story.story");
        VirtualFile virtualFile = myFixture.configureByFile("Another story.story").getVirtualFile();

        var registry = initRegistryAndCalculateCounts(2, virtualFile);

        assertThat(registry.getCountFor(psiFile.getVirtualFile().getPath(), "landing")).isZero();
    }

    //remove

    public void testRemovesMappingForFilePath() {
        VirtualFile virtualFile = myFixture.configureByFile("A_gherkin.feature").getVirtualFile();
        VirtualFile virtualFile2 = myFixture.configureByFile("for_statistics.feature").getVirtualFile();

        var registry = initRegistryAndCalculateCounts(2, virtualFile, virtualFile2);

        assertThat(registry.getTagOccurrences()).hasSize(2);

        registry.remove(virtualFile.getPath());

        assertThat(registry.getTagOccurrences()).hasSize(1);
    }

    public void testRemovesMappingForFilePathForMetas() {
        VirtualFile virtualFile = myFixture.configureByFile("Story.story").getVirtualFile();
        VirtualFile virtualFile2 = myFixture.configureByFile("Another story.story").getVirtualFile();

        var registry = initRegistryAndCalculateCounts(2, virtualFile, virtualFile2);

        assertThat(registry.getTagOccurrences()).hasSize(2);

        registry.remove(virtualFile.getPath());

        assertThat(registry.getTagOccurrences()).hasSize(1);
    }

    //Helpers

    private TagOccurrencesRegistry initRegistryAndCalculateCounts(int bddFileCount, VirtualFile... virtualFiles) {
        var registry = new TagOccurrencesRegistry(getProject());
        registry.init(bddFileCount);

        for (var virtualFile : virtualFiles)
            registry.calculateOccurrenceCounts(virtualFile);

        return registry;
    }

    @Setter
    private static final class InvalidatableMockVirtualFile extends MockVirtualFile {
        private boolean isExist;
        private boolean isValid;

        public InvalidatableMockVirtualFile(String name, boolean isExist, boolean isValid) {
            super(name);
            this.isExist = isExist;
            this.isValid = isValid;
        }

        @Override
        public boolean exists() {
            return isExist;
        }

        @Override
        public boolean isValid() {
            return isValid;
        }

        @Override
        public byte @NotNull [] contentsToByteArray() {
            return """
                @smoke @mobile @edge
                Feature: Videos
                """.getBytes();
        }
    }
}
