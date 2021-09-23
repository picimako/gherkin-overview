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

package com.picimako.gherkin.toolwindow.nodetype;

import static com.picimako.gherkin.SoftAsserts.assertSoftly;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.picimako.gherkin.MediumBasePlatformTestCase;
import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;
import com.picimako.gherkin.toolwindow.LayoutType;
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

    @Override
    protected String getTestDataPath() {
        return "testdata/features";
    }

    //initData

    public void testInitsProjectDataIfNotYetInitialized() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        ModelDataRoot modelDataRoot = new ModelDataRoot(getProject());

        assertThat(modelDataRoot.isProjectDataInitialized()).isFalse();

        modelDataRoot.add(ContentRoot.createModule("module", getProject()));
        modelDataRoot.initData();

        assertThat(modelDataRoot.getContentRoots()).isNotEmpty();

        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.NO_GROUPING;
        modelDataRoot.initData();

        assertThat(modelDataRoot.isProjectDataInitialized()).isTrue();
    }

    public void testDoesntInitProjectDataIfInitialized() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.NO_GROUPING;
        ModelDataRoot modelDataRoot = new ModelDataRoot(getProject());

        assertThat(modelDataRoot.isProjectDataInitialized()).isTrue();
        assertThat(modelDataRoot.getCategories()).hasSize(1);

        modelDataRoot.addCategory(Category.createOther(getProject()));
        modelDataRoot.initData();

        assertThat(modelDataRoot.getCategories()).hasSize(2);
    }

    public void testInitsContentRootDataIfNotYetInitialized() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.NO_GROUPING;
        ModelDataRoot modelDataRoot = new ModelDataRoot(getProject());

        assertThat(modelDataRoot.isContentRootDataInitialized()).isFalse();
        assertThat(modelDataRoot.getCategories()).hasSize(1);

        modelDataRoot.addCategory(Category.createOther(getProject()));
        modelDataRoot.initData();

        assertThat(modelDataRoot.getCategories()).hasSize(2);

        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        modelDataRoot.initData();

        assertThat(modelDataRoot.isContentRootDataInitialized()).isTrue();
    }

    public void testDoesntInitContentRootDataIfInitialized() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        ModelDataRoot modelDataRoot = new ModelDataRoot(getProject());

        assertThat(modelDataRoot.isContentRootDataInitialized()).isTrue();
        assertThat(modelDataRoot.getContentRoots()).isEmpty();

        modelDataRoot.add(ContentRoot.createModule("module", getProject()));
        modelDataRoot.initData();

        assertThat(modelDataRoot.getContentRoots()).hasSize(1);
    }

    //getContentRootsByLayout

    public void testReturnsModules() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        ContentRoot module1 = ContentRoot.createModule("module1", getProject());
        ContentRoot module2 = ContentRoot.createModule("module2", getProject());
        ModelDataRoot modelDataRoot = new ModelDataRoot(getProject())
            .add(module1)
            .add(new ContentRoot("root1", ContentRoot.Type.CONTENT_ROOT, getProject()))
            .add(module2)
            .add(new ContentRoot("root2", ContentRoot.Type.CONTENT_ROOT, getProject()));

        assertThat(modelDataRoot.getContentRootsByLayout()).containsExactly(module1, module2);
    }

    public void testReturnsContentRoots() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        ContentRoot root1 = new ContentRoot("root1", ContentRoot.Type.CONTENT_ROOT, getProject());
        ContentRoot root2 = new ContentRoot("root2", ContentRoot.Type.CONTENT_ROOT, getProject());
        ModelDataRoot modelDataRoot = new ModelDataRoot(getProject())
            .add(ContentRoot.createModule("module1", getProject()))
            .add(root1)
            .add(ContentRoot.createModule("module2", getProject()))
            .add(root2);

        //TODO: the layout type will have to be changed when there is a third layout available
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.NO_GROUPING;

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
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        PsiFile psiFile = myFixture.configureByFile("the_gherkin.feature");

        ContentRoot root = new ModelDataRoot(getProject()).findContentRootOrRootless(psiFile);

        assertThat(root.displayName).isEqualTo("light_idea_test_case");
    }

    public void testGetsAlreadyCreatedModuleForName() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        PsiFile psiFile = myFixture.configureByFile("the_gherkin.feature");

        ModelDataRoot modelDataRoot = new ModelDataRoot(getProject())
            .add(ContentRoot.createModule("light_idea_test_case", getProject()));

        ContentRoot root = modelDataRoot.findContentRootOrRootless(psiFile);

        assertThat(root.displayName).isEqualTo("light_idea_test_case");
    }

    public void testGetsLinkedModuleForDeletedVirtualFile() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        PsiFile psiFile = myFixture.configureByFile("the_gherkin.feature");

        ModelDataRoot modelDataRoot = new ModelDataRoot(getProject())
            .add(ContentRoot.createModule("light_idea_test_case", getProject())
                .addCategory(new Category("category", getProject())
                    .add(new Tag("tag", psiFile.getVirtualFile(), getProject()))));

        WriteAction.run(() -> psiFile.delete());

        ContentRoot root = modelDataRoot.findContentRootOrRootless(psiFile);

        assertThat(root.displayName).isEqualTo("light_idea_test_case");
    }

    //getContentRoot

    public void testGetsModuleForName() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;

        ModelDataRoot modelDataRoot = new ModelDataRoot(getProject());
        ContentRoot root = ContentRoot.createModule("light_idea_test_case", getProject());
        modelDataRoot.add(root);

        assertThat(modelDataRoot.getContentRoot("light_idea_test_case")).hasValue(root);
    }

    public void testDoesntGetModuleForNonExistentName() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;

        ModelDataRoot modelDataRoot = new ModelDataRoot(getProject())
            .add(ContentRoot.createModule("light_idea_test_case", getProject()));

        assertThat(modelDataRoot.getContentRoot("non-existent")).isEmpty();
    }

    //getOther

    public void testGetsOtherCategory() {
        ModelDataRoot modelDataRoot = new ModelDataRoot(getProject());

        assertThat(modelDataRoot.getOther()).isNotNull();
        assertThat(modelDataRoot.getOther().getDisplayName()).isEqualTo("Other");
    }

    //sort

    public void testSortBasedOnProjectData() {
        ModelDataRoot modelDataRoot = new ModelDataRoot(getProject());

        VirtualFile theGherkin = myFixture.configureByFile("the_gherkin.feature").getVirtualFile();
        VirtualFile aGherkin = myFixture.configureByFile("A_gherkin.feature").getVirtualFile();

        Tag smoke = new Tag("smoke", theGherkin, getProject()).add(aGherkin);
        Tag e2e = new Tag("E2E", theGherkin, getProject());
        Category testSuite = new Category("Test Suite", getProject())
            .add(smoke)
            .add(e2e);

        Category component = new Category("Component", getProject())
            .addTagOrFileToTag("vimeo", theGherkin)
            .addTagOrFileToTag("Unsplash", aGherkin);

        modelDataRoot.addCategory(testSuite).addCategory(component);

        modelDataRoot.sort();

        List<Category> categories = modelDataRoot.getCategories();
        assertSoftly(
            softly -> softly.assertThat(categories).containsExactly(component, modelDataRoot.getOther(), testSuite),
            softly -> softly.assertThat(categories.get(2).getTags()).containsExactly(e2e, smoke),
            softly -> softly.assertThat(categories.get(2).getTags().get(1).getGherkinFiles()).containsExactly(aGherkin, theGherkin)
        );
    }

    public void testSortBaseOnContentRootData() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        ModelDataRoot modelDataRoot = new ModelDataRoot(getProject());

        VirtualFile theGherkin = myFixture.configureByFile("the_gherkin.feature").getVirtualFile();
        VirtualFile aGherkin = myFixture.configureByFile("A_gherkin.feature").getVirtualFile();

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
        modelDataRoot.add(projectModule).add(projectModule2);

        modelDataRoot.sort();

        assertSoftly(
            softly -> softly.assertThat(modelDataRoot.getModules()).containsExactly(projectModule, projectModule2),
            softly -> softly.assertThat(projectModule.getCategories()).containsExactly(component, projectModule.getOther(), testSuite),
            softly -> softly.assertThat(modelDataRoot.getContentRoot("features").get().getCategories().get(2).getTags()).containsExactly(e2e, smoke),
            softly -> softly.assertThat(modelDataRoot.getContentRoot("features").get().getCategories().get(2).getTags().get(1).getGherkinFiles()).containsExactly(aGherkin, theGherkin)
        );
    }

    //toString

    public void testReturnsToStringForProjectData() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.SIMPLIFIED;
        VirtualFile theGherkin = myFixture.configureByFile("the_gherkin.feature").getVirtualFile();
        VirtualFile aGherkin = myFixture.configureByFile("A_gherkin.feature").getVirtualFile();

        ModelDataRoot modelDataRoot = new ModelDataRoot(getProject())
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
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.SIMPLIFIED;

        ModelDataRoot modelDataRoot = setupModelData();

        assertThat(modelDataRoot).hasToString("Gherkin Tags - 4 tags, 2 .feature files");
    }

    public void testReturnToStringWithDetailedStatistics() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.DETAILED;

        ModelDataRoot modelDataRoot = setupModelData();

        assertThat(modelDataRoot).hasToString("Gherkin Tags - 4 distinct tags in 2 .feature files");
    }

    public void testReturnToStringWithoutStatistics() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.DISABLED;

        ModelDataRoot modelDataRoot = setupModelData();

        assertThat(modelDataRoot).hasToString("Gherkin Tags");
    }

    private ModelDataRoot setupModelData() {
        VirtualFile theGherkin = myFixture.configureByFile("the_gherkin.feature").getVirtualFile();
        VirtualFile aGherkin = myFixture.configureByFile("A_gherkin.feature").getVirtualFile();

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
}
