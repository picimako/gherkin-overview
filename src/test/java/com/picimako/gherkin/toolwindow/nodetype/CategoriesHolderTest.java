//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import com.picimako.gherkin.toolwindow.TagOccurrencesRegistry;

/**
 * Unit test for {@link CategoriesHolder}.
 */
public class CategoriesHolderTest extends BasePlatformTestCase {

    private DummyCategoriesHolder holder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        holder = new DummyCategoriesHolder(getProject());
    }

    //findCategory

    public void testFindCategory() {
        assertThat(holder.findCategory("Browser")).hasValue(holder.categories.get(2));
    }

    public void testNotFindCategory() {
        assertThat(holder.findCategory("Media")).isEmpty();
    }

    //findCategoryOrOther

    public void testFindCategoryWithoutFallback() {
        assertThat(holder.findCategoryOrOther("Browser")).isEqualTo(holder.categories.get(2));
    }

    public void testFindOtherAsFallback() {
        assertThat(holder.findCategoryOrOther("Media")).isEqualTo(holder.other);
    }

    //addCategory

    public void testAddCategory() {
        DummyCategoriesHolder holder = new DummyCategoriesHolder(getProject());
        Category category = new Category("Addition", getProject());

        holder.addCategory(category);

        assertThat(holder.getCategories().get(3)).isEqualTo(category);
    }

    //findTag

    public void testFindTag() {
        DummyCategoriesHolder holder = new DummyCategoriesHolder(getProject());

        assertThat(holder.findTag("tag2")).hasValue(holder.categories.get(2).getTags().getFirst());
    }

    public void testNotFindTag() {
        DummyCategoriesHolder holder = new DummyCategoriesHolder(getProject());

        assertThat(holder.findTag("nonexistent")).isEmpty();
    }

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

        @Override
        public @NotNull Category getOther() {
            return other;
        }

        @Override
        public List<Category> getCategories() {
            return categories;
        }
    }
}
