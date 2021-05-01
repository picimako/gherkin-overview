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
}
