/*
 *  Copyright 2021 Tamás Balog
 *
 *  Licensed under the Apache License, Version 2.0 \(the "License"\);
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.picimako.gherkin;

import java.util.Collection;
import java.util.List;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;

import com.picimako.gherkin.toolwindow.TagNameUtil;

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
    @NotNull List<PsiFile> collectStoryFilesFromProject();

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
    MultiMap<PsiElement, PsiElement> collectMetasFromFile(PsiFile file);

    /**
     * Collects the meta text elements for the given meta key.
     *
     * @param metaKey the meta key to collect the texts for
     * @return the list of meta text elements, or empty list if the meta key doesn't have meta text associated with it
     */
    Collection<PsiElement> collectMetaTextsForMetaKeyAsList(PsiElement metaKey);

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
    List<String> collectMetasFromFileAsList(PsiFile file);

    /**
     * Returns whether the provided PSI element is of the given type.
     */
    boolean is(PsiElement element, IElementType type);

    boolean isJBehaveStoryFile(PsiFile file);

    /**
     * Convenience method for {@code is(element, META_KEY)} to avoid referencing META_KEY outside any
     * of its implementation classes, e.g. {@link DefaultJBehaveStoryService}.
     */
    boolean isMetaKey(PsiElement element);
}
