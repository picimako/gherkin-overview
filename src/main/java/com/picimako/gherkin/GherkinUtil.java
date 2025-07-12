//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin;

import static com.intellij.openapi.application.ReadAction.compute;
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
            return compute(() -> FileTypeIndex.getFiles(GherkinFileType.INSTANCE, GlobalSearchScope.projectScope(project))
                .stream()
                .map(file -> PsiManager.getInstance(project).findFile(file))
                .collect(toList()));
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
        return compute(() ->
            PsiTreeUtil.findChildrenOfType(file, GherkinTag.class).stream()
                .map(TagNameUtil::tagNameFrom)
                .distinct()
                .collect(toList()));
    }

    /**
     * Returns whether the argument file is a Gherkin file.
     */
    public static boolean isGherkinFile(@NotNull PsiFile file) {
        return GherkinFileType.INSTANCE.equals(file.getFileType());
    }

    /**
     * Returns whether the argument file is a Gherkin file.
     */
    public static boolean isGherkinFile(@NotNull VirtualFile file) {
        return GherkinFileType.INSTANCE.equals(file.getFileType());
    }

    private GherkinUtil() {
        //Utility class
    }
}
