//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import com.intellij.openapi.project.Project;
import com.picimako.gherkin.toolwindow.nodetype.FeatureFile;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Listens to keyboard key events, so that in case of the proper key hit, the Gherkin file selected in the tool window
 * would open.
 * <p>
 * Currently the open action is bound to the Enter key.
 */
@RequiredArgsConstructor
final class KeyboardListeningGherkinFileOpener extends KeyAdapter {

    @NotNull
    private final Project project;
    @NotNull
    private final GherkinTagTree tree;

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && tree.getLastSelectedPathComponent() instanceof FeatureFile) {
            FileOpener.openFile(((FeatureFile) tree.getLastSelectedPathComponent()).getFile(), project);
        }
    }
}
