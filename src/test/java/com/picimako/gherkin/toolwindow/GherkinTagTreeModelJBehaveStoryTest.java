//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static com.picimako.gherkin.toolwindow.BDDTestSupport.getFirstMetaKeyForName;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.io.IOException;
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

import com.picimako.gherkin.MediumBasePlatformTestCase;
import com.picimako.gherkin.settings.CategoryAndTags;
import com.picimako.gherkin.toolwindow.nodetype.Category;
import com.picimako.gherkin.toolwindow.nodetype.ContentRoot;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import com.picimako.gherkin.toolwindow.nodetype.Tag;

/**
 * Unit test for {@link GherkinTagTreeModel}.
 */
public class GherkinTagTreeModelJBehaveStoryTest extends MediumBasePlatformTestCase {

    private VirtualFile treeModelStory;
    private VirtualFile treeModel2Story;
    private PsiFile psitreeModelStory;
    private GherkinTagTreeModel model;
    private ModelDataRoot root;

    @Override
    protected String getTestDataPath() {
        return "testdata/features";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        psitreeModelStory = myFixture.configureByFile("TreeModel.story");
        treeModelStory = psitreeModelStory.getVirtualFile();
        treeModel2Story = myFixture.configureByFile("TreeModel2.story").getVirtualFile();
        model = new ContentRootBasedGherkinTagTreeModel(getProject());
        model.buildModel();
        root = (ModelDataRoot) model.getRoot();
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

        validateCategoryToMetaMappings(expectedCategoryTagMappings, root);
    }

    public void testGenerateGherkinFilesIntoTreeModel() {
        final var expectedMetaStoryFileMappings = new HashMap<String, List<VirtualFile>>();
        expectedMetaStoryFileMappings.put("chrome", List.of(treeModel2Story, treeModelStory));
        expectedMetaStoryFileMappings.put("desktop", List.of(treeModelStory));
        expectedMetaStoryFileMappings.put("disabled", List.of(treeModel2Story));
        expectedMetaStoryFileMappings.put("e2e", List.of(treeModel2Story, treeModelStory));
        expectedMetaStoryFileMappings.put("edge", List.of(treeModel2Story, treeModelStory));
        expectedMetaStoryFileMappings.put("ff", List.of(treeModel2Story));
        expectedMetaStoryFileMappings.put("image", List.of(treeModel2Story, treeModelStory));
        expectedMetaStoryFileMappings.put("mobile", List.of(treeModel2Story));
        expectedMetaStoryFileMappings.put("regression", List.of(treeModelStory));
        expectedMetaStoryFileMappings.put("tablet", List.of(treeModelStory));
        expectedMetaStoryFileMappings.put("sitemap", List.of(treeModelStory));
        expectedMetaStoryFileMappings.put("skip", List.of(treeModelStory));
        expectedMetaStoryFileMappings.put("smoke", List.of(treeModel2Story));
        expectedMetaStoryFileMappings.put("vimeo", List.of(treeModelStory));
        expectedMetaStoryFileMappings.put("youtube", List.of(treeModelStory));

        validateTagToGherkinFileMappings(expectedMetaStoryFileMappings, root);
    }

    //updateTreeForFile

    public void testUpdateTreeModelAndStatisticsWhenFeatureFileIsDeleted() throws IOException {
        var registry = TagOccurrencesRegistry.getInstance(getProject());
        assertThat(registry.getTagOccurrences()).containsKey("/src/TreeModel.story");

        WriteAction.run(() -> treeModelStory.delete(this));
        model.updateModelForFile(psitreeModelStory);

        assertThat(registry.getTagOccurrences()).doesNotContainKey("/src/TreeModel.story");

        final var expectedCategoryTagMappingsAfter = Map.of(
            "Other", List.of("image"),
            "Test Suite", List.of("smoke", "e2e"),
            "Device", List.of("mobile"),
            "Excluded", List.of("disabled"),
            "Browser", List.of("edge", "ff", "chrome"));

        final var expectedTagGherkinFileMappingsAfter = new HashMap<String, List<VirtualFile>>();
        expectedTagGherkinFileMappingsAfter.put("chrome", List.of(treeModel2Story));
        expectedTagGherkinFileMappingsAfter.put("disabled", List.of(treeModel2Story));
        expectedTagGherkinFileMappingsAfter.put("e2e", List.of(treeModel2Story));
        expectedTagGherkinFileMappingsAfter.put("edge", List.of(treeModel2Story));
        expectedTagGherkinFileMappingsAfter.put("ff", List.of(treeModel2Story));
        expectedTagGherkinFileMappingsAfter.put("image", List.of(treeModel2Story));
        expectedTagGherkinFileMappingsAfter.put("mobile", List.of(treeModel2Story));
        expectedTagGherkinFileMappingsAfter.put("smoke", List.of(treeModel2Story));

        validateCategories(List.of("Other", "Test Suite", "Device", "Excluded", "Browser"));
        validateCategoryToMetaMappings(expectedCategoryTagMappingsAfter, root);
        validateTagToGherkinFileMappings(expectedTagGherkinFileMappingsAfter, root);
    }

    public void testRemovesUnusedModuleInUpdatedTree() throws IOException {
        PsiFile psiAGherkin = PsiManager.getInstance(getProject()).findFile(treeModel2Story);

        assertThat(root.getModules()).hasSize(1);

        WriteAction.run(() -> treeModelStory.delete(this));
        model.updateModelForFile(psitreeModelStory);

        WriteAction.run(() -> treeModel2Story.delete(this));
        model.updateModelForFile(psiAGherkin);

        assertThat(root.getModules()).isEmpty();
    }

    public void testUpdateTreeModelWhenGherkinTagIsDeleted() {
        PsiElement metaKey = getFirstMetaKeyForName(psitreeModelStory, "@desktop");
        WriteAction.run(() -> CommandProcessor.getInstance().executeCommand(getProject(), () -> metaKey.delete(), "Delete", "group.id"));
        model.updateModelForFile(psitreeModelStory);

        final var expectedCategoryMetaMappings = Map.of(
            "Other", List.of("image", "vimeo", "youtube"),
            "Test Suite", List.of("smoke", "regression", "e2e"),
            "Device", List.of("mobile", "tablet"),
            "Excluded", List.of("disabled", "skip"),
            "Browser", List.of("edge", "ff", "chrome"),
            "Analytics and SEO", List.of("sitemap"));

        final var expectedMetaStoryFileMappings = new HashMap<String, List<VirtualFile>>();
        expectedMetaStoryFileMappings.put("chrome", List.of(treeModel2Story, treeModelStory));
        expectedMetaStoryFileMappings.put("disabled", List.of(treeModel2Story));
        expectedMetaStoryFileMappings.put("e2e", List.of(treeModel2Story, treeModelStory));
        expectedMetaStoryFileMappings.put("edge", List.of(treeModel2Story, treeModelStory));
        expectedMetaStoryFileMappings.put("ff", List.of(treeModel2Story));
        expectedMetaStoryFileMappings.put("image", List.of(treeModel2Story, treeModelStory));
        expectedMetaStoryFileMappings.put("mobile", List.of(treeModel2Story));
        expectedMetaStoryFileMappings.put("regression", List.of(treeModelStory));
        expectedMetaStoryFileMappings.put("tablet", List.of(treeModelStory));
        expectedMetaStoryFileMappings.put("sitemap", List.of(treeModelStory));
        expectedMetaStoryFileMappings.put("skip", List.of(treeModelStory));
        expectedMetaStoryFileMappings.put("smoke", List.of(treeModel2Story));
        expectedMetaStoryFileMappings.put("vimeo", List.of(treeModelStory));
        expectedMetaStoryFileMappings.put("youtube", List.of(treeModelStory));

        validateCategories(List.of("Other", "Test Suite", "Device", "Excluded", "Browser", "Analytics and SEO", "Jira"));
        validateCategoryToMetaMappings(expectedCategoryMetaMappings, root);
        validateTagToGherkinFileMappings(expectedMetaStoryFileMappings, root);
        assertThat(root.getModules().getFirst().getOther().get("desktop")).isEmpty();
    }

    public void testUpdateTreeModelWhenMetaKeyIsReplaced() {
        PsiElement sitemapMetaKey = getFirstMetaKeyForName(psitreeModelStory, "@sitemap");
        PsiElement wipMetaKey = getFirstMetaKeyForName(myFixture.configureByFile("Another story.story"), "@WIP");
        WriteAction.run(() -> CommandProcessor.getInstance().executeCommand(getProject(), () -> sitemapMetaKey.replace(wipMetaKey), "Replace", "group.id"));
        model.updateModelForFile(psitreeModelStory);

        final var expectedCategoryMetaMappings = Map.of(
            "Other", List.of("image", "vimeo", "youtube"),
            "Test Suite", List.of("smoke", "regression", "e2e"),
            "Device", List.of("mobile", "desktop", "tablet"),
            "Excluded", List.of("disabled", "skip"),
            "Browser", List.of("edge", "ff", "chrome"),
            "Work in Progress", List.of("WIP"));

        final var expectedMetaStoryFileMappings = new HashMap<String, List<VirtualFile>>();
        expectedMetaStoryFileMappings.put("chrome", List.of(treeModel2Story, treeModelStory));
        expectedMetaStoryFileMappings.put("desktop", List.of(treeModelStory));
        expectedMetaStoryFileMappings.put("disabled", List.of(treeModel2Story));
        expectedMetaStoryFileMappings.put("e2e", List.of(treeModel2Story, treeModelStory));
        expectedMetaStoryFileMappings.put("edge", List.of(treeModel2Story, treeModelStory));
        expectedMetaStoryFileMappings.put("ff", List.of(treeModel2Story));
        expectedMetaStoryFileMappings.put("image", List.of(treeModel2Story, treeModelStory));
        expectedMetaStoryFileMappings.put("mobile", List.of(treeModel2Story));
        expectedMetaStoryFileMappings.put("regression", List.of(treeModelStory));
        expectedMetaStoryFileMappings.put("tablet", List.of(treeModelStory));
        expectedMetaStoryFileMappings.put("skip", List.of(treeModelStory));
        expectedMetaStoryFileMappings.put("smoke", List.of(treeModel2Story));
        expectedMetaStoryFileMappings.put("vimeo", List.of(treeModelStory));

        validateCategories(List.of("Other", "Test Suite", "Device", "Excluded", "Browser", "Work in Progress", "Jira"));
        validateCategoryToMetaMappings(expectedCategoryMetaMappings, root);
        validateTagToGherkinFileMappings(expectedMetaStoryFileMappings, root);
        assertThat(root.getModules().getFirst().getOther().get("sitemap")).isEmpty();
        assertThat(root.getModules().getFirst().findCategory("Work in Progress").get().get("WIP")).isNotEmpty();
    }

    public void testTagOccurrenceIsUpdated() {
        PsiElement metaKey = getFirstMetaKeyForName(psitreeModelStory, "@youtube");

        Supplier<Integer> countGetter = () -> TagOccurrencesRegistry.getInstance(getProject())
            .getTagOccurrences()
            .get(root.getModules().getFirst().findTag("youtube").get().getFeatureFiles().getFirst().getPath())
            .get("youtube")
            .intValue();

        assertThat(countGetter.get()).isEqualTo(2);

        WriteAction.run(() -> CommandProcessor.getInstance().executeCommand(getProject(), () -> metaKey.delete(), "Delete", "group.id"));
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
    public void testRemoveNodesForTagsMappedToMultipleDifferentCategories() {
        PsiElement metaKey = getFirstMetaKeyForName(psitreeModelStory, "@JIRA-1234");
        PsiElement wipMetaKey = getFirstMetaKeyForName(myFixture.configureByFile("Another story.story"), "@TRELLO-9999");
        var registry = TagCategoryRegistry.getInstance(getProject());
        registry.putMappingsFrom(singletonList(new CategoryAndTags("Trello", "#^[A-Z]+-[0-9]+$")));

        WriteAction.run(() -> CommandProcessor.getInstance().executeCommand(getProject(), () -> metaKey.replace(wipMetaKey), "Replace", "group.id"));
        model.updateModelForFile(psitreeModelStory);

        assertThat(root.getModules().getFirst().findCategory("Jira")).isEmpty();
        Optional<Category> trello = root.getModules().getFirst().findCategory("Trello");
        assertSoftly(s -> {
            s.assertThat(trello).isNotNull();
            s.assertThat(trello.get().get("TRELLO-9999")).isNotNull();
            s.assertThat(trello.get().get("TRELLO-9999").get().getFeatureFiles().getFirst().getDisplayName()).isEqualTo("TreeModel.story");
        });
    }

    public void testUpdatesDisplayNamesOfFeatureFilesForFilesWithSameNameUnderATag() {
        myFixture.configureByFile("nested/story_with_same_name.story");

        GherkinTagTreeModel model = new ContentRootBasedGherkinTagTreeModel(getProject());
        model.buildModel();
        Tag samename = ((ModelDataRoot) model.getRoot()).getModules().getFirst().findTag("samename").get();

        assertThat(samename.getFeatureFiles().getFirst().getDisplayName()).isEqualTo("story_with_same_name.story");

        PsiFile evenmoremore = myFixture.configureByFile("nested/evenmore/evenmoremore/story_with_same_name.story");

        model.updateModelForFile(evenmoremore);

        assertSoftly(s -> {
            s.assertThat(samename.getFeatureFiles().getFirst().getDisplayName()).isEqualTo("story_with_same_name.story [nested]");
            s.assertThat(samename.getFeatureFiles().get(1).getDisplayName()).isEqualTo("story_with_same_name.story [nested/evenmore/evenmoremore]");
        });
    }

    private void validateCategories(List<String> categories) {
        assertThat(root.getModules())
            .flatMap(ContentRoot::getCategories)
            .extracting(Category::getDisplayName)
            .containsExactlyInAnyOrderElementsOf(categories);
    }

    private void validateCategoryToMetaMappings(Map<String, List<String>> expectedCategoryMetaMappings, ModelDataRoot root) {
        SoftAssertions softly = new SoftAssertions();
        expectedCategoryMetaMappings.forEach((category, tags) -> softly
            .assertThat(root.getModules().getFirst().findCategory(category).get().getTags())
            .extracting(Tag::getDisplayName)
            .containsExactlyInAnyOrderElementsOf(tags));
        softly.assertAll();
    }

    private void validateTagToGherkinFileMappings(Map<String, List<VirtualFile>> expectedTagStoryFileMappings, ModelDataRoot root) {
        SoftAssertions softly = new SoftAssertions();
        Map<String, Tag> tags = root.getContentRoots().getFirst().getCategories().stream()
            .flatMap(category -> category.getTags().stream())
            .collect(toMap(Tag::getDisplayName, Function.identity()));
        expectedTagStoryFileMappings.forEach((tag, gherkinFiles) -> {
            assertThat(tags).containsKey(tag);
            softly.assertThat(tags.keySet()).contains(tag);
            softly.assertThat(tags.get(tag).getGherkinFiles()).containsExactlyInAnyOrderElementsOf(gherkinFiles);
        });
        softly.assertAll();
    }
}
