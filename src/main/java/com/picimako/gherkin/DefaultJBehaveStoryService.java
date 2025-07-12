//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin;

import static com.github.kumaraman21.intellijbehave.highlighter.StoryTokenType.META_KEY;
import static com.github.kumaraman21.intellijbehave.highlighter.StoryTokenType.META_TEXT;
import static com.picimako.gherkin.toolwindow.TagNameUtil.metaNameFrom;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.github.kumaraman21.intellijbehave.language.JBehaveIcons;
import com.github.kumaraman21.intellijbehave.language.StoryFileType;
import com.github.kumaraman21.intellijbehave.language.StoryLanguage;
import com.github.kumaraman21.intellijbehave.parser.StoryFile;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.MultiMap;
import com.picimako.gherkin.toolwindow.TagNameUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Default implementation of the {@link JBehaveStoryService} to use when the JBehave Support plugin is installed
 * and enabled.
 *
 * @see NoopJBehaveStoryService
 * @since 0.2.0
 */
public final class DefaultJBehaveStoryService implements JBehaveStoryService {

    private final Project project;

    //Project service
    public DefaultJBehaveStoryService(Project project) {
        this.project = project;
    }

    @NotNull
    @Override
    public List<PsiFile> collectStoryFilesFromProject() {
        if (FileTypeManager.getInstance().findFileTypeByLanguage(StoryLanguage.STORY_LANGUAGE) != null) {
            return FileTypeIndex.getFiles(StoryFileType.STORY_FILE_TYPE, GlobalSearchScope.projectScope(project))
                .stream()
                .map(file -> PsiManager.getInstance(project).findFile(file))
                .collect(toList());
        }
        return Collections.emptyList();
    }

    @Override
    public MultiMap<PsiElement, PsiElement> collectMetasFromFile(PsiFile file) {
        //meta key -> 0 or more meta text elements
        final var storyMetas = MultiMap.<PsiElement, PsiElement>create();
        PsiTreeUtil.processElements(file, LeafPsiElement.class, potentialMetaKey -> {
            if (isMetaKey(potentialMetaKey)) {
                //This makes sure that Keys are always stored, since they are valid metas with or without meta texts
                storyMetas.put(potentialMetaKey, new SmartList<>());
                collectMetaTextsForMetaKeyAsMap(storyMetas, potentialMetaKey);
            }
            return true;
        });
        return storyMetas;
    }

    /**
     * Collecting the meta text elements is necessary, so that a joined String can be built from them,
     * representing the value portion of a meta key-value pair.
     */
    private void collectMetaTextsForMetaKeyAsMap(MultiMap<PsiElement, PsiElement> storyMetas, LeafPsiElement metaKey) {
        for (var sibling = metaKey.getNextSibling(); sibling != null && !is(sibling, META_KEY); sibling = sibling.getNextSibling()) {
            if (is(sibling, META_TEXT)) {
                storyMetas.putValue(metaKey, sibling);
            }
        }
    }

    @Override
    public Collection<PsiElement> collectMetaTextsForMetaKeyAsList(PsiElement metaKey) {
        final List<PsiElement> metaTexts = new SmartList<>();
        for (var sibling = metaKey.getNextSibling(); sibling != null && !is(sibling, META_KEY); sibling = sibling.getNextSibling()) {
            if (is(sibling, META_TEXT)) {
                metaTexts.add(sibling);
            }
        }
        return metaTexts;
    }

    @Override
    public boolean isMetaTextForMetaKeyWithName(@NotNull PsiElement element, @NotNull String metaName) {
        if (is(element, META_TEXT)) {
            var metaKey = PsiTreeUtil.findSiblingBackward(element, META_KEY, __ -> {
            });
            return metaKey != null && metaName.equals(TagNameUtil.determineTagOrMetaName(metaKey));
        }
        return false;
    }

    @Override
    public List<String> collectMetasFromFileAsList(PsiFile file) {
        return collectMetasFromFile(file).entrySet().stream()
            .map(meta -> metaNameFrom(meta.getKey(), meta.getValue().isEmpty() ? null : meta.getValue()))
            .collect(toList());
    }

    @Override
    public boolean is(PsiElement element, IElementType type) {
        return type.equals(element.getNode().getElementType());
    }

    @Override
    public boolean isJBehaveStoryFile(PsiFile file) {
        return StoryFileType.STORY_FILE_TYPE.equals(file.getFileType());
    }

    @Override
    public boolean isJBehaveStoryFile(VirtualFile file) {
        return StoryFileType.STORY_FILE_TYPE.equals(file.getFileType());
    }

    @Override
    public boolean isMetaKey(PsiElement element) {
        return is(element, META_KEY);
    }

    @Override
    public PsiFile asStoryFile(PsiElement element) {
        return (StoryFile) element;
    }

    @Override
    @Nullable
    public Icon getJBehaveIcon() {
        return JBehaveIcons.JB;
    }
}
