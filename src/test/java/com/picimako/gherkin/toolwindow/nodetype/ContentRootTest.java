//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

import static com.picimako.gherkin.SoftAsserts.assertSoftly;
import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;
import com.picimako.gherkin.toolwindow.StatisticsType;

/**
 * Unit test for {@link ContentRoot}.
 */
public class ContentRootTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "testdata/features";
    }

    // findCategory

    public void testReturnNoCategoryForNullCategoryName() {
        ContentRoot module = ContentRoot.createModule("module", getProject());

        assertThat(module.findCategory(null)).isEmpty();
    }

    public void testReturnNoCategoryForNonExistentCategoryName() {
        ContentRoot module = ContentRoot.createModule("module", getProject())
            .addCategory(new Category("aCategory", getProject()));

        assertThat(module.findCategory("nonexistent")).isEmpty();
    }

    public void testReturnCategory() {
        ContentRoot module = ContentRoot.createModule("module", getProject())
            .addCategory(new Category("Test Suite", getProject()))
            .addCategory(new Category("Component", getProject()));

        Category category = module.findCategory("Component").get();

        assertThat(category).isNotNull();
        assertThat(category.getDisplayName()).isEqualTo("Component");
    }

    // findCategoryOrOther

    public void testReturnOtherCategoryForNullCategoryName() {
        ContentRoot module = ContentRoot.createModule("module", getProject());

        assertThat(module.findCategoryOrOther(null)).isNotNull();
        assertThat(module.findCategoryOrOther("non-existent").getDisplayName()).isEqualTo("Other");
    }

    public void testReturnOtherCategoryForNonExistentCategoryName() {
        ContentRoot module = ContentRoot.createModule("module", getProject())
            .addCategory(new Category("aCategory", getProject()));

        assertThat(module.findCategoryOrOther(null)).isNotNull();
        assertThat(module.findCategoryOrOther("non-existent").getDisplayName()).isEqualTo("Other");
    }

    public void testReturnCategoryForName() {
        ContentRoot module = ContentRoot.createModule("module", getProject())
            .addCategory(new Category("Test Suite", getProject()))
            .addCategory(new Category("Component", getProject()));

        Category category = module.findCategoryOrOther("Component");

        assertThat(category).isNotNull();
        assertThat(category.getDisplayName()).isEqualTo("Component");
    }

    //findTag

    public void testReturnsTag() {
        VirtualFile theGherkin = myFixture.configureByFile("the_gherkin.feature").getVirtualFile();

        Category category = new Category("Test Suite", getProject())
            .add(new Tag("regression", theGherkin, getProject()))
            .add(new Tag("e2e", theGherkin, getProject()));

        ContentRoot module = ContentRoot.createModule("module", getProject()).addCategory(category);

        assertThat(module.findTag("e2e")).hasValueSatisfying(tag -> assertThat(tag.displayName).isEqualTo("e2e"));
    }

    public void testReturnsNoTag() {
        VirtualFile theGherkin = myFixture.configureByFile("the_gherkin.feature").getVirtualFile();

        Category category = new Category("Test Suite", getProject())
            .add(new Tag("regression", theGherkin, getProject()))
            .add(new Tag("e2e", theGherkin, getProject()));

        ContentRoot module = ContentRoot.createModule("module", getProject()).addCategory(category);

        assertThat(module.findTag("smoke")).isEmpty();
    }

    //hasFileMapped

    public void testHasFileMapped() {
        VirtualFile theGherkin = myFixture.configureByFile("the_gherkin.feature").getVirtualFile();

        Category category = new Category("Test Suite", getProject())
            .add(new Tag("regression", theGherkin, getProject()))
            .add(new Tag("e2e", theGherkin, getProject()));

        ContentRoot module = ContentRoot.createModule("module", getProject()).addCategory(category);

        assertThat(module.hasFileMapped(theGherkin)).isTrue();
    }

    public void testDoesntHaveFileMapped() {
        VirtualFile theGherkin = myFixture.configureByFile("the_gherkin.feature").getVirtualFile();
        VirtualFile aGherkin = myFixture.configureByFile("A_gherkin.feature").getVirtualFile();

        Category category = new Category("Test Suite", getProject())
            .add(new Tag("regression", theGherkin, getProject()))
            .add(new Tag("e2e", theGherkin, getProject()));

        ContentRoot module = ContentRoot.createModule("module", getProject()).addCategory(category);

        assertThat(module.hasFileMapped(aGherkin)).isFalse();
    }

    //sort

    public void testSortTagsAndCategories() {
        ContentRoot module = setupModule();

        assertSoftly(
            softly -> softly.assertThat(module.getCategories()).extracting(Category::getDisplayName).containsExactly("Other", "Test Suite", "Component"),
            softly -> softly.assertThat(module.findCategory("Test Suite").get().getTags()).extracting(Tag::getDisplayName).containsExactly("smoke", "E2E")
        );

        module.sort();

        assertSoftly(
            softly -> softly.assertThat(module.getCategories()).extracting(Category::getDisplayName).containsExactly("Component", "Other", "Test Suite"),
            softly -> softly.assertThat(module.findCategory("Test Suite").get().getTags()).extracting(Tag::getDisplayName).containsExactly("E2E", "smoke")
        );
    }

    // toString

    public void testReturnToStringWithSimplifiedStatistics() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.SIMPLIFIED;

        ContentRoot module = setupModule();

        assertThat(module).hasToString("module - 4 tags, 2 .feature files");
    }

    public void testReturnToStringWithDetailedStatistics() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.DETAILED;

        ContentRoot module = setupModule();

        assertThat(module).hasToString("module - 4 distinct tags in 2 .feature files");
    }

    public void testReturnToStringWithoutStatistics() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.DISABLED;

        ContentRoot module = setupModule();

        assertThat(module).hasToString("module");
    }

    private ContentRoot setupModule() {
        ContentRoot module = ContentRoot.createModule("module", getProject());
        VirtualFile theGherkin = myFixture.configureByFile("the_gherkin.feature").getVirtualFile();
        VirtualFile aGherkin = myFixture.configureByFile("A_gherkin.feature").getVirtualFile();

        Category testSuite = new Category("Test Suite", getProject())
            .add(new Tag("smoke", theGherkin, getProject()).add(aGherkin))
            .add(new Tag("E2E", theGherkin, getProject()));

        Category component = new Category("Component", getProject())
            .addTagOrFileToTag("vimeo", theGherkin)
            .addTagOrFileToTag("Unsplash", aGherkin);

        module.addCategory(testSuite).addCategory(component);

        return module;
    }
}
