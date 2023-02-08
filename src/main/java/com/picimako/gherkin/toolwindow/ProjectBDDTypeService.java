//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Service.Level;
import com.intellij.openapi.project.Project;

/**
 * Project service to store information about what type of BDD files are available in the current project.
 *
 * @see GherkinTagTreeModel
 * @see com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot
 * @since 0.2.0
 */
@Service(Level.PROJECT)
public final class ProjectBDDTypeService {

    public boolean isProjectContainGherkinFile;
    public boolean isProjectContainJBehaveStoryFile;

    public ProjectBDDTypeService(Project project) {
    }

    public boolean hasOnlyJBehaveStoryFiles() {
        return !isProjectContainGherkinFile && isProjectContainJBehaveStoryFile;
    }

    public boolean hasBothGherkinAndStoryFiles() {
        return isProjectContainGherkinFile && isProjectContainJBehaveStoryFile;
    }
}
