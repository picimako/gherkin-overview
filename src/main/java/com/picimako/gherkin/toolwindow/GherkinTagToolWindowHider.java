//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import java.awt.*;
import java.util.function.BiPredicate;
import javax.swing.*;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBPanelWithEmptyText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import com.picimako.gherkin.resources.GherkinBundle;
import com.picimako.gherkin.toolwindow.nodetype.Category;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;

/**
 * A wrapper panel that shows a pre-defined text when there is no Gherkin tag defined in a project.
 */
public class GherkinTagToolWindowHider extends JBPanelWithEmptyText {

    private static final BiPredicate<ModelDataRoot, Project> IS_TAG_PRESENT_IN_PROJECT =
        (modelRoot, proj) -> GherkinTagsToolWindowSettings.getInstance(proj).layout == LayoutType.NO_GROUPING
            ? modelRoot.getCategories().stream().anyMatch(Category::hasTag)
            : modelRoot.getContentRootsByLayout().stream().flatMap(root -> root.getCategories().stream()).anyMatch(Category::hasTag);
    private final Project project;

    public GherkinTagToolWindowHider(@NotNull JComponent gherkinTagOverview, @NotNull Project project, String hiderMessage) {
        super(new BorderLayout());
        this.project = project;
        getEmptyText().setText(hiderMessage);
        add(gherkinTagOverview, BorderLayout.CENTER);
    }

    @TestOnly
    public GherkinTagToolWindowHider(@NotNull JComponent gherkinTagOverview, @NotNull Project project) {
        this(gherkinTagOverview, project, GherkinBundle.toolWindow("no.tag.in.project"));
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
