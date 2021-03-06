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

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.SmartList;
import com.picimako.gherkin.resources.GherkinBundle;
import com.picimako.gherkin.toolwindow.TagOccurrencesRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;

/**
 * Represents a Gherkin Tag in the tool window.
 * <p>
 * One or multiple Gherkin files (as {@link FeatureFile}s) may be bound to a tag, meaning the tag is present in all
 * bound Gherkin files. It may be present one or more times in one file.
 */
public class Tag extends AbstractNodeType {

    private final List<FeatureFile> featureFiles = new SmartList<>();

    /**
     * The reason a VirtualFile is required is that a tag is displayed only when it has at least one Gherkin file
     * associated to it.
     */
    public Tag(@NotNull String displayName, @NotNull VirtualFile initialFile, @NotNull Project project) {
        super(displayName, project);
        featureFiles.add(new FeatureFile(initialFile, displayName, project));
    }

    public List<FeatureFile> getFeatureFiles() {
        return featureFiles;
    }

    public boolean hasFeatureFile() {
        return !featureFiles.isEmpty();
    }

    /**
     * Gets whether the argument virtual file is assigned to this tag.
     *
     * @param gherkinFile the gherkin file
     * @return true if the file is assigned, false otherwise
     */
    public boolean contains(VirtualFile gherkinFile) {
        return featureFiles.stream().anyMatch(featureFile -> Objects.equals(featureFile.getFile(), gherkinFile));
    }

    /**
     * Adds the provided file to this tag if it isn't already added.
     * <p>
     * If, after adding the file, there are multiple files with its name, linked to this tag, than their display names
     * are updated to contain to contain the Feature keywords or the relative paths from the project root in their
     * display names.
     *
     * @param file the file to add
     */
    public Tag add(@NotNull VirtualFile file) {
        if (!this.contains(file)) {
            FeatureFile featureFile = new FeatureFile(file, displayName, project);
            featureFiles.add(featureFile);
            updateDisplayNames(file);
        }
        return this;
    }

    /**
     * Updates the display names of files that have the same name as the argument file.
     *
     * @param file the file to update display names based on
     */
    public void updateDisplayNames(@NotNull VirtualFile file) {
        List<FeatureFile> featureFilesWithTheSameName;
        if (featureFiles.size() > 1 && (featureFilesWithTheSameName = getFeatureFilesWithTheNameOf(file)).size() > 1) {
            updateDisplayNamesOf(featureFilesWithTheSameName);
        }
    }

    /**
     * Removes the provided file from the underlying set of linked files.
     * <p>
     * When a Gherkin file is removed, and with that name only one file remains under this tag, then the remaining
     * Gherkin file's display name is restored to the file's name from the relative path.
     * <p>
     * If more than one file remains with that name they are examined and updated to contain the Feature keywords
     * or the relative paths from the project root in their display names.
     *
     * @param file the file to remove
     */
    public void remove(@NotNull VirtualFile file) {
        featureFiles.removeIf(featureFile -> featureFile.getPath().equals(file.getPath()));

        if (featureFiles.size() == 1) {
            featureFiles.get(0).resetDisplayName();
        } else if (featureFiles.size() > 1) {
            var featureFilesWithTheSameName = getFeatureFilesWithTheNameOf(file);
            if (featureFilesWithTheSameName.size() == 1) {
                featureFilesWithTheSameName.get(0).resetDisplayName();
            } else if (featureFilesWithTheSameName.size() > 1) {
                updateDisplayNamesOf(featureFilesWithTheSameName);
            }
        }
    }

    /**
     * If all files with the same name have different top level Feature keyword names, then those values are used
     * besides the filenames to identify which file is which, otherwise instead of using the Feature names, the
     * files' relative path from the project root are set as identifiers.
     */
    private void updateDisplayNamesOf(List<FeatureFile> featureFilesWithTheSameName) {
        var distinctFeatureNames = featureFilesWithTheSameName.stream()
            .map(featureFile -> PsiManager.getInstance(project).findFile(featureFile.getFile()))
            .filter(Objects::nonNull)
            .map(psiFile -> ((GherkinFile) psiFile).getFeatures())
            .filter(features -> features.length > 0)
            .map(features -> features[0].getFeatureName()) //regardless of the number of Feature keywords in the file, it always takes the first one if there is at least one
            .distinct()
            .collect(toList());

        if (distinctFeatureNames.size() == featureFilesWithTheSameName.size()) {
            for (int i = 0; i < distinctFeatureNames.size(); i++) {
                featureFilesWithTheSameName.get(i).setDisplayNameWithFeatureName(distinctFeatureNames.get(i));
            }
        } else {
            featureFilesWithTheSameName.forEach(FeatureFile::setDisplayNameWithPath);
        }
    }

    private List<FeatureFile> getFeatureFilesWithTheNameOf(VirtualFile file) {
        return featureFiles.stream().filter(featureFile -> featureFile.hasFileName(file.getName())).collect(toList());
    }

    /**
     * Sorts the Gherkin files by their filenames if there is more than one Gherkin file in this tag.
     */
    @Override
    public void sort() {
        if (featureFiles.size() > 1) {
            featureFiles.sort(comparing(featureFile -> featureFile.getName().toLowerCase()));
        }
    }

    @Override
    public String toString() {
        return getToString(
            () -> displayName + " (" + occurrenceCount() + ")",
            () -> GherkinBundle.toolWindow("statistics.tag.detailed", displayName, occurrenceCount(), featureFiles.size()));
    }

    /**
     * Gets the overall tag count for this tag.
     * <p>
     * It goes through only those files that this is actually in.
     */
    int occurrenceCount() {
        var registry = TagOccurrencesRegistry.getInstance(project);
        return featureFiles.stream()
            .mapToInt(file -> registry.getCountFor(file.getPath(), displayName))
            .sum();
    }

    @TestOnly
    public List<VirtualFile> getGherkinFiles() {
        return featureFiles.stream().map(FeatureFile::getFile).collect(toList());
    }
}
