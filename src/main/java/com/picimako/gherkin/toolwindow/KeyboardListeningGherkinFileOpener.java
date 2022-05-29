//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

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
