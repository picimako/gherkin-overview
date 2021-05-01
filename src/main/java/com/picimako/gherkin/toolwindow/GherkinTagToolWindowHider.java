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

import java.awt.*;
import java.util.function.BiPredicate;
import javax.swing.*;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.picimako.gherkin.resources.GherkinBundle;
import com.picimako.gherkin.toolwindow.nodetype.Category;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapper panel that shows a pre-defined text when there is no Gherkin tag defined in a project.
 */
public class GherkinTagToolWindowHider extends JBPanelWithEmptyText {

    private static final BiPredicate<ModelDataRoot, Project> IS_TAG_PRESENT_IN_PROJECT =
        (modelRoot, project) -> GherkinTagsToolWindowSettings.getInstance(project).layout == LayoutType.NO_GROUPING
            ? modelRoot.getCategories().stream().anyMatch(Category::hasTag)
            : modelRoot.getContentRootsByLayout().stream().flatMap(root -> root.getCategories().stream()).anyMatch(Category::hasTag);
    private final Project project;

    public GherkinTagToolWindowHider(@NotNull JComponent gherkinTagOverview, @NotNull Project project) {
        super(new BorderLayout());
        this.project = project;
        getEmptyText().setText(GherkinBundle.toolWindow("no.tag.in.project"));
        add(gherkinTagOverview, BorderLayout.CENTER);
    }

    /**
     * Shows or hides the underlying content based on the data available in the model data.
     * <p>
     * The content is shown only when there is at least one category with at least one tag available.
     *
     * @param modelRoot the Gherkin tag tree's model root
     */
    public void setContentVisibilityBasedOn(ModelDataRoot modelRoot) {
        for (int i = 0, count = getComponentCount(); i < count; i++) {
            getComponent(i).setVisible(IS_TAG_PRESENT_IN_PROJECT.test(modelRoot, project));
        }
    }
}
