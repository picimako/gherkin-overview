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

package com.picimako.gherkin.toolwindow;

import static com.picimako.gherkin.GherkinUtil.tagNameFrom;

import java.util.HashMap;
import java.util.Map;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;

/**
 * Stores the tag occurrence counts mapped to Gherkin files' paths and tag names stored in those files.
 * <p>
 * The aim of this project service is to provide a central place and potentially easier logic to query the tag
 * occurrences in contrast to storing this information in the {@link com.picimako.gherkin.toolwindow.nodetype.FeatureFile}s.
 * This way it may be easier to oversee this information.
 */
@Service
public final class TagOccurrencesRegistry {

    /**
     * FeatureFile path -> &lt;tag name, count>
     */
    private Map<String, Map<String, MutableInt>> tagOccurrences;
    private final Project project;

    public TagOccurrencesRegistry(Project project) {
        this.project = project;
    }

    /**
     * Initializes the map according to the number of Gherkin files in the project to minimize the allocation size.
     */
    public void init(int gherkinFileCount) {
        tagOccurrences = new HashMap<>(gherkinFileCount);
    }

    /**
     * Calculates the tags' occurrence counts in and for the provided file.
     */
    public void calculateOccurrenceCounts(@NotNull VirtualFile file) {
        if (!tagOccurrences.containsKey(file.getPath())) {
            tagOccurrences.put(file.getPath(), new HashMap<>()); //TODO: this may be optimized by reducing the initial capacity
            calculateCounts(file);
        }
    }

    /**
     * Updates the tags' occurrence counts for only the provided file.
     */
    public void updateOccurrenceCounts(@NotNull VirtualFile file) {
        tagOccurrences.get(file.getPath()).clear();
        calculateCounts(file);
    }

    private void calculateCounts(@NotNull VirtualFile file) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile != null) {
            var counts = tagOccurrences.get(file.getPath());
            PsiTreeUtil.processElements(psiFile, GherkinTag.class, element -> {
                String tagName = tagNameFrom(element);
                if (counts.containsKey(tagName)) {
                    counts.get(tagName).increment();
                } else {
                    counts.put(tagName, new MutableInt(1));
                }
                return true;
            });
        }
    }

    /**
     * Gets the occurrence count for the provided file path and tag name.
     *
     * @return the occurrence count, or 0 if it is not present for the provided data
     */
    public int getCountFor(String path, String tag) {
        MutableInt mutableInt = tagOccurrences.get(path).get(tag);
        return mutableInt != null ? mutableInt.intValue() : 0;
    }

    /**
     * Removes the occurrences mapping for the argument file path.
     */
    public void remove(String path) {
        tagOccurrences.remove(path);
    }

    @TestOnly
    public Map<String, Map<String, MutableInt>> getTagOccurrences() {
        return tagOccurrences;
    }

    public static TagOccurrencesRegistry getInstance(Project project) {
        return ServiceManager.getService(project, TagOccurrencesRegistry.class);
    }
}
