//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static com.picimako.gherkin.SoftAsserts.assertSoftly;
import static com.picimako.gherkin.toolwindow.BDDTestSupport.getFirstGherkinTagForName;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.assertj.core.api.SoftAssertions;
import org.jetbrains.plugins.cucumber.psi.GherkinElementFactory;
import org.jetbrains.plugins.cucumber.psi.GherkinFeature;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;

import com.picimako.gherkin.MediumBasePlatformTestCase;
import com.picimako.gherkin.settings.CategoryAndTags;
import com.picimako.gherkin.toolwindow.nodetype.AbstractNodeType;
import com.picimako.gherkin.toolwindow.nodetype.Category;
import com.picimako.gherkin.toolwindow.nodetype.ContentRoot;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import com.picimako.gherkin.toolwindow.nodetype.Tag;

/**
 * Unit test for {@link GherkinTagTreeModel}.
 */
public class GherkinTagTreeModelTest extends MediumBasePlatformTestCase {

    private VirtualFile theGherkin;
    private VirtualFile aGherkin;
    private PsiFile psiTheGherkin;
    private GherkinTagTreeModel model;
    private ModelDataRoot root;

    private List<VirtualFile> theGherkinList;
    private List<VirtualFile> aGherkinList;
    private List<VirtualFile> mixedFiles;

    @Override
    protected String getTestDataPath() {
        return "testdata/features";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        psiTheGherkin = myFixture.configureByFile("the_gherkin.feature");
        theGherkin = psiTheGherkin.getVirtualFile();
        aGherkin = myFixture.configureByFile("A_gherkin.feature").getVirtualFile();
        model = new ContentRootBasedGherkinTagTreeModel(getProject());
        model.buildModel();
        root = (ModelDataRoot) model.getRoot();

        theGherkinList = Collections.singletonList(theGherkin);
        aGherkinList = Collections.singletonList(aGherkin);
        mixedFiles = List.of(aGherkin, theGherkin);
    }

    public void testShouldNotReInitExistingModelData() {
        model.buildModel();
        root.add(ContentRoot.createModule("module", getProject()));

        assertThat(root.getContentRoots()).extracting(ContentRoot::getDisplayName).containsExactly("light_idea_test_case", "module");
    }

    public void testGenerateCategoriesIntoTreeModel() {
        validateCategories(List.of("Browser", "Device", "Excluded", "Other", "Test Suite", "Analytics and SEO", "Jira"));
    }

    public void testGenerateTagsIntoTreeModel() {
        final var expectedCategoryTagMappings = Map.of(
            "Other", List.of("image", "youtube", "vimeo"),
            "Test Suite", List.of("smoke", "regression", "e2e"),
            "Device", List.of("mobile", "desktop", "tablet"),
            "Excluded", List.of("disabled", "skip"),
            "Browser", List.of("edge", "ff", "chrome"),
            "Analytics and SEO", List.of("sitemap"));

        validateCategoryToTagMappings(expectedCategoryTagMappings, root);
    }

    public void testGenerateGherkinFilesIntoTreeModel() {
        final var expectedTagGherkinFileMappings = buildTagToFileMapping(
            List.of("desktop", "regression", "tablet", "sitemap", "skip", "vimeo", "youtube"),
            List.of("disabled", "ff", "mobile", "smoke"),
            List.of("chrome", "e2e", "edge", "image"));

        validateTagToGherkinFileMappings(expectedTagGherkinFileMappings, root);
    }

    //updateTreeForFile

    public void testUpdateTreeModelAndStatisticsWhenFeatureFileIsDeleted() throws IOException {
        var registry = TagOccurrencesRegistry.getInstance(getProject());
        assertThat(registry.getTagOccurrences()).containsKey("/src/the_gherkin.feature");

        WriteAction.run(() -> theGherkin.delete(this));
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

    public void testRemovesUnusedModuleInUpdatedTree() throws IOException {
        PsiFile psiAGherkin = PsiManager.getInstance(getProject()).findFile(aGherkin);

        assertThat(root.getModules()).hasSize(1);

        WriteAction.run(() -> theGherkin.delete(this));
        model.updateModelForFile(psiTheGherkin);

        WriteAction.run(() -> aGherkin.delete(this));
        model.updateModelForFile(psiAGherkin);

        assertThat(root.getModules()).isEmpty();
    }

    public void testUpdateTreeModelWhenGherkinTagIsDeleted() {
        GherkinTag tag = getFirstGherkinTagForName(psiTheGherkin, "@desktop");
        WriteAction.run(() -> CommandProcessor.getInstance().executeCommand(getProject(), () -> tag.delete(), "Delete", "group.id"));
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
        assertThat(root.getModules().get(0).getOther().get("desktop")).isEmpty();
    }

    public void testUpdateTreeModelWhenGherkinTagIsReplaced() {
        GherkinTag tag = getFirstGherkinTagForName(psiTheGherkin, "@sitemap");
        PsiElement[] topLevelElements = GherkinElementFactory.getTopLevelElements(getProject(), "@WIP\nFeature: Wip feature\n");
        WriteAction.run(() -> CommandProcessor.getInstance().executeCommand(getProject(), () -> tag.replace(topLevelElements[0]), "Replace", "group.id"));
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
        assertThat(root.getModules().get(0).getOther().get("sitemap")).isEmpty();
        assertThat(root.getModules().get(0).findCategory("Work in Progress").get().get("WIP")).isNotEmpty();
    }

    public void testTagOccurrenceIsUpdated() {
        GherkinTag tag = getFirstGherkinTagForName(psiTheGherkin, "@youtube");

        Supplier<Integer> countGetter = () -> TagOccurrencesRegistry.getInstance(getProject())
            .getTagOccurrences()
            .get(root.getModules().get(0).findTag("youtube").get().getFeatureFiles().get(0).getPath())
            .get("youtube")
            .intValue();

        assertThat(countGetter.get()).isEqualTo(2);

        WriteAction.run(() -> CommandProcessor.getInstance().executeCommand(getProject(), () -> tag.delete(), "Delete", "group.id"));
        model.updateModelForFile(psiTheGherkin);

        assertThat(countGetter.get()).isEqualTo(1);
    }

    /**
     * - Have a regex based tag value, e.g. Jira -> #^[A-Z]+-[0-9]+$
     * - Add a Jira tag to a feature file.
     * - Add another regex mapping e.g. Trello (that would be processed later in the flow than Jira) with the same regex value e.g. Trello -> #^[A-Z]+-[0-9]+$
     * - Modify the previously added Jira tag in feature file.
     * - The tool window should remove the original category+tag (Jira + the tag) because it is no longer present, instead should show the new values (Trello + the new tag)
     */
    public void testRemoveNodesForTagsMappedToMultipleDifferentCategories() {
        GherkinTag tag = getFirstGherkinTagForName(psiTheGherkin, "@JIRA-1234");
        var registry = TagCategoryRegistry.getInstance(getProject());
        registry.putMappingsFrom(singletonList(new CategoryAndTags("Trello", "#^[A-Z]+-[0-9]+$")));

        PsiElement[] topLevelElements = GherkinElementFactory.getTopLevelElements(getProject(), "@TRELLO-9999\nFeature: Wip feature\n");
        WriteAction.run(() -> CommandProcessor.getInstance().executeCommand(getProject(), () -> tag.replace(topLevelElements[0]), "Replace", "group.id"));
        model.updateModelForFile(psiTheGherkin);

        assertThat(root.getModules().get(0).findCategory("Jira")).isEmpty();
        Optional<Category> trello = root.getModules().get(0).findCategory("Trello");
        assertSoftly(
            softly -> softly.assertThat(trello).isNotNull(),
            softly -> softly.assertThat(trello.get().get("TRELLO-9999")).isNotNull(),
            softly -> softly.assertThat(trello.get().get("TRELLO-9999").get().getFeatureFiles().get(0).getDisplayName()).isEqualTo("the_gherkin.feature")
        );
    }

    public void testUpdatesDisplayNamesOfFeatureFilesForFilesWithSameNameUnderATag() {
        myFixture.configureByFile("nested/gherkin_with_same_name.feature");
        PsiFile evenmoremore = myFixture.configureByFile("nested/evenmore/evenmoremore/gherkin_with_same_name.feature");

        GherkinTagTreeModel model = new ContentRootBasedGherkinTagTreeModel(getProject());
        model.buildModel();
        Tag samename = ((ModelDataRoot) model.getRoot()).getModules().get(0).findTag("samename").get();

        assertThat(samename.getFeatureFiles()).extracting(AbstractNodeType::getDisplayName)
            .containsExactlyInAnyOrder("gherkin_with_same_name.feature [Almost same name]", "gherkin_with_same_name.feature [Same name]");

        GherkinFeature feature = GherkinElementFactory.createFeatureFromText(getProject(), "Feature: Same name");
        WriteAction.run(() -> CommandProcessor.getInstance().executeCommand(getProject(), () -> ((GherkinFile) evenmoremore).getFeatures()[0].replace(feature), "Replace", "group.id"));

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
        SoftAssertions softly = new SoftAssertions();
        expectedCategoryTagMappings.forEach((category, tags) -> softly
            .assertThat(root.getModules().get(0).findCategory(category).get().getTags())
            .extracting(Tag::getDisplayName)
            .containsExactlyInAnyOrderElementsOf(tags));
        softly.assertAll();
    }

    private void validateTagToGherkinFileMappings(Map<String, List<VirtualFile>> expectedTagGherkinFileMappings, ModelDataRoot root) {
        SoftAssertions softly = new SoftAssertions();
        Map<String, Tag> tags = root.getContentRoots().get(0).getCategories().stream()
            .flatMap(category -> category.getTags().stream())
            .collect(toMap(Tag::getDisplayName, Function.identity()));
        expectedTagGherkinFileMappings.forEach((tag, gherkinFiles) -> {
            assertThat(tags).containsKey(tag);
            softly.assertThat(tags.keySet()).contains(tag);
            softly.assertThat(tags.get(tag).getGherkinFiles()).containsExactlyInAnyOrderElementsOf(gherkinFiles);
        });
        softly.assertAll();
    }
}
