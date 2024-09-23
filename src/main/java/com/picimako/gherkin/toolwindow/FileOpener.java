//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Utility class for opening files.
 */
final class FileOpener {

    /**
     * Opens the provided file in the default text editor. If the file is already open, it makes that particular
     * editor active.
     *
     * @param file    the file to open
     * @param project the current project
     */
    static void openFile(VirtualFile file, Project project) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        fileEditorManager.openFile(file, true, fileEditorManager.isFileOpen(file));
        fileEditorManager.setSelectedEditor(file, "text-editor");
    }

    private FileOpener() {
        //Utility class
    }
}
