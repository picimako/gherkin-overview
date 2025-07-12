//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

import java.util.List;
import java.util.Optional;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.SmartList;
import com.picimako.gherkin.resources.GherkinBundle;
import lombok.Getter;
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
public final class Category extends AbstractNodeType {

    static final String OTHER_CATEGORY_NAME = "Other";
    @Getter
    private final List<Tag> tags = new SmartList<>();

    public Category(@NotNull String displayName, Project project) {
        super(displayName, project);
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
            () -> GherkinBundle.message("gherkin.overview.toolwindow.statistics.category.detailed", displayName, tagsOccurrenceCount(), tags.size()));
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
