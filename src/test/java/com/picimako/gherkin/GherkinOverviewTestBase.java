//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase5;
import com.intellij.util.LazyInitializer.LazyValue;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * Base test class for all plugin tests.
 */
public abstract class GherkinOverviewTestBase extends LightJavaCodeInsightFixtureTestCase5 {

    private static final LazyValue<Sdk> REAL_JDK =
        new LazyValue<>(() -> JavaSdk.getInstance().createJdk("Real JDK", System.getenv("JAVA_HOME"), false));

    protected GherkinOverviewTestBase() {
        super(new DefaultLightProjectDescriptor(REAL_JDK::get));
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/testdata";
    }

    protected Project getProject() {
        return getFixture().getProject();
    }

    protected void invokeInWriteActionOnEDTAndWait(@NotNull ThrowableRunnable<Exception> action) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            try {
                WriteAction.run(action);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Shorthand for calling {@code ApplicationManager.getApplication().invokeAndWait(runnable)}.
     */
    protected void invokeAndWait(Runnable runnable) {
        ApplicationManager.getApplication().invokeAndWait(runnable);
    }

    protected PsiFile findPsiFile(VirtualFile file) {
        return ReadAction.computeBlocking(() -> PsiManager.getInstance(getProject()).findFile(file));
    }

    protected VirtualFile configureVirtualFile(String filename) {
        return getFixture().configureByFile(filename).getVirtualFile();
    }

    protected VirtualFile copyFileToProject(String filename) {
        return getFixture().copyFileToProject(filename);
    }

    protected PsiFile configureEmptyFile(String filename) {
        return getFixture().configureByText(filename, "");
    }

    protected PsiFile configureByText(String filename, String text) {
        return getFixture().configureByText(filename, text);
    }

    protected PsiFile configureByFile(String filePath) {
        return getFixture().configureByFile(filePath);
    }

    protected void executeCommandProcessorCommand(Runnable runnable, String name, Object groupId) {
        invokeInWriteActionOnEDTAndWait(() -> CommandProcessor.getInstance().executeCommand(getProject(), runnable, name, groupId));
    }
}
