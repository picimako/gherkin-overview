//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.intellij.openapi.application.WriteAction;
import com.picimako.gherkin.MediumBasePlatformTestCase;
import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;
import com.picimako.gherkin.toolwindow.LayoutType;
import com.picimako.gherkin.toolwindow.ProjectBDDTypeService;
import com.picimako.gherkin.toolwindow.StatisticsType;
import com.picimako.gherkin.toolwindow.TagOccurrencesRegistry;

/**
 * Unit test for {@link ModelDataRoot}.
 */
public class ModelDataRootTest extends MediumBasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TagOccurrencesRegistry.getInstance(getProject()).init(1);
    }

    //initData

    public void testInitsProjectDataIfNotYetInitialized() {
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

    public void testDoesntInitProjectDataIfInitialized() {
        configureToolWindowLayout(LayoutType.NO_GROUPING);
        var modelDataRoot = new ModelDataRoot(getProject());

        assertThat(modelDataRoot.isInitializedAsProjectData()).isTrue();
        assertThat(modelDataRoot.getCategories()).hasSize(1);

        modelDataRoot.addCategory(Category.createOther(getProject()));
        modelDataRoot.initData();

        assertThat(modelDataRoot.getCategories()).hasSize(2);
    }

    public void testInitsContentRootDataIfNotYetInitialized() {
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

    public void testDoesntInitContentRootDataIfInitialized() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);
        var modelDataRoot = new ModelDataRoot(getProject());

        assertThat(modelDataRoot.isInitializedAsContentRootData()).isTrue();
        assertThat(modelDataRoot.getContentRoots()).isEmpty();

        modelDataRoot.add(ContentRoot.createModule("module", getProject()));
        modelDataRoot.initData();

        assertThat(modelDataRoot.getContentRoots()).hasSize(1);
    }

    //updateDisplayName

    public void testUpdatesDisplayNameForTagsOnly() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);
        var modelDataRoot = new ModelDataRoot(getProject());

        configureFileTypesInProject(null, true);
        modelDataRoot.updateDisplayName();

        assertThat(modelDataRoot.displayName).isEqualTo("Gherkin Tags");
    }

    public void testUpdatesDisplayNameForMetasOnly() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);
        var modelDataRoot = new ModelDataRoot(getProject());

        configureFileTypesInProject(true, null);

        modelDataRoot.updateDisplayName();
        assertThat(modelDataRoot.displayName).isEqualTo("Story Metas");
    }

    public void testUpdatesDisplayNameForTagsAndMetas() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);
        var modelDataRoot = new ModelDataRoot(getProject());

        configureFileTypesInProject(true, true);
        modelDataRoot.updateDisplayName();

        assertThat(modelDataRoot.displayName).isEqualTo("Tags and Metas");
    }

    //getContentRootsByLayout

    public void testReturnsModules() {
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

    public void testReturnsContentRoots() {
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

    public void testCreatesAndGetsModuleForName() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);
        var psiFile = myFixture.configureByFile("the_gherkin.feature");

        var root = new ModelDataRoot(getProject()).findContentRootOrRootless(psiFile);

        assertThat(root.displayName).isEqualTo("light_idea_test_case");
    }

    public void testGetsAlreadyCreatedModuleForName() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);
        var psiFile = myFixture.configureByFile("the_gherkin.feature");

        var modelDataRoot = new ModelDataRoot(getProject())
            .add(ContentRoot.createModule("light_idea_test_case", getProject()));

        var root = modelDataRoot.findContentRootOrRootless(psiFile);

        assertThat(root.displayName).isEqualTo("light_idea_test_case");
    }

    public void testGetsLinkedModuleForDeletedVirtualFile() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);
        var psiFile = myFixture.configureByFile("the_gherkin.feature");

        var modelDataRoot = new ModelDataRoot(getProject())
            .add(ContentRoot.createModule("light_idea_test_case", getProject())
                .addCategory(new Category("category", getProject())
                    .add(new Tag("tag", psiFile.getVirtualFile(), getProject()))));

        WriteAction.run(psiFile::delete);

        var root = modelDataRoot.findContentRootOrRootless(psiFile);

        assertThat(root.displayName).isEqualTo("light_idea_test_case");
    }

    //getContentRoot

    public void testGetsModuleForName() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);

        var root = ContentRoot.createModule("light_idea_test_case", getProject());
        var modelDataRoot = new ModelDataRoot(getProject()).add(root);

        assertThat(modelDataRoot.getContentRoot("light_idea_test_case")).hasValue(root);
    }

    public void testDoesntGetModuleForNonExistentName() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);

        var modelDataRoot = new ModelDataRoot(getProject())
            .add(ContentRoot.createModule("light_idea_test_case", getProject()));

        assertThat(modelDataRoot.getContentRoot("non-existent")).isEmpty();
    }

    //getOther

    public void testGetsOtherCategory() {
        assertThat(new ModelDataRoot(getProject()).getOther())
            .isNotNull()
            .extracting(AbstractNodeType::getDisplayName).isEqualTo("Other");
    }

    //sort

    public void testSortBasedOnProjectData() {
        var theGherkinFile = myFixture.configureByFile("the_gherkin.feature").getVirtualFile();
        var aGherkinFile = myFixture.configureByFile("A_gherkin.feature").getVirtualFile();

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

    public void testSortBaseOnContentRootData() {
        configureToolWindowLayout(LayoutType.GROUP_BY_MODULES);

        var theGherkin = myFixture.configureByFile("the_gherkin.feature").getVirtualFile();
        var aGherkin = myFixture.configureByFile("A_gherkin.feature").getVirtualFile();

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

    public void testReturnsToStringForProjectData() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.SIMPLIFIED;
        var theGherkin = myFixture.configureByFile("the_gherkin.feature").getVirtualFile();
        var aGherkin = myFixture.configureByFile("A_gherkin.feature").getVirtualFile();

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

    public void testReturnToStringWithSimplifiedStatistics() {
        configureToolWindowSettings(LayoutType.GROUP_BY_MODULES, StatisticsType.SIMPLIFIED);

        assertThat(setupModelData()).hasToString("Gherkin Tags - 4 tags, 2 .feature files");
    }

    public void testReturnToStringWithDetailedStatistics() {
        configureToolWindowSettings(LayoutType.GROUP_BY_MODULES, StatisticsType.DETAILED);

        assertThat(setupModelData()).hasToString("Gherkin Tags - 4 distinct tags in 2 .feature files");
    }

    public void testReturnDetailedToStringWithStoryOnlyProject() {
        configureToolWindowSettings(LayoutType.GROUP_BY_MODULES, StatisticsType.DETAILED);
        var anotherStory = myFixture.configureByFile("Another story.story").getVirtualFile();

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

    public void testReturnDetailedToStringWithProjectContainingBothGherkinAndStoryFiles() {
        configureToolWindowSettings(LayoutType.GROUP_BY_MODULES, StatisticsType.DETAILED);
        var anotherStory = myFixture.configureByFile("Another story.story").getVirtualFile();

        var modelDataRoot = setupModelData();
        modelDataRoot.getModules().getFirst().findCategory("Component").get().addTagOrFileToTag("vimeo", anotherStory);

        configureFileTypesInProject(true, true);
        modelDataRoot.updateDisplayName();

        assertThat(modelDataRoot).hasToString("Tags and Metas - 4 distinct items in 3 .feature/.story files");
    }

    public void testReturnToStringWithoutStatistics() {
        configureToolWindowSettings(LayoutType.GROUP_BY_MODULES, StatisticsType.DISABLED);

        assertThat(setupModelData()).hasToString("Gherkin Tags");
    }

    public void testReturnSimplifiedToStringWithStoryOnlyProject() {
        configureToolWindowSettings(LayoutType.GROUP_BY_MODULES, StatisticsType.SIMPLIFIED);
        var anotherStory = myFixture.configureByFile("Another story.story").getVirtualFile();

        var modelDataRoot = new ModelDataRoot(getProject())
            .add(ContentRoot.createModule("features", getProject())
                .addCategory(new Category("Component", getProject())
                    .addTagOrFileToTag("vimeo", anotherStory)));

        configureFileTypesInProject(true, false);
        modelDataRoot.updateDisplayName();

        assertThat(modelDataRoot).hasToString("Story Metas - 1 meta, 1 .story file");
    }

    public void testReturnSimplifiedToStringWithProjectContainingBothGherkinAndStoryFiles() {
        configureToolWindowSettings(LayoutType.GROUP_BY_MODULES, StatisticsType.SIMPLIFIED);
        var anotherStory = myFixture.configureByFile("Another story.story").getVirtualFile();

        var modelDataRoot = setupModelData();
        modelDataRoot.getModules().getFirst().findCategory("Component").get().addTagOrFileToTag("vimeo", anotherStory);

        configureFileTypesInProject(true, true);
        modelDataRoot.updateDisplayName();

        assertThat(modelDataRoot).hasToString("Tags and Metas - 4 items, 3 .feature/.story files");
    }

    private ModelDataRoot setupModelData() {
        var theGherkin = myFixture.configureByFile("the_gherkin.feature").getVirtualFile();
        var aGherkin = myFixture.configureByFile("A_gherkin.feature").getVirtualFile();

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
