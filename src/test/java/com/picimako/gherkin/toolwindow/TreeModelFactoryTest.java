//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static org.assertj.core.api.Assertions.assertThat;

import com.picimako.gherkin.GherkinOverviewTestBase;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link TreeModelFactory}.
 */
final class TreeModelFactoryTest extends GherkinOverviewTestBase {

    @Test
    void createModel() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        GherkinTagTreeModel treeModel = new TreeModelFactory().createTreeModel(getProject());

        assertThat(treeModel).isInstanceOf(ContentRootBasedGherkinTagTreeModel.class);
    }

    @Test
    void createModelFromExistingDataRoot() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        GherkinTagTreeModel treeModel = new TreeModelFactory().createTreeModel(new ModelDataRoot(getProject()), getProject());

        assertThat(treeModel).isInstanceOf(ContentRootBasedGherkinTagTreeModel.class);

    }
}
