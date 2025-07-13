//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.util.stream.Stream;

import com.intellij.openapi.vfs.VirtualFile;
import com.picimako.gherkin.GherkinOverviewTestBase;
import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;
import com.picimako.gherkin.toolwindow.StatisticsType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit test for {@link Category}.
 */
final class CategoryTest extends GherkinOverviewTestBase {

    //get

    @Test
    void returnOptionalWithTagForName() {
        assertThat(createCategory().get("regression"))
            .hasValueSatisfying(tag -> assertThat(tag.getDisplayName()).isEqualTo("regression"));
    }

    @Test
    void returnEmptyOptionalForNotFoundTagName() {
        VirtualFile theGherkin = configureVirtualFile("the_gherkin.feature");

        Category category = new Category("Test Suite", getProject())
            .add(new Tag("smoke", theGherkin, getProject()));

        assertThat(category.get("e2e")).isEmpty();
    }

    //hasTag

    @Test
    void hasTag() {
        assertThat(createCategory().hasTag()).isTrue();
    }

    @Test
    void doesntHaveTag() {
        assertThat(new Category("test suite", getProject()).hasTag()).isFalse();
    }

    //addTagOrFileToTag

    @Test
    void addFeatureFileToTagIfTagExists() {
        VirtualFile theGherkin = configureVirtualFile("the_gherkin.feature");
        VirtualFile aGherkin = configureVirtualFile("A_gherkin.feature");

        Tag tag;
        Category category = new Category("Test Suite", getProject())
            .add(tag = new Tag("smoke", theGherkin, getProject()));

        category.addTagOrFileToTag("smoke", aGherkin);

        assertThat(tag.getGherkinFiles()).containsExactly(theGherkin, aGherkin);
    }

    @Test
    void createTagInitializedWithFeatureFileIfTagDoesntExist() {
        VirtualFile theGherkin = configureVirtualFile("the_gherkin.feature");
        VirtualFile aGherkin = configureVirtualFile("A_gherkin.feature");

        Category category = new Category("Test Suite", getProject())
            .add(new Tag("smoke", theGherkin, getProject()));

        category.addTagOrFileToTag("regression", aGherkin);

        assertSoftly(s -> {
            s.assertThat(category.getTags()).hasSize(2);
            s.assertThat(category.getTags().get(1).getGherkinFiles()).containsOnly(aGherkin);
        });
    }

    //isOther

    @Test
    void beOtherCategory() {
        assertThat(Category.createOther(getProject()).isOther()).isTrue();
    }

    @Test
    void notBeOtherCategory() {
        assertThat(new Category("Not other", getProject()).isOther()).isFalse();
    }

    //isNotOtherAndDoesntHaveAnyTag

    @Test
    void other() {
        assertThat(Category.createOther(getProject()).isNotOtherAndDoesntHaveAnyTag()).isFalse();
    }

    @Test
    void notOtherWithNotEmptyTags() {
        VirtualFile aGherkin = configureVirtualFile("A_gherkin.feature");

        Category notOther = new Category("Not other", getProject())
            .add(new Tag("tag", aGherkin, getProject()));

        assertThat(notOther.isNotOtherAndDoesntHaveAnyTag()).isFalse();
    }

    @Test
    void notOtherWithEmptyTags() {
        assertThat(new Category("Not other", getProject()).isNotOtherAndDoesntHaveAnyTag()).isTrue();
    }

    //sort

    @Test
    void sortMoreThanOneTags() {
        Category category = createCategory();

        assertThat(category.getTags()).extracting(AbstractNodeType::getDisplayName).containsExactly("smoke", "regression");

        category.sort();

        assertThat(category.getTags()).extracting(AbstractNodeType::getDisplayName).containsExactly("regression", "smoke");
    }

    //toString

    @ParameterizedTest
    @MethodSource("toStrings")
    void testToString(StatisticsType statisticsType, String toString) {
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = statisticsType;

        assertThat(createCategory()).hasToString(toString);
    }

    private static Stream<Arguments> toStrings() {
        return Stream.of(
            argumentSet("returns disabled toString()", StatisticsType.DISABLED, "Test Suite"),
            argumentSet("builds simplified toString()", StatisticsType.SIMPLIFIED, "Test Suite (2)"),
            argumentSet("builds detailed toString()", StatisticsType.DETAILED, "Test Suite - 2 for 2 distinct tags")
        );
    }

    private Category createCategory() {
        VirtualFile theGherkin = configureVirtualFile("the_gherkin.feature");
        VirtualFile aGherkin = configureVirtualFile("A_gherkin.feature");

        return new Category("Test Suite", getProject())
            .add(new Tag("smoke", aGherkin, getProject()))
            .add(new Tag("regression", theGherkin, getProject()));
    }
}
