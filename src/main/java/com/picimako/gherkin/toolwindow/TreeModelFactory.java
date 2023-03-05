//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.intellij.openapi.project.Project;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;

/**
 * Creates models for the {@link javax.swing.JTree} component in the Gherkin Tags tool window.
 */
public final class TreeModelFactory {
    private static final Map<LayoutType, Function<Project, GherkinTagTreeModel>> MODELS = Map.of(
        LayoutType.NO_GROUPING, ProjectSpecificGherkinTagTreeModel::new,
        LayoutType.GROUP_BY_MODULES, ContentRootBasedGherkinTagTreeModel::new
    );

    /**
     * Although a logic could be put in place to reuse the same model object for each layout type (per layout type)
     * instead of creating a new model each time the view is switched, the view most probably won't be changed that
     * many times to have a significant positive performance impact.
     */
    private static final Map<LayoutType, BiFunction<ModelDataRoot, Project, GherkinTagTreeModel>> COPIED_MODELS = Map.of(
        LayoutType.NO_GROUPING, ProjectSpecificGherkinTagTreeModel::new,
        LayoutType.GROUP_BY_MODULES, ContentRootBasedGherkinTagTreeModel::new
    );

    /**
     * Creates a model, based on the layout type set in the tool window, without a predefined {@link ModelDataRoot}.
     * This method is used for the initial building of the tree.
     */
    public GherkinTagTreeModel createTreeModel(Project project) {
        return MODELS.get(GherkinTagsToolWindowSettings.getInstance(project).layout).apply(project);
    }

    /**
     * Creates a model, based on the layout type set in the tool window, with an already existing {@link ModelDataRoot}.
     * This method is used for updating the tree when the layout (display mode) is changed.
     *
     * @param data    the already built model data
     * @param project the project
     */
    public GherkinTagTreeModel createTreeModel(ModelDataRoot data, Project project) {
        return COPIED_MODELS.get(GherkinTagsToolWindowSettings.getInstance(project).layout).apply(data, project);
    }
}
