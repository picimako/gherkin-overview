//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import java.util.List;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import com.picimako.gherkin.BDDUtil;

/**
 * Listener to rebuild the tool window model in case of file and folder changes.
 *
 * <strong>NOTE:</strong>
 * Due to the nature of this plugin, that it supports any kind of projects with Gherkin and Story files,
 * limiting the directory and file change events to ones only in test resources is not achievable since it would be
 * difficult or even impossible to handle different languages and project type structures.
 * <p>
 * Thus, more frequent tool window updates and model rebuilds are expected mostly during various folder related changes.
 */
@RequiredArgsConstructor
public class FileAndFolderChangeListener implements BulkFileListener {

    private final Runnable rebuildModel;
    private final Project project;

    /**
     * Updating the UI is necessary because in case of e.g. a file rename, or a Git bulk rollback/revert of files,
     * the tool window may get stuck, or end up in a broken state.
     * <p>
     * Since "It's prohibited to access index #filetypes during event dispatching",
     * (see {@link com.intellij.openapi.project.NoAccessDuringPsiEvents}),
     * model rebuild is invoked later when all events have been processed.
     */
    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        if (events.stream().anyMatch(this::isChangeOnFolderOrBDDFile)) {
            ApplicationManager.getApplication().invokeLater(rebuildModel);
        }
    }

    private boolean isChangeOnFolderOrBDDFile(VFileEvent event) {
        return event.getFile() != null && (isEventOnDirectory(event) || isEventOnBDDFile(event));
    }

    /**
     * Folder creation is ignored since there is no BDD file in it yet that makes the model rebuild necessary.
     */
    private boolean isEventOnDirectory(VFileEvent event) {
        return event.getFile().isDirectory() && !(event instanceof VFileCreateEvent);
    }

    /**
     * BDD file content change and file deletion events are handled by {@link GherkinPsiChangeListener}.
     */
    private boolean isEventOnBDDFile(VFileEvent event) {
        return BDDUtil.isABDDFile(event.getFile(), project) && !(event instanceof VFileContentChangeEvent) && !(event instanceof VFileDeleteEvent);
    }
}
