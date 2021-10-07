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

package com.picimako.gherkin;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;
import org.jetbrains.plugins.cucumber.psi.GherkinLanguage;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;

import com.picimako.gherkin.toolwindow.TagNameUtil;

/**
 * Provider utility methods for Gherkin files.
 */
public final class GherkinUtil {

    /**
     * Collects all Gherkin files from the provided project.
     *
     * @return the list of Gherkin files, or empty list if no Gherkin file is found
     */
    @NotNull
    public static List<PsiFile> collectGherkinFilesFromProject(@NotNull Project project) {
        if (FileTypeManager.getInstance().findFileTypeByLanguage(GherkinLanguage.INSTANCE) != null) {
            return FileTypeIndex.getFiles(GherkinFileType.INSTANCE, GlobalSearchScope.projectScope(project))
                .stream()
                .map(file -> PsiManager.getInstance(project).findFile(file))
                .collect(toList());
        }
        return Collections.emptyList();
    }

    /**
     * Collects the names of the Gherkin tags (as per {@link TagNameUtil#tagNameFrom(GherkinTag)}) from the provided file.
     *
     * @param file the file to collect the tag names from
     * @return the list of tag names, or empty collection if no tag is found
     */
    @NotNull
    public static List<String> collectGherkinTagsFromFile(PsiFile file) {
        return PsiTreeUtil.findChildrenOfType(file, GherkinTag.class).stream().map(TagNameUtil::tagNameFrom).distinct().collect(toList());
    }

    /**
     * Returns whether the argument file is a Gherkin file.
     */
    public static boolean isGherkinFile(PsiFile file) {
        return GherkinFileType.INSTANCE.equals(file.getFileType());
    }

    /**
     * Returns whether the argument file is a Gherkin file.
     */
    public static boolean isGherkinFile(VirtualFile file) {
        return GherkinFileType.INSTANCE.equals(file.getFileType());
    }

    private GherkinUtil() {
        //Utility class
    }
}
