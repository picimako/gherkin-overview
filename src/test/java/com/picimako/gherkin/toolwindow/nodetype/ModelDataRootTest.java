//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.picimako.gherkin.MediumBasePlatformTestCase;
import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;
import com.picimako.gherkin.toolwindow.LayoutType;
import com.picimako.gherkin.toolwindow.ProjectBDDTypeService;
import com.picimako.gherkin.toolwindow.StatisticsType;
import com.picimako.gherkin.toolwindow.TagOccurrencesRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ModelDataRoot}.
 */
final class ModelDataRootTest extends MediumBasePlatformTestCase {

    @BeforeEach
    void setUp() {
        TagOccurrencesRegistry.getInstance(getProject()).init(1);
    }

    //initData

    @Test
    void initsProjectDataIfNotYetInitialized() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);
        var modelDataRoot = new ModelDataRoot(getProject());

        assertThat(modelDataRoot.isInitializedAsProjectData()).isFalse();

        modelDataRoot.add(ContentRoot.createModule("module", getProject()));
        modelDataRoot.initData();

        assertThat(modelDataRoot.getContentRoots()).isNotEmpty();

        configureToolWindowLayout(LayoutType.NO_GROUPING);
        modelDataRoot.initData();

        assertThat(modelDataRoot.isInitializedAsProjectData()).isTrue();
    }

    @Test
    void doesntInitProjectDataIfInitialized() {
        configureToolWindowLayout(LayoutType.NO_GROUPING);
        var modelDataRoot = new ModelDataRoot(getProject());

        assertThat(modelDataRoot.isInitializedAsProjectData()).isTrue();
        assertThat(modelDataRoot.getCategories()).hasSize(1);

        modelDataRoot.addCategory(Category.createOther(getProject()));
        modelDataRoot.initData();

        assertThat(modelDataRoot.getCategories()).hasSize(2);
    }

    @Test
    void initsContentRootDataIfNotYetInitialized() {
        configureToolWindowLayout(LayoutType.NO_GROUPING);
        var modelDataRoot = new ModelDataRoot(getProject());

        assertThat(modelDataRoot.isInitializedAsContentRootData()).isFalse();
        assertThat(modelDataRoot.getCategories()).hasSize(1);

        modelDataRoot.addCategory(Category.createOther(getProject()));
        modelDataRoot.initData();

        assertThat(modelDataRoot.getCategories()).hasSize(2);

        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);
        modelDataRoot.initData();

        assertThat(modelDataRoot.isInitializedAsContentRootData()).isTrue();
    }

    @Test
    void doesntInitContentRootDataIfInitialized() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);
        var modelDataRoot = new ModelDataRoot(getProject());

        assertThat(modelDataRoot.isInitializedAsContentRootData()).isTrue();
        assertThat(modelDataRoot.getContentRoots()).isEmpty();

        modelDataRoot.add(ContentRoot.createModule("module", getProject()));
        modelDataRoot.initData();

        assertThat(modelDataRoot.getContentRoots()).hasSize(1);
    }

    //updateDisplayName

    @Test
    void updatesDisplayNameForTagsOnly() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);
        var modelDataRoot = new ModelDataRoot(getProject());

        configureFileTypesInProject(null, true);
        modelDataRoot.updateDisplayName();

        assertThat(modelDataRoot.displayName).isEqualTo("Gherkin Tags");
    }

    @Test
    void updatesDisplayNameForMetasOnly() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);
        var modelDataRoot = new ModelDataRoot(getProject());

        configureFileTypesInProject(true, null);

        modelDataRoot.updateDisplayName();
        assertThat(modelDataRoot.displayName).isEqualTo("Story Metas");
    }

    @Test
    void updatesDisplayNameForTagsAndMetas() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);
        var modelDataRoot = new ModelDataRoot(getProject());

        configureFileTypesInProject(true, true);
        modelDataRoot.updateDisplayName();

        assertThat(modelDataRoot.displayName).isEqualTo("Tags and Metas");
    }

    //getContentRootsByLayout

    @Test
    void returnsModules() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);
        var module1 = ContentRoot.createModule("module1", getProject());
        var module2 = ContentRoot.createModule("module2", getProject());
        var modelDataRoot = new ModelDataRoot(getProject())
            .add(module1)
            .add(new ContentRoot("root1", ContentRoot.Type.CONTENT_ROOT, getProject()))
            .add(module2)
            .add(new ContentRoot("root2", ContentRoot.Type.CONTENT_ROOT, getProject()));

        assertThat(modelDataRoot.getContentRootsByLayout()).containsExactly(module1, module2);
    }

    @Test
    void returnsContentRoots() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);
        var root1 = new ContentRoot("root1", ContentRoot.Type.CONTENT_ROOT, getProject());
        var root2 = new ContentRoot("root2", ContentRoot.Type.CONTENT_ROOT, getProject());
        var modelDataRoot = new ModelDataRoot(getProject())
            .add(ContentRoot.createModule("module1", getProject()))
            .add(root1)
            .add(ContentRoot.createModule("module2", getProject()))
            .add(root2);

        //NOTE: the layout type will have to be changed when there is a third layout available
        configureToolWindowLayout(LayoutType.NO_GROUPING);

        assertThat(modelDataRoot.getContentRootsByLayout()).containsExactly(root1, root2);
    }

    //findContentRootOrRootless

    public void _testCreatesAndGetsRootless() {
        //TODO: How to and where to add file to not be in a project?
    }

    public void _testGetsAlreadyCreatedRootless() {
        //TODO: How to and where to add file to not be in a project?
    }

    @Test
    void createsAndGetsModuleForName() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);
        var psiFile = configureByFile("the_gherkin.feature");

        var root = new ModelDataRoot(getProject()).findContentRootOrRootless(psiFile);

        assertThat(root.displayName).isEqualTo("light_idea_test_case");
    }

    @Test
    void getsAlreadyCreatedModuleForName() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);
        var psiFile = configureByFile("the_gherkin.feature");

        var modelDataRoot = new ModelDataRoot(getProject())
            .add(ContentRoot.createModule("light_idea_test_case", getProject()));

        var root = modelDataRoot.findContentRootOrRootless(psiFile);

        assertThat(root.displayName).isEqualTo("light_idea_test_case");
    }

    @Test
    void getsLinkedModuleForDeletedVirtualFile() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);
        var psiFile = configureByFile("the_gherkin.feature");

        var modelDataRoot = new ModelDataRoot(getProject())
            .add(ContentRoot.createModule("light_idea_test_case", getProject())
                .addCategory(new Category("category", getProject())
                    .add(new Tag("tag", psiFile.getVirtualFile(), getProject()))));

        invokeInWriteActionOnEDTAndWait(psiFile::delete);

        var root = modelDataRoot.findContentRootOrRootless(psiFile);

        assertThat(root.displayName).isEqualTo("light_idea_test_case");
    }

    //getContentRoot

    @Test
    void getsModuleForName() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);

        var root = ContentRoot.createModule("light_idea_test_case", getProject());
        var modelDataRoot = new ModelDataRoot(getProject()).add(root);

        assertThat(modelDataRoot.getContentRoot("light_idea_test_case")).hasValue(root);
    }

    @Test
    void doesntGetModuleForNonExistentName() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);

        var modelDataRoot = new ModelDataRoot(getProject())
            .add(ContentRoot.createModule("light_idea_test_case", getProject()));

        assertThat(modelDataRoot.getContentRoot("non-existent")).isEmpty();
    }

    //getOther

    @Test
    void getsOtherCategory() {
        assertThat(new ModelDataRoot(getProject()).getOther())
            .isNotNull()
            .extracting(AbstractNodeType::getDisplayName).isEqualTo("Other");
    }

    //sort

    @Test
    void sortBasedOnProjectData() {
        var theGherkinFile = configureVirtualFile("the_gherkin.feature");
        var aGherkinFile = configureVirtualFile("A_gherkin.feature");

        Tag smoke = new Tag("smoke", theGherkinFile, getProject()).add(aGherkinFile);
        Tag e2e = new Tag("E2E", theGherkinFile, getProject());
        Category testSuite = new Category("Test Suite", getProject())
            .add(smoke)
            .add(e2e);

        Category component = new Category("Component", getProject())
            .addTagOrFileToTag("vimeo", theGherkinFile)
            .addTagOrFileToTag("Unsplash", aGherkinFile);

        var modelDataRoot = new ModelDataRoot(getProject());
        modelDataRoot.addCategory(testSuite).addCategory(component);
        modelDataRoot.sort();

        var categories = modelDataRoot.getCategories();
        assertSoftly(s -> {
            s.assertThat(categories).containsExactly(component, modelDataRoot.getOther(), testSuite);
            s.assertThat(categories.get(2).getTags()).containsExactly(e2e, smoke);
            s.assertThat(categories.get(2).getTags().get(1).getGherkinFiles()).containsExactly(aGherkinFile, theGherkinFile);
        });
    }

    @Test
    void sortBaseOnContentRootData() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);

        var theGherkin = configureVirtualFile("the_gherkin.feature");
        var aGherkin = configureVirtualFile("A_gherkin.feature");

        Tag smoke = new Tag("smoke", theGherkin, getProject()).add(aGherkin);
        Tag e2e = new Tag("E2E", theGherkin, getProject());
        Category testSuite = new Category("Test Suite", getProject())
            .add(smoke)
            .add(e2e);

        Category component = new Category("Component", getProject())
            .addTagOrFileToTag("vimeo", theGherkin)
            .addTagOrFileToTag("Unsplash", aGherkin);

        ContentRoot projectModule = ContentRoot.createModule("features", getProject())
            .addCategory(testSuite)
            .addCategory(component);
        ContentRoot projectModule2 = ContentRoot.createModule("stories", getProject());

        var modelDataRoot = new ModelDataRoot(getProject());
        modelDataRoot.add(projectModule).add(projectModule2);
        modelDataRoot.sort();

        assertSoftly(s -> {
            s.assertThat(modelDataRoot.getModules()).containsExactly(projectModule, projectModule2);
            s.assertThat(projectModule.getCategories()).containsExactly(component, projectModule.getOther(), testSuite);
            s.assertThat(modelDataRoot.getContentRoot("features").get().getCategories().get(2).getTags()).containsExactly(e2e, smoke);
            s.assertThat(modelDataRoot.getContentRoot("features").get().getCategories().get(2).getTags().get(1).getGherkinFiles()).containsExactly(aGherkin, theGherkin);
        });
    }

    //toString

    @Test
    void returnsToStringForProjectData() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.SIMPLIFIED;
        var theGherkin = configureVirtualFile("the_gherkin.feature");
        var aGherkin = configureVirtualFile("A_gherkin.feature");

        var modelDataRoot = new ModelDataRoot(getProject())
            .addCategory(new Category("Test Suite", getProject())
                .add(new Tag("smoke", theGherkin, getProject())
                    .add(aGherkin))
                .add(new Tag("E2E", theGherkin, getProject())))
            .addCategory(new Category("Component", getProject())
                .addTagOrFileToTag("vimeo", theGherkin)
                .addTagOrFileToTag("Unsplash", aGherkin));

        assertThat(modelDataRoot).hasToString("Gherkin Tags - 4 tags, 2 .feature files");
    }

    @Test
    void returnToStringWithSimplifiedStatistics() {
        configureToolWindowSettings(LayoutType.GROUP_BY_MODULES, StatisticsType.SIMPLIFIED);

        assertThat(setupModelData()).hasToString("Gherkin Tags - 4 tags, 2 .feature files");
    }

    @Test
    void returnToStringWithDetailedStatistics() {
        configureToolWindowSettings(LayoutType.GROUP_BY_MODULES, StatisticsType.DETAILED);

        assertThat(setupModelData()).hasToString("Gherkin Tags - 4 distinct tags in 2 .feature files");
    }

    @Test
    void returnDetailedToStringWithStoryOnlyProject() {
        configureToolWindowSettings(LayoutType.GROUP_BY_MODULES, StatisticsType.DETAILED);
        var anotherStory = configureVirtualFile("Another story.story");

        var modelDataRoot = new ModelDataRoot(getProject())
            .add(ContentRoot.createModule("features", getProject())
                .addCategory(new Category("Component", getProject())
                    .addTagOrFileToTag("vimeo", anotherStory)));

        var service = getProject().getService(ProjectBDDTypeService.class);
        service.isProjectContainGherkinFile = false;
        service.isProjectContainJBehaveStoryFile = true;
        modelDataRoot.updateDisplayName();

        assertThat(modelDataRoot).hasToString("Story Metas - 1 distinct meta in 1 .story file");
    }

    @Test
    void returnDetailedToStringWithProjectContainingBothGherkinAndStoryFiles() {
        configureToolWindowSettings(LayoutType.GROUP_BY_MODULES, StatisticsType.DETAILED);
        var anotherStory = configureVirtualFile("Another story.story");

        var modelDataRoot = setupModelData();
        modelDataRoot.getModules().getFirst().findCategory("Component").get().addTagOrFileToTag("vimeo", anotherStory);

        configureFileTypesInProject(true, true);
        modelDataRoot.updateDisplayName();

        assertThat(modelDataRoot).hasToString("Tags and Metas - 4 distinct items in 3 .feature/.story files");
    }

    @Test
    void returnToStringWithoutStatistics() {
        configureToolWindowSettings(LayoutType.GROUP_BY_MODULES, StatisticsType.DISABLED);

        assertThat(setupModelData()).hasToString("Gherkin Tags");
    }

    @Test
    void returnSimplifiedToStringWithStoryOnlyProject() {
        configureToolWindowSettings(LayoutType.GROUP_BY_MODULES, StatisticsType.SIMPLIFIED);
        var anotherStory = configureVirtualFile("Another story.story");

        var modelDataRoot = new ModelDataRoot(getProject())
            .add(ContentRoot.createModule("features", getProject())
                .addCategory(new Category("Component", getProject())
                    .addTagOrFileToTag("vimeo", anotherStory)));

        configureFileTypesInProject(true, false);
        modelDataRoot.updateDisplayName();

        assertThat(modelDataRoot).hasToString("Story Metas - 1 meta, 1 .story file");
    }

    @Test
    void returnSimplifiedToStringWithProjectContainingBothGherkinAndStoryFiles() {
        configureToolWindowSettings(LayoutType.GROUP_BY_MODULES, StatisticsType.SIMPLIFIED);
        var anotherStory = configureVirtualFile("Another story.story");

        var modelDataRoot = setupModelData();
        modelDataRoot.getModules().getFirst().findCategory("Component").get().addTagOrFileToTag("vimeo", anotherStory);

        configureFileTypesInProject(true, true);
        modelDataRoot.updateDisplayName();

        assertThat(modelDataRoot).hasToString("Tags and Metas - 4 items, 3 .feature/.story files");
    }

    private ModelDataRoot setupModelData() {
        var theGherkin = configureVirtualFile("the_gherkin.feature");
        var aGherkin = configureVirtualFile("A_gherkin.feature");

        return new ModelDataRoot(getProject())
            .add(ContentRoot.createModule("features", getProject())
                .addCategory(new Category("Test Suite", getProject())
                    .add(new Tag("smoke", theGherkin, getProject())
                        .add(aGherkin))
                    .add(new Tag("E2E", theGherkin, getProject())))
                .addCategory(new Category("Component", getProject())
                    .addTagOrFileToTag("vimeo", theGherkin)
                    .addTagOrFileToTag("Unsplash", aGherkin)));
    }

    //Helpers

    private void configureToolWindowSettings(LayoutType layoutType, StatisticsType statisticsType) {
        var settings = GherkinTagsToolWindowSettings.getInstance(getProject());
        settings.layout = layoutType;
        settings.statisticsType = statisticsType;
    }

    private void configureToolWindowLayout(LayoutType layoutType) {
        var settings = GherkinTagsToolWindowSettings.getInstance(getProject());
        settings.layout = layoutType;
    }

    private void configureFileTypesInProject(Boolean isProjectContainJBehaveStoryFile, Boolean isProjectContainGherkinFile) {
        var service = getProject().getService(ProjectBDDTypeService.class);
        if (isProjectContainGherkinFile != null)
            service.isProjectContainGherkinFile = isProjectContainGherkinFile;
        if (isProjectContainJBehaveStoryFile != null)
            service.isProjectContainJBehaveStoryFile = isProjectContainJBehaveStoryFile;
    }
}
