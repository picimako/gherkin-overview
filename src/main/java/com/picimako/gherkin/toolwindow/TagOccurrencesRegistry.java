//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static com.picimako.gherkin.toolwindow.TagNameUtil.determineTagOrMetaName;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

/**
 * Stores the tag occurrence counts mapped to Gherkin and Story files' paths and tag names stored in those files.
 * <p>
 * The aim of this project service is to provide a central place and potentially easier logic to query the tag
 * occurrences in contrast to storing this information in the {@link com.picimako.gherkin.toolwindow.nodetype.FeatureFile}s.
 * This way it may be easier to oversee this information.
 */
@RequiredArgsConstructor
@Service(Service.Level.PROJECT)
public final class TagOccurrencesRegistry implements Disposable {

    private final Project project;
    /**
     * FeatureFile path -> &lt;tag name, count>
     */
    @Getter
    @TestOnly //the getter is test-only, and not the field itself
    private Map<String, Map<String, MutableInt>> tagOccurrences;

    /**
     * Initializes the map according to the number of Gherkin and Story files in the project to minimize the allocation size.
     */
    public void init(int bddFileCount) {
        tagOccurrences = new HashMap<>(bddFileCount);
    }

    /**
     * Calculates the tags' occurrence counts in and for the provided file.
     */
    public void calculateOccurrenceCounts(@NotNull VirtualFile file) {
        if (!tagOccurrences.containsKey(file.getPath())) {
            tagOccurrences.put(file.getPath(), new HashMap<>());
            calculateCounts(file);
        }
    }

    /**
     * Updates the tags' occurrence counts for only the provided file.
     */
    public void updateOccurrenceCounts(@NotNull VirtualFile file) {
        var occurrences = tagOccurrences.get(file.getPath());
        if (occurrences != null) occurrences.clear();
        calculateCounts(file);
    }

    private void calculateCounts(@NotNull VirtualFile file) {
        if (!file.exists() || !file.isValid()) return;

        var counts = tagOccurrences.get(file.getPath());
        if (counts == null) return;

        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null) return;

        PsiTreeUtil.processElements(psiFile, element -> {
            if (!element.equals(psiFile)) { //the file itself is definitely not a tag/meta, so can be skipped
                String tagOrMetaName = determineTagOrMetaName(element);
                if (tagOrMetaName != null) {
                    if (counts.containsKey(tagOrMetaName)) {
                        counts.get(tagOrMetaName).increment();
                    } else {
                        counts.put(tagOrMetaName, new MutableInt(1));
                    }
                }
            }
            return true; //continue execution, so that all tags/metas are counted
        });
    }

    /**
     * Gets the occurrence count for the provided file path and tag name.
     *
     * @return the occurrence count, or 0 if it is not present for the provided data
     */
    public int getCountFor(String path, String tag) {
        return Optional.ofNullable(tagOccurrences.get(path))
            .map(tagToCountForPath -> tagToCountForPath.get(tag))
            .map(MutableInt::intValue)
            .orElse(0);
    }

    /**
     * Removes the occurrences mapping for the argument file path.
     */
    public void remove(String path) {
        tagOccurrences.remove(path);
    }

    public static TagOccurrencesRegistry getInstance(Project project) {
        return project.getService(TagOccurrencesRegistry.class);
    }

    @Override
    public void dispose() {
        tagOccurrences.clear();
        tagOccurrences = null;
    }
}
