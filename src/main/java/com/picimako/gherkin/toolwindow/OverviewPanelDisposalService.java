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

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

/**
 * No-op light service with the sole purpose of acting as a parent disposable of {@link GherkinPsiChangeListener} in {@link GherkinTagOverviewPanel}.
 */
@Service(Service.Level.PROJECT)
public final class OverviewPanelDisposalService implements Disposable {
    
    @Override
    public void dispose() {
    }

    public static OverviewPanelDisposalService getInstance(Project project) {
        return project.getService(OverviewPanelDisposalService.class);
    }
}
