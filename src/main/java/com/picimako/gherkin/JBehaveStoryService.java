//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;

import com.picimako.gherkin.toolwindow.TagNameUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Interface for project services to provide JBehave Story files related data.
 * <p>
 * It is implemented in a way to prevent ClassNotFoundExceptions and NoClassDefFoundExceptions due to
 * JBehave Support as dependency being optional.
 * <p>
 * If the plugin is not installed or disabled, and there is a reference to a class during class loading, classes from
 * JBehave Support won't be available, thus resulting in the aforementioned exceptions.
 *
 * @see DefaultJBehaveStoryService
 * @see NoopJBehaveStoryService
 * @since 0.2.0
 */
public interface JBehaveStoryService {

    /**
     * Collects all JBehave Story files from the provided project.
     *
     * @return the list of Story files, or empty list if no Story file is found
     */
    default @NotNull List<PsiFile> collectStoryFilesFromProject() {
        return Collections.emptyList();
    }

    /**
     * Collects all Metas from the provided file as a 1-N, Meta key to Meta text(s) mapping.
     * <p>
     * Since this method collects elements rather than Strings, if the same meta (by its text) is
     * present multiple times in the file, then all occurrences will be returned by this method too.
     * <p>
     * Although standalone meta texts are recognized as valid metas by the JBehave Support plugin, they are not
     * recognized as such by JBehave itself. A valid meta is either a standalone key, or a key-value pair.
     *
     * @param file the story file to collect metas from
     * @return the metas as a mapping, or empty map if no meta is found
     */
    default MultiMap<PsiElement, PsiElement> collectMetasFromFile(PsiFile file) {
        return MultiMap.empty();
    }

    /**
     * Collects the meta text elements for the given meta key.
     *
     * @param metaKey the meta key to collect the texts for
     * @return the list of meta text elements, or empty list if the meta key doesn't have meta text associated with it
     */
    default Collection<PsiElement> collectMetaTextsForMetaKeyAsList(PsiElement metaKey) {
        return Collections.emptyList();
    }

    /**
     * Returns whether {@code element} is a meta text and it belongs to a meta key whose name (meta key + meta texts)
     * matches the provided meta name.
     *
     * @param element  the element to inspect
     * @param metaName the meta name against which the found meta name should match
     */
    default boolean isMetaTextForMetaKeyWithName(PsiElement element, String metaName) {
        return false;
    }

    /**
     * Collects combined meta names (as per {@link TagNameUtil#metaNameFrom(PsiElement, Collection)}) from the provided file.
     * <p>
     * Returned metas are not distinct. If the same meta is present multiple times in the file, then all occurrences
     * will be returned by this method too.
     *
     * @param file the story file to collect metas from
     * @return the list of meta names, or empty list if none found
     * @see #collectMetasFromFile(PsiFile)
     */
    default List<String> collectMetasFromFileAsList(PsiFile file) {
        return Collections.emptyList();
    }

    /**
     * Returns whether the provided PSI element is of the given type.
     */
    default boolean is(PsiElement element, IElementType type) {
        return false;
    }

    default boolean isJBehaveStoryFile(PsiFile file) {
        return false;
    }

    default boolean isJBehaveStoryFile(VirtualFile file) {
        return false;
    }

    /**
     * Convenience method for {@code is(element, META_KEY)} to avoid referencing META_KEY outside any
     * of its implementation classes, e.g. {@link DefaultJBehaveStoryService}.
     */
    default boolean isMetaKey(PsiElement element) {
        return false;
    }

    default PsiFile asStoryFile(PsiElement child) {
        return null;
    }

    @Nullable
    default Icon getJBehaveIcon() {
        return null;
    }
}
