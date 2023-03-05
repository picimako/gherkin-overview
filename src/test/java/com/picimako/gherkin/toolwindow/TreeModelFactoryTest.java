//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;

/**
 * Unit test for {@link TreeModelFactory}.
 */
public class TreeModelFactoryTest extends BasePlatformTestCase {

    public void testCreateModel() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        GherkinTagTreeModel treeModel = new TreeModelFactory().createTreeModel(getProject());

        assertThat(treeModel).isInstanceOf(ContentRootBasedGherkinTagTreeModel.class);
    }

    public void testCreateModelFromExistingDataRoot() {
        GherkinTagsToolWindowSettings.getInstance(getProject()).layout = LayoutType.GROUP_BY_MODULES;
        GherkinTagTreeModel treeModel = new TreeModelFactory().createTreeModel(new ModelDataRoot(getProject()), getProject());

        assertThat(treeModel).isInstanceOf(ContentRootBasedGherkinTagTreeModel.class);

    }
}
