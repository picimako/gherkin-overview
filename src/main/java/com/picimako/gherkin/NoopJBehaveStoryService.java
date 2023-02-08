//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

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

/**
 * A no-operation implementation of the {@link JBehaveStoryService} to use when the JBehave Support plugin is not
 * installed, or installed but disabled.
 *
 * @see DefaultJBehaveStoryService
 * @since 0.2.0
 */
public class NoopJBehaveStoryService implements JBehaveStoryService {

    @Override
    public @NotNull List<PsiFile> collectStoryFilesFromProject() {
        return Collections.emptyList();
    }

    @Override
    public MultiMap<PsiElement, PsiElement> collectMetasFromFile(PsiFile file) {
        return MultiMap.empty();
    }

    @Override
    public boolean is(PsiElement element, IElementType type) {
        return false;
    }

    @Override
    public Collection<PsiElement> collectMetaTextsForMetaKeyAsList(PsiElement metaKey) {
        return Collections.emptyList();
    }

    @Override
    public boolean isMetaTextForMetaKeyWithName(PsiElement element, String metaName) {
        return false;
    }

    @Override
    public List<String> collectMetasFromFileAsList(PsiFile file) {
        return Collections.emptyList();
    }

    @Override
    public boolean isJBehaveStoryFile(PsiFile file) {
        return false;
    }

    @Override
    public boolean isJBehaveStoryFile(VirtualFile file) {
        return false;
    }

    @Override
    public boolean isMetaKey(PsiElement element) {
        return false;
    }

    @Override
    public PsiFile asStoryFile(PsiElement child) {
        return null;
    }
}
