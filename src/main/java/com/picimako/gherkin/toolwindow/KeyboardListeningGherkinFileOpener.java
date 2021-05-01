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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import com.intellij.openapi.project.Project;
import com.picimako.gherkin.toolwindow.nodetype.FeatureFile;
import org.jetbrains.annotations.NotNull;

/**
 * Listens to keyboard key events, so that in case of the proper key hit, the Gherkin file selected in the tool window
 * would open.
 * <p>
 * Currently the open action is bound to the Enter key.
 */
public class KeyboardListeningGherkinFileOpener extends KeyAdapter {

    private final Project project;
    private final GherkinTagTree tree;

    public KeyboardListeningGherkinFileOpener(@NotNull Project project, @NotNull GherkinTagTree tree) {
        this.project = project;
        this.tree = tree;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && tree.getLastSelectedPathComponent() instanceof FeatureFile) {
            FileOpener.openFile(((FeatureFile) tree.getLastSelectedPathComponent()).getFile(), project);
        }
    }
}
