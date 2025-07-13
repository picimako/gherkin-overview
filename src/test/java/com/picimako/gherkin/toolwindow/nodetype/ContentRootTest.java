//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.intellij.openapi.vfs.VirtualFile;
import com.picimako.gherkin.GherkinOverviewTestBase;
import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;
import com.picimako.gherkin.toolwindow.StatisticsType;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ContentRoot}.
 */
final class ContentRootTest extends GherkinOverviewTestBase {

    // findCategory

    @Test
    void returnNoCategoryForNullCategoryName() {
        ContentRoot module = ContentRoot.createModule("module", getProject());

        assertThat(module.findCategory(null)).isEmpty();
    }

    @Test
    void returnNoCategoryForNonExistentCategoryName() {
        ContentRoot module = ContentRoot.createModule("module", getProject())
            .addCategory(new Category("aCategory", getProject()));

        assertThat(module.findCategory("nonexistent")).isEmpty();
    }

    @Test
    void returnCategory() {
        ContentRoot module = ContentRoot.createModule("module", getProject())
            .addCategory(new Category("Test Suite", getProject()))
            .addCategory(new Category("Component", getProject()));

        Category category = module.findCategory("Component").get();

        assertThat(category).isNotNull().extracting(Category::getDisplayName).isEqualTo("Component");
    }

    // findCategoryOrOther

    @Test
    void returnOtherCategoryForNullCategoryName() {
        ContentRoot module = ContentRoot.createModule("module", getProject());

        assertThat(module.findCategoryOrOther(null)).isNotNull();
        assertThat(module.findCategoryOrOther("non-existent").getDisplayName()).isEqualTo("Other");
    }

    @Test
    void returnOtherCategoryForNonExistentCategoryName() {
        ContentRoot module = ContentRoot.createModule("module", getProject())
            .addCategory(new Category("aCategory", getProject()));

        assertThat(module.findCategoryOrOther(null)).isNotNull();
        assertThat(module.findCategoryOrOther("non-existent").getDisplayName()).isEqualTo("Other");
    }

    @Test
    void returnCategoryForName() {
        ContentRoot module = ContentRoot.createModule("module", getProject())
            .addCategory(new Category("Test Suite", getProject()))
            .addCategory(new Category("Component", getProject()));

        Category category = module.findCategoryOrOther("Component");

        assertThat(category).isNotNull().extracting(Category::getDisplayName).isEqualTo("Component");
    }

    //findTag

    @Test
    void returnsTag() {
        VirtualFile theGherkin = configureVirtualFile("the_gherkin.feature");

        Category category = new Category("Test Suite", getProject())
            .add(new Tag("regression", theGherkin, getProject()))
            .add(new Tag("e2e", theGherkin, getProject()));

        ContentRoot module = ContentRoot.createModule("module", getProject()).addCategory(category);

        assertThat(module.findTag("e2e")).hasValueSatisfying(tag -> assertThat(tag.displayName).isEqualTo("e2e"));
    }

    @Test
    void returnsNoTag() {
        VirtualFile theGherkin = configureVirtualFile("the_gherkin.feature");

        Category category = new Category("Test Suite", getProject())
            .add(new Tag("regression", theGherkin, getProject()))
            .add(new Tag("e2e", theGherkin, getProject()));

        ContentRoot module = ContentRoot.createModule("module", getProject()).addCategory(category);

        assertThat(module.findTag("smoke")).isEmpty();
    }

    //hasFileMapped

    @Test
    void hasFileMapped() {
        VirtualFile theGherkin = configureVirtualFile("the_gherkin.feature");

        Category category = new Category("Test Suite", getProject())
            .add(new Tag("regression", theGherkin, getProject()))
            .add(new Tag("e2e", theGherkin, getProject()));

        ContentRoot module = ContentRoot.createModule("module", getProject()).addCategory(category);

        assertThat(module.hasFileMapped(theGherkin)).isTrue();
    }

    @Test
    void doesntHaveFileMapped() {
        VirtualFile theGherkin = configureVirtualFile("the_gherkin.feature");
        VirtualFile aGherkin = configureVirtualFile("A_gherkin.feature");

        Category category = new Category("Test Suite", getProject())
            .add(new Tag("regression", theGherkin, getProject()))
            .add(new Tag("e2e", theGherkin, getProject()));

        ContentRoot module = ContentRoot.createModule("module", getProject()).addCategory(category);

        assertThat(module.hasFileMapped(aGherkin)).isFalse();
    }

    //sort

    @Test
    void sortTagsAndCategories() {
        ContentRoot module = setupModule();

        assertSoftly(s -> {
            s.assertThat(module.getCategories()).extracting(Category::getDisplayName).containsExactly("Other", "Test Suite", "Component");
            s.assertThat(module.findCategory("Test Suite").get().getTags()).extracting(Tag::getDisplayName).containsExactly("smoke", "E2E");
        });

        module.sort();

        assertSoftly(s -> {
            s.assertThat(module.getCategories()).extracting(Category::getDisplayName).containsExactly("Component", "Other", "Test Suite");
            s.assertThat(module.findCategory("Test Suite").get().getTags()).extracting(Tag::getDisplayName).containsExactly("E2E", "smoke");
        });
    }

    // toString

    @Test
    void returnToStringWithSimplifiedStatistics() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.SIMPLIFIED;

        ContentRoot module = setupModule();

        assertThat(module).hasToString("module - 4 tags, 2 .feature files");
    }

    @Test
    void returnToStringWithDetailedStatistics() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.DETAILED;

        ContentRoot module = setupModule();

        assertThat(module).hasToString("module - 4 distinct tags in 2 .feature files");
    }

    @Test
    void returnToStringWithoutStatistics() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.DISABLED;

        ContentRoot module = setupModule();

        assertThat(module).hasToString("module");
    }

    private ContentRoot setupModule() {
        ContentRoot module = ContentRoot.createModule("module", getProject());
        VirtualFile theGherkin = configureVirtualFile("the_gherkin.feature");
        VirtualFile aGherkin = configureVirtualFile("A_gherkin.feature");

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
