//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.github.kumaraman21.intellijbehave.language.StoryFileType;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.picimako.gherkin.GherkinOverviewTestBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Unit test for {@link FileAndFolderChangeListener}.
 */
final class FileAndFolderChangeListenerTest extends GherkinOverviewTestBase {

    @Test
    void updatesGherkinFile() {
        VirtualFile gherkinFile = Mockito.mock();
        when(gherkinFile.getFileType()).thenReturn(GherkinFileType.INSTANCE);

        var isCalled = new Ref<>(false);
        var listener = new FileAndFolderChangeListener(() -> isCalled.set(true), getProject());
        listener.after(singletonList(new VFileMoveEvent(gherkinFile)));

        assertThat(isCalled.get()).isTrue();
    }

    @Test
    void updatesStoryFile() {
        VirtualFile storyFile = Mockito.mock();
        when(storyFile.getFileType()).thenReturn(StoryFileType.STORY_FILE_TYPE);

        var isCalled = new Ref<>(false);
        var listener = new FileAndFolderChangeListener(() -> isCalled.set(true), getProject());
        listener.after(singletonList(new VFileMoveEvent(storyFile)));

        assertThat(isCalled.get()).isTrue();
    }

    @Test
    void doesntUpdateGherkinFileForDeleteEvent() {
        VirtualFile gherkinFile = Mockito.mock();
        when(gherkinFile.getFileType()).thenReturn(GherkinFileType.INSTANCE);

        var isCalled = new Ref<>(false);
        var listener = new FileAndFolderChangeListener(() -> isCalled.set(true), getProject());
        listener.after(singletonList(new VFileDeleteEvent(gherkinFile)));

        assertThat(isCalled.get()).isFalse();
    }

    @Test
    void doesntUpdateGherkinFileForContentChangeEvent() {
        VirtualFile gherkinFile = Mockito.mock();
        when(gherkinFile.getFileType()).thenReturn(GherkinFileType.INSTANCE);

        var isCalled = new Ref<>(false);
        var listener = new FileAndFolderChangeListener(() -> isCalled.set(true), getProject());
        listener.after(singletonList(new VFileContentChangeEvent(gherkinFile)));

        assertThat(isCalled.get()).isFalse();
    }

    //Dummy events

    private static final class VFileMoveEvent extends DummyVFileEvent {
        VFileMoveEvent(@NotNull VirtualFile file) {
            super(file);
        }
    }

    private static final class VFileDeleteEvent extends DummyVFileEvent {
        VFileDeleteEvent(@NotNull VirtualFile file) {
            super(file);
        }
    }

    private static final class VFileContentChangeEvent extends DummyVFileEvent {
        VFileContentChangeEvent(@NotNull VirtualFile file) {
            super(file);
        }
    }

    /**
     * This class and its implementation are in place because implementations of {@link VFileEvent} are
     * marked as internal.
     */
    private static abstract class DummyVFileEvent extends VFileEvent {
        private final VirtualFile file;

        public DummyVFileEvent(@NotNull VirtualFile file) {
            super(null);
            this.file = file;
        }

        @Override
        protected @NotNull String computePath() {
            return "";
        }

        @Override
        public @Nullable VirtualFile getFile() {
            return file;
        }

        @Override
        public @NotNull VirtualFileSystem getFileSystem() {
            return null;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }
    }
}
