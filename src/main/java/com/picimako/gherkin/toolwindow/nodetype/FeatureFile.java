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

import java.util.Objects;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.picimako.gherkin.resources.GherkinBundle;
import com.picimako.gherkin.toolwindow.TagOccurrencesRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a Gherkin file with the .feature extension.
 * <p>
 * This is a wrapper for a {@link VirtualFile}, so that statistics information from them can be queried
 * and stored. For this reason, it also stores the tag's name this file is assigned to, so that it knows the occurrence
 * count of which tag it should display.
 */
public class FeatureFile extends AbstractNodeType {

    private final VirtualFile file;
    private final String parentTag;

    public FeatureFile(@NotNull VirtualFile file, String parentTag, Project project) {
        super(file.getName(), project);
        this.file = file;
        this.parentTag = parentTag;
        TagOccurrencesRegistry.getInstance(project).calculateOccurrenceCounts(file);
    }

    public VirtualFile getFile() {
        return file;
    }

    public String getPath() {
        return file.getPath();
    }

    public String getName() {
        return file.getName();
    }

    public boolean hasFileName(String name) {
        return name != null && name.equals(file.getName());
    }

    void resetDisplayName() {
        displayName = file.getName();
    }

    /**
     * Sets the display name to the combination of the file's name and argument Feature keyword text.
     * <p>
     * For instance, with a file name {@code smoke.feature} and a Feature name {@code Smoke testing}, it sets
     * the string {@code smoke.feature [Smoke testing]} as the display name.
     *
     * @param featureName the Feature keywords text
     */
    public void setDisplayNameWithFeatureName(String featureName) {
        displayName = file.getName() + " [" + featureName + "]";
    }

    /**
     * Sets the display name to the combination of the file's name and its relative path to the project's root folder.
     * <p>
     * The following cases are viable:
     * <ul>
     *     <li>in case there is no relative path for some reason, then the display name is the file's name, e.g. {@code "smoke.feature"}</li>
     *     <li>if the relative path is empty (the file is located in the project root folder), then the display name is set as e.g. {@code "smoke.feature [/]"}</li>
     *     <li>if the file is located somewhere deeper in the project, then the display name is set as e.g. {@code "smoke.feature [module-name/src/main/resources/features]"}</li>
     * </ul>
     */
    void setDisplayNameWithPath() {
        String relativePath = VfsUtil.getRelativePath(file.getParent(), ProjectUtil.guessProjectDir(project));
        displayName = relativePath != null
            ? file.getName() + " [" + (relativePath.isEmpty() ? "/" : relativePath) + "]"
            : file.getName();
    }

    @Override
    public String toString() {
        return getToString(
            () -> displayName + " (" + count() + ")",
            () -> GherkinBundle.toolWindow("statistics.feature.file.detailed", displayName, count()));
    }

    private int count() {
        return TagOccurrencesRegistry.getInstance(project).getCountFor(file.getPath(), parentTag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeatureFile that = (FeatureFile) o;
        return Objects.equals(file.getPath(), that.file.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }
}
