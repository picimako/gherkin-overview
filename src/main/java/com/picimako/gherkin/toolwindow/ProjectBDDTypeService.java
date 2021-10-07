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

package com.picimako.gherkin.toolwindow;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

/**
 * Project service to store information about what type of BDD files are available in the current project.
 *
 * @see GherkinTagTreeModel
 * @see com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot
 * @since 0.2.0
 */
@Service
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
