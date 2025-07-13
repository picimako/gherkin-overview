//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightVirtualFile;
import com.picimako.gherkin.GherkinOverviewTestBase;
import com.picimako.gherkin.toolwindow.TagOccurrencesRegistry;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link CategoriesHolder}.
 */
final class CategoriesHolderTest extends GherkinOverviewTestBase {

    private DummyCategoriesHolder holder;

    @BeforeEach
    void setUp() {
        holder = new DummyCategoriesHolder(getProject());
    }

    //findCategory

    @Test
    void findCategory() {
        assertThat(holder.findCategory("Browser")).hasValue(holder.categories.get(2));
    }

    @Test
    void notFindCategory() {
        assertThat(holder.findCategory("Media")).isEmpty();
    }

    //findCategoryOrOther

    @Test
    void findCategoryWithoutFallback() {
        assertThat(holder.findCategoryOrOther("Browser")).isEqualTo(holder.categories.get(2));
    }

    @Test
    void findOtherAsFallback() {
        assertThat(holder.findCategoryOrOther("Media")).isEqualTo(holder.other);
    }

    //addCategory

    @Test
    void addCategory() {
        var categoriesHolder = new DummyCategoriesHolder(getProject());
        var category = new Category("Addition", getProject());

        categoriesHolder.addCategory(category);

        assertThat(categoriesHolder.getCategories().get(3)).isEqualTo(category);
    }

    //findTag

    @Test
    void findTag() {
        var categoriesHolder = new DummyCategoriesHolder(getProject());

        assertThat(categoriesHolder.findTag("tag2")).hasValue(categoriesHolder.categories.get(2).getTags().getFirst());
    }

    @Test
    void notFindTag() {
        var categoriesHolder = new DummyCategoriesHolder(getProject());

        assertThat(categoriesHolder.findTag("nonexistent")).isEmpty();
    }

    @Getter
    private static final class DummyCategoriesHolder implements CategoriesHolder {
        final Category other;
        final List<Category> categories = new ArrayList<>();

        public DummyCategoriesHolder(Project project) {
            TagOccurrencesRegistry.getInstance(project).init(2);
            other = Category.createOther(project);
            categories.add(other);
            categories.add(new Category("Test Suite", project)
                .add(new Tag("tag", new LightVirtualFile(), project)));
            categories.add(new Category("Browser", project)
                .add(new Tag("tag2", new LightVirtualFile(), project)));
        }
    }
}
