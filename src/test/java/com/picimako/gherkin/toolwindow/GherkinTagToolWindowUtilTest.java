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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.intellij.openapi.wm.ToolWindow;

import com.picimako.gherkin.MediumBasePlatformTestCase;
import com.picimako.gherkin.ToolWindowTestSupport;

/**
 * Unit test for {@link GherkinTagToolWindowUtil}.
 */
public class GherkinTagToolWindowUtilTest extends MediumBasePlatformTestCase {

    //getGherkinTagsToolWindow

    public void testReturnsGherkinTagToolWindow() {
        ToolWindowTestSupport.registerToolWindow(getProject());

        assertThat(GherkinTagToolWindowUtil.getGherkinTagsToolWindow(getProject())).isNotNull();
    }

    public void testDoesntReturnGherkinTagToolWindowIfNotRegistered() {
        assertThatThrownBy(() -> GherkinTagToolWindowUtil.getGherkinTagsToolWindow(getProject()))
            .isInstanceOf(Throwable.class)
            .hasMessage("There is no tool window registered with the id: [gherkin.overview.tool.window.id]");
    }

    //getToolWindowHider

    public void testReturnsToolWindowHider() {
        ToolWindowTestSupport.registerToolWindow(new GherkinTagOverviewPanel(getProject()), getProject());
        ToolWindow gherkinTagsToolWindow = GherkinTagToolWindowUtil.getGherkinTagsToolWindow(getProject());

        assertThat(GherkinTagToolWindowUtil.getToolWindowHider(gherkinTagsToolWindow).getComponent(0)).isInstanceOf(GherkinTagOverviewPanel.class);
    }
}