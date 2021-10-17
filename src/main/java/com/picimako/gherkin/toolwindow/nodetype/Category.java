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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.SmartList;
import com.picimako.gherkin.resources.GherkinBundle;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a category of Gherkin tags in the tool window.
 * <p>
 * Such categories may be {@code Test Suite} for @regression, @smoke and @e2e tags, {@code Media}
 * for @youtube and @image tags, and any custom one defined by users.
 * <p>
 * Such categorization makes it easier to find tags and to better overview the types of tags defined.
 * <p>
 * In case of grouped layouts in the tool window, Category objects with the same name but with different/overlapping tags
 * may be assigned to different modules, content roots, etc.
 */
public class Category extends AbstractNodeType {

    static final String OTHER_CATEGORY_NAME = "Other";
    private final List<Tag> tags = new SmartList<>();

    public Category(@NotNull String displayName, Project project) {
        super(displayName, project);
    }

    public List<Tag> getTags() {
        return tags;
    }

    /**
     * Gets the tag with the provided name.
     *
     * @param tagName the tag name
     * @return the Tag as Optional, or empty Optional if none found
     */
    public Optional<Tag> get(@NotNull String tagName) {
        return tags.stream().filter(tag -> tag.hasName(tagName)).findFirst();
    }

    /**
     * Adds the argument tag to this node.
     *
     * @param tag the tag
     */
    public Category add(Tag tag) {
        tags.add(tag);
        return this;
    }

    /**
     * Returns whether there is any tag added to this category.
     */
    public boolean hasTag() {
        return !tags.isEmpty();
    }

    /**
     * Adds the argument file to the Tag with the provided name, if a tag with that name already exists, otherwise
     * it creates a new tag with that name, and adds the file to that tag.
     * <p>
     * The tag name passed in is expected to not contain the leading @ character.
     *
     * @param tagNameWithoutAt the tag name to add the file to
     * @param file             the file to add
     */
    public Category addTagOrFileToTag(@NotNull String tagNameWithoutAt, @NotNull VirtualFile file) {
        get(tagNameWithoutAt).ifPresentOrElse(tag -> tag.add(file), () -> tags.add(new Tag(tagNameWithoutAt, file, project)));
        return this;
    }

    /**
     * Gets whether this category is the catch-all "Other" one.
     */
    public boolean isOther() {
        return OTHER_CATEGORY_NAME.equals(displayName);
    }

    public boolean isNotOtherAndDoesntHaveAnyTag() {
        return !isOther() && tags.isEmpty();
    }

    @Override
    public void sort() {
        tags.forEach(Tag::sort);
        sortIfContainsMultiple(tags);
    }

    @Override
    public String toString() {
        return getToString(
            () -> displayName + " (" + tags.size() + ")",
            () -> GherkinBundle.toolWindow("statistics.category.detailed", displayName, tagsOccurrenceCount(), tags.size()));
    }

    private int tagsOccurrenceCount() {
        return tags.stream().mapToInt(Tag::occurrenceCount).sum();
    }

    public static Category createOther(Project project) {
        return new Category(OTHER_CATEGORY_NAME, project);
    }

    @Override
    public void dispose() {
        tags.forEach(Tag::dispose);
        tags.clear();
    }
}
