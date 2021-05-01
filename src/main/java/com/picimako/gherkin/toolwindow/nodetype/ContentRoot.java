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
import java.util.Map;
import javax.swing.*;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.SmartList;
import com.picimako.gherkin.resources.GherkinBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

/**
 * Represents a general content root, be it a project module, sources root, resources root, or other type of content root
 * identified by the IDE.
 */
public class ContentRoot extends AbstractNodeType implements CategoriesHolder {

    private static final Map<Type, Icon> ICONS = Map.of(
        Type.MODULE, AllIcons.Actions.ModuleDirectory,
        Type.CONTENT_ROOT, AllIcons.Modules.ResourcesRoot //TODO: modify icon based on the root is a resource, test resource or other non-source root.
    );
    private final List<Category> categories = new SmartList<>();
    private final Category other;
    private final Type type;

    /**
     * It initializes the collection of categories with one called {@code Other}, where unmapped tags will be put.
     */
    protected ContentRoot(@NotNull String displayName, Type type, @NotNull Project project) {
        super(displayName, project);
        this.type = type;
        other = Category.createOther(project);
        categories.add(other);
    }

    public Type getType() {
        return type;
    }

    public Icon getIcon() {
        return ICONS.get(type);
    }

    public boolean isModule() {
        return type == Type.MODULE;
    }

    public boolean isContentRoot() {
        return type == Type.CONTENT_ROOT;
    }

    @Override
    public List<Category> getCategories() {
        return categories;
    }

    /**
     * Gets whether this content root node has the argument file stored in any of its underlying tags.
     *
     * @param file the file to look for
     * @return true if the file is stored, false otherwise
     */
    public boolean hasFileMapped(VirtualFile file) {
        return categories.stream()
            .flatMap(category -> category.getTags().stream())
            .flatMap(tag -> tag.getFeatureFiles().stream())
            .anyMatch(featureFile -> featureFile.getFile().equals(file));
    }

    /**
     * Gets the category dedicated for unmapped tags.
     */
    @Override
    public Category getOther() {
        return other;
    }

    /**
     * Sorts each collection of categories, tags and virtual files alphabetically.
     */
    @Override
    public void sort() {
        categories.forEach(Category::sort);
        sortIfContainsMultiple(categories);
    }

    /**
     * This doesn't show the number of all Gherkin files in the project, only the number of those containing tags.
     */
    @Override
    public String toString() {
        return getToString(
            () -> GherkinBundle.toolWindow("statistics.module.simplified", displayName, tagCount(), gherkinFileCount()),
            () -> GherkinBundle.toolWindow("statistics.module.detailed", displayName, tagCount(), gherkinFileCount()));
    }

    /**
     * Counts the number of tags stored in this model.
     */
    public int tagCount() {
        return categories.stream().mapToInt(category -> category.getTags().size()).sum();
    }

    /**
     * Counts the distinct number of Gherkin files stored in this model.
     */
    public long gherkinFileCount() {
        return categories.stream()
            .flatMap(category -> category.getTags().stream())
            .flatMap(tag -> tag.getFeatureFiles().stream())
            .distinct()
            .count();
    }

    @TestOnly
    public static ContentRoot createModule(@NotNull String displayName, @NotNull Project project) {
        return new ContentRoot(displayName, Type.MODULE, project);
    }

    /**
     * The type of the content root.
     */
    public enum Type {
        MODULE, CONTENT_ROOT
    }
}
