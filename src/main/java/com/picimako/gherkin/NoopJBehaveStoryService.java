//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.MultiMap;
import com.picimako.gherkin.jbehave.DefaultJBehaveStoryService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A no-operation implementation of the {@link JBehaveStoryService} to use when the JBehave Support plugin is not
 * installed, or installed but disabled.
 *
 * @see DefaultJBehaveStoryService
 * @since 0.2.0
 */
public final class NoopJBehaveStoryService implements JBehaveStoryService {

    public NoopJBehaveStoryService(Project project) {
    }

    @Override
    public @NotNull List<PsiFile> collectStoryFilesFromProject() {
        return Collections.emptyList();
    }

    @Override
    public MultiMap<PsiElement, PsiElement> collectMetasFromFile(PsiFile file) {
        return MultiMap.empty();
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
    public boolean is(PsiElement element, IElementType type) {
        return false;
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

    @Nullable
    @Override
    public Icon getJBehaveIcon() {
        return null;
    }
}
