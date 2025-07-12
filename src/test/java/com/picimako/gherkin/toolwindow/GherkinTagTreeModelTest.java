//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static com.picimako.gherkin.toolwindow.BDDTestSupport.getFirstGherkinTagForName;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.picimako.gherkin.MediumBasePlatformTestCase;
import com.picimako.gherkin.settings.CategoryAndTags;
import com.picimako.gherkin.toolwindow.nodetype.AbstractNodeType;
import com.picimako.gherkin.toolwindow.nodetype.Category;
import com.picimako.gherkin.toolwindow.nodetype.ContentRoot;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import com.picimako.gherkin.toolwindow.nodetype.Tag;
import org.jetbrains.plugins.cucumber.psi.GherkinElementFactory;
import org.jetbrains.plugins.cucumber.psi.GherkinFeature;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link GherkinTagTreeModel}.
 */
final class GherkinTagTreeModelTest extends MediumBasePlatformTestCase {
    private VirtualFile theGherkin;
    private VirtualFile aGherkin;
    private PsiFile psiTheGherkin;
    private GherkinTagTreeModel model;
    private ModelDataRoot root;

    private List<VirtualFile> theGherkinList;
    private List<VirtualFile> aGherkinList;
    private List<VirtualFile> mixedFiles;

    @BeforeEach
    void setUp() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        psiTheGherkin = getFixture().configureByFile("the_gherkin.feature");
        theGherkin = psiTheGherkin.getVirtualFile();
        aGherkin = configureVirtualFile("A_gherkin.feature");
        model = new ContentRootBasedGherkinTagTreeModel(getProject());
        model.buildModel();
        root = (ModelDataRoot) model.getRoot();

        theGherkinList = Collections.singletonList(theGherkin);
        aGherkinList = Collections.singletonList(aGherkin);
        mixedFiles = List.of(aGherkin, theGherkin);
    }

    @Test
    void shouldNotReInitExistingModelData() {
        model.buildModel();
        root.add(ContentRoot.createModule("module", getProject()));

        assertThat(root.getContentRoots()).extracting(ContentRoot::getDisplayName).containsExactly("light_idea_test_case", "module");
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

        validateCategoryToTagMappings(expectedCategoryTagMappings, root);
    }

    @Test
    void generateGherkinFilesIntoTreeModel() {
        final var expectedTagGherkinFileMappings = buildTagToFileMapping(
            List.of("desktop", "regression", "tablet", "sitemap", "skip", "vimeo", "youtube"),
            List.of("disabled", "ff", "mobile", "smoke"),
            List.of("chrome", "e2e", "edge", "image"));

        validateTagToGherkinFileMappings(expectedTagGherkinFileMappings, root);
    }

    //updateTreeForFile

    @Test
    void updateTreeModelAndStatisticsWhenFeatureFileIsDeleted() {
        var registry = TagOccurrencesRegistry.getInstance(getProject());
        assertThat(registry.getTagOccurrences()).containsKey("/src/the_gherkin.feature");

        invokeInWriteActionOnEDTAndWait(() -> theGherkin.delete(this));
        model.updateModelForFile(psiTheGherkin);

        assertThat(registry.getTagOccurrences()).doesNotContainKey("/src/the_gherkin.feature");

        final var expectedCategoryTagMappingsAfter = Map.of(
            "Other", List.of("image"),
            "Test Suite", List.of("smoke", "e2e"),
            "Device", List.of("mobile"),
            "Excluded", List.of("disabled"),
            "Browser", List.of("edge", "ff", "chrome"));

        final var expectedTagGherkinFileMappingsAfter = buildTagToFileMapping(List.of(),
            List.of("chrome", "disabled", "e2e", "edge", "ff", "image", "mobile", "smoke"), List.of());

        validateCategories(List.of("Other", "Test Suite", "Device", "Excluded", "Browser"));
        validateCategoryToTagMappings(expectedCategoryTagMappingsAfter, root);
        validateTagToGherkinFileMappings(expectedTagGherkinFileMappingsAfter, root);
    }

    @Test
    void removesUnusedModuleInUpdatedTree() {
        PsiFile psiAGherkin = findPsiFile(aGherkin);

        assertThat(root.getModules()).hasSize(1);

        invokeInWriteActionOnEDTAndWait(() -> theGherkin.delete(this));
        model.updateModelForFile(psiTheGherkin);

        invokeInWriteActionOnEDTAndWait(() -> aGherkin.delete(this));
        model.updateModelForFile(psiAGherkin);

        assertThat(root.getModules()).isEmpty();
    }

    @Test
    void updateTreeModelWhenGherkinTagIsDeleted() {
        GherkinTag tag = getFirstGherkinTagForName(psiTheGherkin, "@desktop");
        executeCommandProcessorCommand(() -> tag.delete(), "Delete", "group.id");
        model.updateModelForFile(psiTheGherkin);

        final var expectedCategoryTagMappings = Map.of(
            "Other", List.of("image", "vimeo", "youtube"),
            "Test Suite", List.of("smoke", "regression", "e2e"),
            "Device", List.of("mobile", "tablet"),
            "Excluded", List.of("disabled", "skip"),
            "Browser", List.of("edge", "ff", "chrome"),
            "Analytics and SEO", List.of("sitemap"));

        final var expectedTagGherkinFileMappings = buildTagToFileMapping(
            List.of("regression", "tablet", "sitemap", "skip", "vimeo", "youtube"),
            List.of("disabled", "ff", "mobile", "smoke"),
            List.of("chrome", "e2e", "edge", "image"));

        validateCategories(List.of("Other", "Test Suite", "Device", "Excluded", "Browser", "Analytics and SEO", "Jira"));
        validateCategoryToTagMappings(expectedCategoryTagMappings, root);
        validateTagToGherkinFileMappings(expectedTagGherkinFileMappings, root);
        assertThat(root.getModules().getFirst().getOther().get("desktop")).isEmpty();
    }

    @Test
    void updateTreeModelWhenGherkinTagIsReplaced() {
        GherkinTag tag = getFirstGherkinTagForName(psiTheGherkin, "@sitemap");
        PsiElement[] topLevelElements = ReadAction.compute(() -> GherkinElementFactory.getTopLevelElements(getProject(), "@WIP\nFeature: Wip feature\n"));
        executeCommandProcessorCommand(() -> tag.replace(topLevelElements[0]), "Replace", "group.id");
        model.updateModelForFile(psiTheGherkin);

        final var expectedCategoryTagMappings = Map.of(
            "Other", List.of("image", "vimeo", "youtube"),
            "Test Suite", List.of("smoke", "regression", "e2e"),
            "Device", List.of("mobile", "desktop", "tablet"),
            "Excluded", List.of("disabled", "skip"),
            "Browser", List.of("edge", "ff", "chrome"),
            "Work in Progress", List.of("WIP"));

        final var expectedTagGherkinFileMappings = buildTagToFileMapping(
            List.of("desktop", "regression", "tablet", "skip", "vimeo"),
            List.of("disabled", "ff", "mobile", "smoke"),
            List.of("chrome", "e2e", "edge", "image"));

        validateCategories(List.of("Other", "Test Suite", "Device", "Excluded", "Browser", "Work in Progress", "Jira"));
        validateCategoryToTagMappings(expectedCategoryTagMappings, root);
        validateTagToGherkinFileMappings(expectedTagGherkinFileMappings, root);
        assertThat(root.getModules().getFirst().getOther().get("sitemap")).isEmpty();
        assertThat(root.getModules().getFirst().findCategory("Work in Progress").get().get("WIP")).isNotEmpty();
    }

    @Test
    void tagOccurrenceIsUpdated() {
        GherkinTag tag = getFirstGherkinTagForName(psiTheGherkin, "@youtube");

        Supplier<Integer> countGetter = () -> TagOccurrencesRegistry.getInstance(getProject())
            .getTagOccurrences()
            .get(root.getModules().getFirst().findTag("youtube").get().getFeatureFiles().getFirst().getPath())
            .get("youtube")
            .intValue();

        assertThat(countGetter.get()).isEqualTo(2);

        executeCommandProcessorCommand(() -> tag.delete(), "Delete", "group.id");
        model.updateModelForFile(psiTheGherkin);

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
        GherkinTag tag = getFirstGherkinTagForName(psiTheGherkin, "@JIRA-1234");
        var registry = TagCategoryRegistry.getInstance(getProject());
        registry.putMappingsFrom(singletonList(new CategoryAndTags("Trello", "#^[A-Z]+-[0-9]+$")));

        PsiElement[] topLevelElements = ReadAction.compute(() -> GherkinElementFactory.getTopLevelElements(getProject(), "@TRELLO-9999\nFeature: Wip feature\n"));
        executeCommandProcessorCommand(() -> tag.replace(topLevelElements[0]), "Replace", "group.id");
        model.updateModelForFile(psiTheGherkin);

        assertThat(root.getModules().getFirst().findCategory("Jira")).isEmpty();
        Optional<Category> trello = root.getModules().getFirst().findCategory("Trello");
        assertSoftly(s -> {
            s.assertThat(trello).isNotNull();
            s.assertThat(trello.get().get("TRELLO-9999")).isNotNull();
            s.assertThat(trello.get().get("TRELLO-9999").get().getFeatureFiles().getFirst().getDisplayName()).isEqualTo("the_gherkin.feature");
        });
    }

    @Test
    void updatesDisplayNamesOfFeatureFilesForFilesWithSameNameUnderATag() {
        getFixture().configureByFile("nested/gherkin_with_same_name.feature");
        PsiFile evenmoremore = getFixture().configureByFile("nested/evenmore/evenmoremore/gherkin_with_same_name.feature");

        GherkinTagTreeModel model = new ContentRootBasedGherkinTagTreeModel(getProject());
        model.buildModel();
        Tag samename = ((ModelDataRoot) model.getRoot()).getModules().getFirst().findTag("samename").get();

        assertThat(samename.getFeatureFiles()).extracting(AbstractNodeType::getDisplayName)
            .containsExactlyInAnyOrder("gherkin_with_same_name.feature [Almost same name]", "gherkin_with_same_name.feature [Same name]");

        GherkinFeature feature = ReadAction.compute(() -> GherkinElementFactory.createFeatureFromText(getProject(), "Feature: Same name"));
        executeCommandProcessorCommand(() -> ((GherkinFile) evenmoremore).getFeatures()[0].replace(feature), "Replace", "group.id");

        model.updateModelForFile(evenmoremore);

        assertThat(samename.getFeatureFiles()).extracting(AbstractNodeType::getDisplayName)
            .containsExactlyInAnyOrder("gherkin_with_same_name.feature [nested/evenmore/evenmoremore]", "gherkin_with_same_name.feature [nested]");
    }

    //Helper methods

    private HashMap<String, List<VirtualFile>> buildTagToFileMapping(List<String> theGherkinCategories, List<String> aGherkinCategories,
                                                                     List<String> mixedCategories) {
        final var expectedTagGherkinFileMappings = new HashMap<String, List<VirtualFile>>();
        theGherkinCategories.forEach(s -> expectedTagGherkinFileMappings.put(s, theGherkinList));
        aGherkinCategories.forEach(s -> expectedTagGherkinFileMappings.put(s, aGherkinList));
        mixedCategories.forEach(s -> expectedTagGherkinFileMappings.put(s, mixedFiles));
        return expectedTagGherkinFileMappings;
    }

    private void validateCategories(List<String> categories) {
        assertThat(root.getModules())
            .flatMap(ContentRoot::getCategories)
            .extracting(Category::getDisplayName)
            .containsExactlyInAnyOrderElementsOf(categories);
    }

    private void validateCategoryToTagMappings(Map<String, List<String>> expectedCategoryTagMappings, ModelDataRoot root) {
        assertSoftly(s ->
            expectedCategoryTagMappings.forEach((category, tags) ->
                s.assertThat(root.getModules().getFirst().findCategory(category).get().getTags())
                    .extracting(Tag::getDisplayName)
                    .containsExactlyInAnyOrderElementsOf(tags)));
    }

    private void validateTagToGherkinFileMappings(Map<String, List<VirtualFile>> expectedTagGherkinFileMappings, ModelDataRoot root) {
        Map<String, Tag> tags = root.getContentRoots().getFirst().getCategories().stream()
            .flatMap(category -> category.getTags().stream())
            .collect(toMap(Tag::getDisplayName, Function.identity()));
        assertSoftly(s ->
            expectedTagGherkinFileMappings.forEach((tag, gherkinFiles) -> {
                assertThat(tags).containsKey(tag);
                s.assertThat(tags.keySet()).contains(tag);
                s.assertThat(tags.get(tag).getGherkinFiles()).containsExactlyInAnyOrderElementsOf(gherkinFiles);
            }));
    }
}
