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
import java.util.Collections;
import java.util.List;

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
    public List<String> collectMetasFromFileAsList(PsiFile file) {
        return Collections.emptyList();
    }

    @Override
    public boolean isJBehaveStoryFile(PsiFile file) {
        return false;
    }

    @Override
    public boolean isMetaKey(PsiElement element) {
        return false;
    }
}
