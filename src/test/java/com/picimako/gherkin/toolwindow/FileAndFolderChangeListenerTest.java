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

import java.util.Collections;

import com.github.kumaraman21.intellijbehave.language.StoryFileType;
import com.intellij.mock.MockApplication;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;
import org.mockito.Mockito;

/**
 * Unit test for {@link FileAndFolderChangeListener}.
 */
public class FileAndFolderChangeListenerTest extends BasePlatformTestCase {

    private Application application;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        application = ApplicationManager.getApplication();
        MockApp.setUp(getTestRootDisposable());
    }

    @Override
    public void tearDown() throws Exception {
        ApplicationManager.setApplication(application, getTestRootDisposable());
        super.tearDown();
    }

    public void testUpdatesGherkinFile() {
        VirtualFile gherkinFile = Mockito.mock(VirtualFile.class);
        Mockito.when(gherkinFile.getFileType()).thenReturn(GherkinFileType.INSTANCE);
        VirtualFile movedGherkinFile = Mockito.mock(VirtualFile.class);

        boolean[] isCalled = new boolean[]{false};
        var listener = new FileAndFolderChangeListener(() -> isCalled[0] = true, getProject());
        listener.after(Collections.singletonList(new VFileMoveEvent(this, gherkinFile, movedGherkinFile)));

        assertThat(isCalled[0]).isTrue();
    }

    public void testUpdatesStoryFile() {
        VirtualFile storyFile = Mockito.mock(VirtualFile.class);
        Mockito.when(storyFile.getFileType()).thenReturn(StoryFileType.STORY_FILE_TYPE);
        VirtualFile movedStoryFile = Mockito.mock(VirtualFile.class);

        boolean[] isCalled = new boolean[]{false};
        var listener = new FileAndFolderChangeListener(() -> isCalled[0] = true, getProject());
        listener.after(Collections.singletonList(new VFileMoveEvent(this, storyFile, movedStoryFile)));

        assertThat(isCalled[0]).isTrue();
    }

    public void testDoesntUpdateGherkinFileForDeleteEvent() {
        VirtualFile gherkinFile = Mockito.mock(VirtualFile.class);
        Mockito.when(gherkinFile.getFileType()).thenReturn(GherkinFileType.INSTANCE);

        boolean[] isCalled = new boolean[]{false};
        var listener = new FileAndFolderChangeListener(() -> isCalled[0] = true, getProject());
        listener.after(Collections.singletonList(new VFileDeleteEvent(this, gherkinFile, false)));

        assertThat(isCalled[0]).isFalse();
    }

    public void testDoesntUpdateGherkinFileForContentChangeEvent() {
        VirtualFile gherkinFile = Mockito.mock(VirtualFile.class);
        Mockito.when(gherkinFile.getFileType()).thenReturn(GherkinFileType.INSTANCE);

        boolean[] isCalled = new boolean[]{false};
        var listener = new FileAndFolderChangeListener(() -> isCalled[0] = true, getProject());
        listener.after(Collections.singletonList(new VFileContentChangeEvent(this, gherkinFile, 0, 0, false)));

        assertThat(isCalled[0]).isFalse();
    }

    public static final class MockApp extends MockApplication {

        public MockApp(@NotNull Disposable parentDisposable) {
            super(parentDisposable);
        }

        @Override
        public void invokeLater(@NotNull Runnable runnable) {
            runnable.run();
        }

        @NotNull
        public static MockApp setUp(@NotNull Disposable parentDisposable) {
            MockApp app = new MockApp(parentDisposable);
            ApplicationManager.setApplication(app, parentDisposable);
            return app;
        }
    }
}
