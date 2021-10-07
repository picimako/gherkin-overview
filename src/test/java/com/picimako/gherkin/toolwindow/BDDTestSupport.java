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

import static com.github.kumaraman21.intellijbehave.highlighter.StoryTokenType.META_KEY;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;

import com.picimako.gherkin.DefaultJBehaveStoryService;

/**
 * Utility for handling Gherkin Tags and Story metas in tests.
 */
public final class BDDTestSupport {

    /**
     * Returns the first {@link GherkinTag} from the provided file for the given tag name.
     *
     * @param psiFile the file to get the tag from
     * @param tagName the tag name including the leading @ symbol
     * @return the first tag for the given name, or null if no tag found with that name
     */
    @Nullable
    public static GherkinTag getFirstGherkinTagForName(PsiFile psiFile, String tagName) {
        GherkinTag[] tag = new GherkinTag[1];
        PsiTreeUtil.processElements(psiFile, GherkinTag.class, element -> {
            if (tagName.equals(element.getName())) {
                tag[0] = element;
                return false;
            }
            return true;
        });
        return tag[0];
    }

    /**
     * Returns the first {@link com.github.kumaraman21.intellijbehave.highlighter.StoryTokenType#META_KEY} from the
     * provided file for the given meta key name.
     *
     * @param psiFile     the file to get the meta key from
     * @param metaKeyName the meta key name including the leading @ symbol
     * @return the first meta key for the given name, or null if no meta key found with that name
     */
    @Nullable
    public static PsiElement getFirstMetaKeyForName(PsiFile psiFile, String metaKeyName) {
        PsiElement[] metaKey = new PsiElement[1];
        PsiTreeUtil.processElements(psiFile, LeafPsiElement.class, potentialMetaKey -> {
            if (new DefaultJBehaveStoryService(psiFile.getProject()).is(potentialMetaKey, META_KEY) && metaKeyName.equals(potentialMetaKey.getText())) {
                metaKey[0] = potentialMetaKey;
                return false;
            }
            return true;
        });
        return metaKey[0];
    }

    private BDDTestSupport() {
        //Utility class
    }
}
