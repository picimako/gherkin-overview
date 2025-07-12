//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import com.picimako.gherkin.toolwindow.StatisticsType;
import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;

/**
 * Unit test for {@link Category}.
 */
public class CategoryTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "testdata/features";
    }

    //get

    public void testReturnOptionalWithTagForName() {
        assertThat(createCategory().get("regression"))
            .hasValueSatisfying(tag -> assertThat(tag.getDisplayName()).isEqualTo("regression"));
    }

    public void testReturnEmptyOptionalForNotFoundTagName() {
        VirtualFile theGherkin = myFixture.configureByFile("the_gherkin.feature").getVirtualFile();

        Category category = new Category("Test Suite", getProject())
            .add(new Tag("smoke", theGherkin, getProject()));

        assertThat(category.get("e2e")).isEmpty();
    }

    //hasTag

    public void testHasTag() {
        assertThat(createCategory().hasTag()).isTrue();
    }

    public void testDoesntHaveTag() {
        assertThat(new Category("test suite", getProject()).hasTag()).isFalse();
    }

    //addTagOrFileToTag

    public void testAddFeatureFileToTagIfTagExists() {
        VirtualFile theGherkin = myFixture.configureByFile("the_gherkin.feature").getVirtualFile();
        VirtualFile aGherkin = myFixture.configureByFile("A_gherkin.feature").getVirtualFile();

        Tag tag;
        Category category = new Category("Test Suite", getProject())
            .add(tag = new Tag("smoke", theGherkin, getProject()));

        category.addTagOrFileToTag("smoke", aGherkin);

        assertThat(tag.getGherkinFiles()).containsExactly(theGherkin, aGherkin);
    }

    public void testCreateTagInitializedWithFeatureFileIfTagDoesntExist() {
        VirtualFile theGherkin = myFixture.configureByFile("the_gherkin.feature").getVirtualFile();
        VirtualFile aGherkin = myFixture.configureByFile("A_gherkin.feature").getVirtualFile();

        Category category = new Category("Test Suite", getProject())
            .add(new Tag("smoke", theGherkin, getProject()));

        category.addTagOrFileToTag("regression", aGherkin);

        assertSoftly(s -> {
            s.assertThat(category.getTags()).hasSize(2);
            s.assertThat(category.getTags().get(1).getGherkinFiles()).containsOnly(aGherkin);
        });
    }

    //isOther

    public void testBeOtherCategory() {
        assertThat(Category.createOther(getProject()).isOther()).isTrue();
    }

    public void testNotBeOtherCategory() {
        assertThat(new Category("Not other", getProject()).isOther()).isFalse();
    }

    //isNotOtherAndDoesntHaveAnyTag

    public void testOther() {
        assertThat(Category.createOther(getProject()).isNotOtherAndDoesntHaveAnyTag()).isFalse();
    }

    public void testNotOtherWithNotEmptyTags() {
        VirtualFile aGherkin = myFixture.configureByFile("A_gherkin.feature").getVirtualFile();

        Category notOther = new Category("Not other", getProject())
            .add(new Tag("tag", aGherkin, getProject()));

        assertThat(notOther.isNotOtherAndDoesntHaveAnyTag()).isFalse();
    }

    public void testNotOtherWithEmptyTags() {
        assertThat(new Category("Not other", getProject()).isNotOtherAndDoesntHaveAnyTag()).isTrue();
    }

    //sort

    public void testSortMoreThanOneTags() {
        Category category = createCategory();

        assertThat(category.getTags()).extracting(AbstractNodeType::getDisplayName).containsExactly("smoke", "regression");

        category.sort();

        assertThat(category.getTags()).extracting(AbstractNodeType::getDisplayName).containsExactly("regression", "smoke");
    }

    //toString

    public void testShowSimplifiedStatistics() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.SIMPLIFIED;

        assertThat(createCategory()).hasToString("Test Suite (2)");
    }

    public void testShowDetailedStatistics() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.DETAILED;

        assertThat(createCategory()).hasToString("Test Suite - 2 for 2 distinct tags");
    }

    public void testNotShowStatistics() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.DISABLED;

        assertThat(createCategory()).hasToString("Test Suite");
    }

    private Category createCategory() {
        VirtualFile theGherkin = myFixture.configureByFile("the_gherkin.feature").getVirtualFile();
        VirtualFile aGherkin = myFixture.configureByFile("A_gherkin.feature").getVirtualFile();

        return new Category("Test Suite", getProject())
            .add(new Tag("smoke", aGherkin, getProject()))
            .add(new Tag("regression", theGherkin, getProject()));
    }
}
