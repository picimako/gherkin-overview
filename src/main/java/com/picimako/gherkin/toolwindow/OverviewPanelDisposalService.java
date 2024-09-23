//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

/**
 * No-op light service with the sole purpose of acting as a parent disposable of {@link GherkinPsiChangeListener} in {@link GherkinTagOverviewPanel}.
 */
@Service(Service.Level.PROJECT)
final class OverviewPanelDisposalService implements Disposable {

    @SuppressWarnings("unused")
    OverviewPanelDisposalService(Project project) {
    }

    @Override
    public void dispose() {
    }

    public static OverviewPanelDisposalService getInstance(Project project) {
        return project.getService(OverviewPanelDisposalService.class);
    }
}
