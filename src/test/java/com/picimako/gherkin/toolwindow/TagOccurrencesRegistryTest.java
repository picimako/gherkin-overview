//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.intellij.mock.MockVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.picimako.gherkin.GherkinOverviewTestBase;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link TagOccurrencesRegistry}.
 */
final class TagOccurrencesRegistryTest extends GherkinOverviewTestBase {

    //calculateOccurrenceCounts

    @Test
    void calculatesCounts() {
        VirtualFile virtualFile = configureVirtualFile("A_gherkin.feature");
        VirtualFile virtualFile2 = configureVirtualFile("for_statistics.feature");

        var registry = initRegistryAndCalculateCounts(2, virtualFile, virtualFile2);

        assertSoftly(s -> {
            s.assertThat(registry.getCountFor(virtualFile.getPath(), "disabled")).isOne();
            s.assertThat(registry.getCountFor(virtualFile2.getPath(), "tablet")).isEqualTo(2);
            s.assertThat(registry.getCountFor(virtualFile2.getPath(), "youtube")).isEqualTo(3);
        });
    }

    @Test
    void calculatesCountsForMetas() {
        VirtualFile virtualFile = configureVirtualFile("Story.story");
        VirtualFile virtualFile2 = configureVirtualFile("Another story.story");

        var registry = initRegistryAndCalculateCounts(2, virtualFile, virtualFile2);

        assertSoftly(s -> {
            s.assertThat(registry.getCountFor(virtualFile.getPath(), "Disabled")).isEqualTo(2);
            s.assertThat(registry.getCountFor(virtualFile2.getPath(), "Device:tablet")).isEqualTo(2);
            s.assertThat(registry.getCountFor(virtualFile2.getPath(), "Media:youtube vimeo")).isEqualTo(3);
        });
    }

    @Test
    void doesntCalculateCountsForNonExistentFile() {
        var nonExistentFile = new InvalidatableMockVirtualFile("some_non_existent_gherkin.feature", false, false);

        var registry = initRegistryAndCalculateCounts(2, nonExistentFile);

        assertThat(registry.getTagOccurrences().get(nonExistentFile.getPath())).isEmpty();
    }

    @Test
    void doesntCalculateCountsForInvalidFile() {
        var invalidFile = new InvalidatableMockVirtualFile("some_invalid_gherkin.feature", true, false);

        var registry = initRegistryAndCalculateCounts(2, invalidFile);

        assertThat(registry.getTagOccurrences().get(invalidFile.getPath())).isEmpty();
    }

    //updateOccurrenceCounts

    @Test
    void updatesCounts() {
        PsiFile psiFile = configureByFile("for_statistics.feature");
        VirtualFile virtualFile = psiFile.getVirtualFile();

        var registry = initRegistryAndCalculateCounts(1, virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "youtube")).isEqualTo(3);

        GherkinTag tag = BDDTestSupport.getFirstGherkinTagForName(psiFile, "@youtube");
        executeCommandProcessorCommand(tag::delete, "Delete", "group.id");
        registry.updateOccurrenceCounts(virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "youtube")).isEqualTo(2);
    }

    @Test
    void updatesCountsForMetas() {
        PsiFile psiFile = configureByFile("Story.story");
        VirtualFile virtualFile = psiFile.getVirtualFile();

        var registry = initRegistryAndCalculateCounts(1, virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "Disabled")).isEqualTo(2);

        PsiElement meta = BDDTestSupport.getFirstMetaKeyForName(psiFile, "@Disabled");
        executeCommandProcessorCommand(meta::delete, "Delete", "group.id");
        registry.updateOccurrenceCounts(virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "Disabled")).isOne();
    }

    @Test
    void doesntUpdateCountsForFileBecameNonExistent() {
        var toBecomeNonExistentFile = new InvalidatableMockVirtualFile("some_to_become_non_existent_gherkin.feature", true, true);

        var registry = initRegistryAndCalculateCounts(2, toBecomeNonExistentFile);

        toBecomeNonExistentFile.setExist(false);
        registry.updateOccurrenceCounts(toBecomeNonExistentFile);

        assertThat(registry.getTagOccurrences().get(toBecomeNonExistentFile.getPath())).isEmpty();
    }

    @Test
    void doesntUpdateCountsForFileBecameInvalid() {
        var toBecomeInvalidFile = new InvalidatableMockVirtualFile("some_to_become_invalid_gherkin.feature", true, true);

        var registry = initRegistryAndCalculateCounts(2, toBecomeInvalidFile);

        toBecomeInvalidFile.setValid(false);
        registry.updateOccurrenceCounts(toBecomeInvalidFile);

        assertThat(registry.getTagOccurrences().get(toBecomeInvalidFile.getPath())).isEmpty();
    }

    //getCountFor

    @Test
    void returnsCountFor() {
        configureByFile("the_gherkin.feature");
        configureByFile("A_gherkin.feature");
        VirtualFile virtualFile = configureVirtualFile("for_statistics.feature");

        var registry = initRegistryAndCalculateCounts(2, virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "tablet")).isEqualTo(2);
    }

    @Test
    void returnsCountForMetas() {
        configureByFile("Story.story");
        VirtualFile virtualFile = configureVirtualFile("Another story.story");

        var registry = initRegistryAndCalculateCounts(1, virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "Media:youtube vimeo")).isEqualTo(3);
    }

    @Test
    void returnsZeroAsCountForNonMappedTag() {
        configureByFile("the_gherkin.feature");
        configureByFile("A_gherkin.feature");
        VirtualFile virtualFile = configureVirtualFile("for_statistics.feature");

        var registry = initRegistryAndCalculateCounts(3, virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "landing")).isZero();
    }

    @Test
    void returnsZeroAsCountForNonMappedTagForMetas() {
        configureByFile("Story.story");
        VirtualFile virtualFile = configureVirtualFile("Another story.story");

        var registry = initRegistryAndCalculateCounts(2, virtualFile);

        assertThat(registry.getCountFor(virtualFile.getPath(), "landing")).isZero();
    }

    @Test
    void returnsZeroAsCountForNonMappedFilePath() {
        PsiFile psiFile = configureByFile("Story.story");
        VirtualFile virtualFile = configureVirtualFile("Another story.story");

        var registry = initRegistryAndCalculateCounts(2, virtualFile);

        assertThat(registry.getCountFor(psiFile.getVirtualFile().getPath(), "landing")).isZero();
    }

    //remove

    @Test
    void removesMappingForFilePath() {
        VirtualFile virtualFile = configureVirtualFile("A_gherkin.feature");
        VirtualFile virtualFile2 = configureVirtualFile("for_statistics.feature");

        var registry = initRegistryAndCalculateCounts(2, virtualFile, virtualFile2);

        assertThat(registry.getTagOccurrences()).hasSize(2);

        registry.remove(virtualFile.getPath());

        assertThat(registry.getTagOccurrences()).hasSize(1);
    }

    @Test
    void removesMappingForFilePathForMetas() {
        VirtualFile virtualFile = configureVirtualFile("Story.story");
        VirtualFile virtualFile2 = configureVirtualFile("Another story.story");

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
        @Getter
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
        public byte @NotNull [] contentsToByteArray() {
            return """
                @smoke @mobile @edge
                Feature: Videos
                """.getBytes();
        }
    }
}
