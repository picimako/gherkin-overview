//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static com.picimako.gherkin.toolwindow.BDDTestSupport.getFirstMetaKeyForName;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.picimako.gherkin.settings.CategoryAndTags;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import com.picimako.gherkin.toolwindow.nodetype.Tag;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link GherkinTagTreeModel}.
 */
final class GherkinTagTreeModelJBehaveStoryTest extends GherkinTagTreeModelTestBase {
    private VirtualFile treeModelStory;
    private VirtualFile treeModel2Story;
    private PsiFile psitreeModelStory;
    private GherkinTagTreeModel model;
    @Getter
    private ModelDataRoot root;

    @BeforeEach
    void setUp() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        psitreeModelStory = configureByFile("TreeModel.story");
        treeModelStory = psitreeModelStory.getVirtualFile();
        treeModel2Story = configureVirtualFile("TreeModel2.story");
        model = new ContentRootBasedGherkinTagTreeModel(getProject());
        model.buildModel();
        root = (ModelDataRoot) model.getRoot();
    }

    @Test
    void generateCategoriesIntoTreeModel() {
        validateCategories(List.of("Browser", "Device", "Excluded", "Other", "Test Suite", "Analytics and SEO", "Jira"));
    }

    @Test
    void generateTagsIntoTreeModel() {
        final var expectedCategoryTagMappings = Map.of(
            "Other", List.of("image", "youtube", "vimeo"),
            "Test Suite", List.of("smoke", "regression", "e2e"),
            "Device", List.of("mobile", "desktop", "tablet"),
            "Excluded", List.of("disabled", "skip"),
            "Browser", List.of("edge", "ff", "chrome"),
            "Analytics and SEO", List.of("sitemap"));

        validateCategoryToTagOrMetaMappings(expectedCategoryTagMappings, root);
    }

    @Test
    void generateGherkinFilesIntoTreeModel() {
        final var expectedMetaStoryFileMappings = ImmutableMap.<String, List<VirtualFile>>builder()
            .put("chrome", List.of(treeModel2Story, treeModelStory))
            .put("desktop", List.of(treeModelStory))
            .put("disabled", List.of(treeModel2Story))
            .put("e2e", List.of(treeModel2Story, treeModelStory))
            .put("edge", List.of(treeModel2Story, treeModelStory))
            .put("ff", List.of(treeModel2Story))
            .put("image", List.of(treeModel2Story, treeModelStory))
            .put("mobile", List.of(treeModel2Story))
            .put("regression", List.of(treeModelStory))
            .put("tablet", List.of(treeModelStory))
            .put("sitemap", List.of(treeModelStory))
            .put("skip", List.of(treeModelStory))
            .put("smoke", List.of(treeModel2Story))
            .put("vimeo", List.of(treeModelStory))
            .put("youtube", List.of(treeModelStory))
            .build();

        validateTagToFileMappings(expectedMetaStoryFileMappings, root);
    }

    //updateTreeForFile

    @Test
    void updateTreeModelAndStatisticsWhenFeatureFileIsDeleted() {
        var registry = TagOccurrencesRegistry.getInstance(getProject());
        assertThat(registry.getTagOccurrences()).containsKey("/src/TreeModel.story");

        invokeInWriteActionOnEDTAndWait(() -> treeModelStory.delete(this));
        model.updateModelForFile(psitreeModelStory);

        assertThat(registry.getTagOccurrences()).doesNotContainKey("/src/TreeModel.story");

        final var expectedCategoryTagMappingsAfter = Map.of(
            "Other", List.of("image"),
            "Test Suite", List.of("smoke", "e2e"),
            "Device", List.of("mobile"),
            "Excluded", List.of("disabled"),
            "Browser", List.of("edge", "ff", "chrome"));

        final var expectedTagGherkinFileMappingsAfter = ImmutableMap.<String, List<VirtualFile>>builder()
            .put("chrome", List.of(treeModel2Story))
            .put("disabled", List.of(treeModel2Story))
            .put("e2e", List.of(treeModel2Story))
            .put("edge", List.of(treeModel2Story))
            .put("ff", List.of(treeModel2Story))
            .put("image", List.of(treeModel2Story))
            .put("mobile", List.of(treeModel2Story))
            .put("smoke", List.of(treeModel2Story))
            .build();

        validateCategories(List.of("Other", "Test Suite", "Device", "Excluded", "Browser"));
        validateCategoryToTagOrMetaMappings(expectedCategoryTagMappingsAfter, root);
        validateTagToFileMappings(expectedTagGherkinFileMappingsAfter, root);
    }

    @Test
    void removesUnusedModuleInUpdatedTree() {
        PsiFile psiAGherkin = findPsiFile(treeModel2Story);

        assertThat(root.getModules()).hasSize(1);

        invokeInWriteActionOnEDTAndWait(() -> treeModelStory.delete(this));
        model.updateModelForFile(psitreeModelStory);

        invokeInWriteActionOnEDTAndWait(() -> treeModel2Story.delete(this));
        model.updateModelForFile(psiAGherkin);

        assertThat(root.getModules()).isEmpty();
    }

    @Test
    void updateTreeModelWhenGherkinTagIsDeleted() {
        PsiElement metaKey = getFirstMetaKeyForName(psitreeModelStory, "@desktop");
        executeCommandProcessorCommand(metaKey::delete, "Delete", "group.id");
        model.updateModelForFile(psitreeModelStory);

        final var expectedCategoryMetaMappings = Map.of(
            "Other", List.of("image", "vimeo", "youtube"),
            "Test Suite", List.of("smoke", "regression", "e2e"),
            "Device", List.of("mobile", "tablet"),
            "Excluded", List.of("disabled", "skip"),
            "Browser", List.of("edge", "ff", "chrome"),
            "Analytics and SEO", List.of("sitemap"));

        final var expectedMetaStoryFileMappings = ImmutableMap.<String, List<VirtualFile>>builder()
            .put("chrome", List.of(treeModel2Story, treeModelStory))
            .put("disabled", List.of(treeModel2Story))
            .put("e2e", List.of(treeModel2Story, treeModelStory))
            .put("edge", List.of(treeModel2Story, treeModelStory))
            .put("ff", List.of(treeModel2Story))
            .put("image", List.of(treeModel2Story, treeModelStory))
            .put("mobile", List.of(treeModel2Story))
            .put("regression", List.of(treeModelStory))
            .put("tablet", List.of(treeModelStory))
            .put("sitemap", List.of(treeModelStory))
            .put("skip", List.of(treeModelStory))
            .put("smoke", List.of(treeModel2Story))
            .put("vimeo", List.of(treeModelStory))
            .put("youtube", List.of(treeModelStory))
            .build();

        validateCategories(List.of("Other", "Test Suite", "Device", "Excluded", "Browser", "Analytics and SEO", "Jira"));
        validateCategoryToTagOrMetaMappings(expectedCategoryMetaMappings, root);
        validateTagToFileMappings(expectedMetaStoryFileMappings, root);
        assertThat(root.getModules().getFirst().getOther().get("desktop")).isEmpty();
    }

    @Test
    void updateTreeModelWhenMetaKeyIsReplaced() {
        PsiElement sitemapMetaKey = getFirstMetaKeyForName(psitreeModelStory, "@sitemap");
        PsiElement wipMetaKey = getFirstMetaKeyForName(configureByFile("Another story.story"), "@WIP");
        executeCommandProcessorCommand(() -> sitemapMetaKey.replace(wipMetaKey), "Replace", "group.id");
        model.updateModelForFile(psitreeModelStory);

        final var expectedCategoryMetaMappings = Map.of(
            "Other", List.of("image", "vimeo", "youtube"),
            "Test Suite", List.of("smoke", "regression", "e2e"),
            "Device", List.of("mobile", "desktop", "tablet"),
            "Excluded", List.of("disabled", "skip"),
            "Browser", List.of("edge", "ff", "chrome"),
            "Work in Progress", List.of("WIP"));

        final var expectedMetaStoryFileMappings = ImmutableMap.<String, List<VirtualFile>>builder()
            .put("chrome", List.of(treeModel2Story, treeModelStory))
            .put("desktop", List.of(treeModelStory))
            .put("disabled", List.of(treeModel2Story))
            .put("e2e", List.of(treeModel2Story, treeModelStory))
            .put("edge", List.of(treeModel2Story, treeModelStory))
            .put("ff", List.of(treeModel2Story))
            .put("image", List.of(treeModel2Story, treeModelStory))
            .put("mobile", List.of(treeModel2Story))
            .put("regression", List.of(treeModelStory))
            .put("tablet", List.of(treeModelStory))
            .put("skip", List.of(treeModelStory))
            .put("smoke", List.of(treeModel2Story))
            .put("vimeo", List.of(treeModelStory))
            .build();

        validateCategories(List.of("Other", "Test Suite", "Device", "Excluded", "Browser", "Work in Progress", "Jira"));
        validateCategoryToTagOrMetaMappings(expectedCategoryMetaMappings, root);
        validateTagToFileMappings(expectedMetaStoryFileMappings, root);
        assertThat(root.getModules().getFirst().getOther().get("sitemap")).isEmpty();
        assertThat(root.getModules().getFirst().findCategory("Work in Progress").get().get("WIP")).isNotEmpty();
    }

    @Test
    void tagOccurrenceIsUpdated() {
        PsiElement metaKey = getFirstMetaKeyForName(psitreeModelStory, "@youtube");

        Supplier<Integer> countGetter = () -> TagOccurrencesRegistry.getInstance(getProject())
            .getTagOccurrences()
            .get(root.getModules().getFirst().findTag("youtube").get().getFeatureFiles().getFirst().getPath())
            .get("youtube")
            .intValue();

        assertThat(countGetter.get()).isEqualTo(2);

        executeCommandProcessorCommand(metaKey::delete, "Delete", "group.id");
        model.updateModelForFile(psitreeModelStory);

        assertThat(countGetter.get()).isOne();
    }

    /**
     * - Have a regex based tag value, e.g. Jira -> #^[A-Z]+-[0-9]+$
     * - Add a Jira tag to a feature file.
     * - Add another regex mapping e.g. Trello (that would be processed later in the flow than Jira) with the same regex value e.g. Trello -> #^[A-Z]+-[0-9]+$
     * - Modify the previously added Jira tag in feature file.
     * - The tool window should remove the original category+tag (Jira + the tag) because it is no longer present, instead should show the new values (Trello + the new tag)
     */
    @Test
    void removeNodesForTagsMappedToMultipleDifferentCategories() {
        PsiElement metaKey = getFirstMetaKeyForName(psitreeModelStory, "@JIRA-1234");
        PsiElement wipMetaKey = getFirstMetaKeyForName(configureByFile("Another story.story"), "@TRELLO-9999");
        var registry = TagCategoryRegistry.getInstance(getProject());
        registry.putMappingsFrom(singletonList(new CategoryAndTags("Trello", "#^[A-Z]+-[0-9]+$")));

        executeCommandProcessorCommand(() -> metaKey.replace(wipMetaKey), "Replace", "group.id");
        model.updateModelForFile(psitreeModelStory);

        assertThat(root.getModules().getFirst().findCategory("Jira")).isEmpty();
        var trello = root.getModules().getFirst().findCategory("Trello");
        assertSoftly(s -> {
            s.assertThat(trello).isNotNull();
            s.assertThat(trello.get().get("TRELLO-9999")).isNotEmpty();
            s.assertThat(trello.get().get("TRELLO-9999").get().getFeatureFiles().getFirst().getDisplayName()).isEqualTo("TreeModel.story");
        });
    }

    @Test
    void updatesDisplayNamesOfFeatureFilesForFilesWithSameNameUnderATag() {
        configureByFile("nested/story_with_same_name.story");

        GherkinTagTreeModel model = new ContentRootBasedGherkinTagTreeModel(getProject());
        model.buildModel();
        Tag samename = ((ModelDataRoot) model.getRoot()).getModules().getFirst().findTag("samename").get();

        assertThat(samename.getFeatureFiles().getFirst().getDisplayName()).isEqualTo("story_with_same_name.story");

        PsiFile evenmoremore = configureByFile("nested/evenmore/evenmoremore/story_with_same_name.story");

        model.updateModelForFile(evenmoremore);

        assertSoftly(s -> {
            s.assertThat(samename.getFeatureFiles().getFirst().getDisplayName()).isEqualTo("story_with_same_name.story [nested]");
            s.assertThat(samename.getFeatureFiles().get(1).getDisplayName()).isEqualTo("story_with_same_name.story [nested/evenmore/evenmoremore]");
        });
    }
}
