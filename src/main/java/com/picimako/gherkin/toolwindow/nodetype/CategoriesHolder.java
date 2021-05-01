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

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Marks tree node types that they store and manage {@link Category} objects and information.
 *
 * @see ModelDataRoot
 * @see ContentRoot
 */
public interface CategoriesHolder {

    /**
     * Gets the category with the provided name or empty Optional if none found.
     *
     * @param name the category name to find
     * @return the category, or empty Optional
     */
    default Optional<Category> findCategory(@Nullable String name) {
        return getCategories().stream().filter(category -> category.hasName(name)).findFirst();
    }

    /**
     * Gets the dedicated catch-all category called "Other".
     */
    Category getOther();

    /**
     * Gets the category with the provided name, or the catch-all Other category if none is found with that name.
     *
     * @param name the category name to find
     * @return the found category, or Other
     */
    @NotNull
    default Category findCategoryOrOther(@Nullable String name) {
        return findCategory(name).orElse(getOther());
    }

    /**
     * Gets the list of categories stored by this node.
     */
    List<Category> getCategories();

    /**
     * Adds the argument category to this node.
     * <p>
     * It doesn't do any check whether the category is already added to it. Ideally it shouldn't happen that one
     * Category object is added more than once.
     *
     * @param category the category to add
     */
    default <T extends CategoriesHolder> T addCategory(Category category) {
        getCategories().add(category);
        return (T) this;
    }

    /**
     * Queries the tag with the provided name in the underlying model data.
     * <p>
     * It returns the Tag as an Optional, or an empty Optional if no tag is found with the given name.
     *
     * @param tagName the tag's name to search for
     */
    default Optional<Tag> findTag(String tagName) {
        return getCategories().stream().flatMap(category -> category.getTags().stream()).filter(tag -> tag.hasName(tagName)).findFirst();
    }
}
