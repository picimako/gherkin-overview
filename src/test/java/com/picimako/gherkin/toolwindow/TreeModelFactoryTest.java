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
