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

import java.util.List;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;

/**
 * Provider utility methods for Gherkin files.
 */
public final class GherkinUtil {

    /**
     * Collects all Gherkin files from the provided project.
     */
    public static List<PsiFile> collectGherkinFilesFromProject(@NotNull Project project) {
        return FileTypeIndex.getFiles(GherkinFileType.INSTANCE, GlobalSearchScope.projectScope(project))
            .stream()
            .map(file -> PsiManager.getInstance(project).findFile(file))
            .collect(toList());
    }

    /**
     * Returns the argument Gherkin tag's name without the leading @ symbol.
     */
    public static String tagNameFrom(GherkinTag tag) {
        return tag.getName().substring(1);
    }

    private GherkinUtil() {
        //Utility class
    }
}
