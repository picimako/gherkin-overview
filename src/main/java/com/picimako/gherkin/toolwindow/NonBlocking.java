//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.util.Consumer;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

/**
 * Utility for non-blocking read actions.
 */
public final class NonBlocking {

    /**
     * Performs a non-block read action in smart mode.
     *
     * @param project            the project to pass in for smart mode
     * @param backgroundTask     the task to execute in the background
     * @param consumerOnUiThread the consumer to run on the result of {@code backgroundTask} on the UI thread
     * @param <T>                the type of the result object {@code backgroundTask} returns
     */
    public static <T> void read(Project project, @NotNull Callable<? extends T> backgroundTask, Consumer<? super T> consumerOnUiThread) {
        ReadAction.nonBlocking(backgroundTask)
            .inSmartMode(project)
            .finishOnUiThread(ModalityState.nonModal(), consumerOnUiThread)
            .submit(AppExecutorUtil.getAppExecutorService());
    }

    /**
     * Performs a non-block read action in smart mode, and ignoring the computation result (by returning null).
     *
     * @param project            the project to pass in for smart mode
     * @param backgroundTask     the task to execute in the background
     * @param runnableOnUiThread the actions to run on the UI thread after {@code backgroundTask} finished
     */
    public static void readNoResult(Project project, @NotNull Runnable backgroundTask, Runnable runnableOnUiThread) {
        ReadAction.nonBlocking(() -> {
                backgroundTask.run();
                return null;
            })
            .inSmartMode(project)
            .finishOnUiThread(ModalityState.nonModal(), __ -> runnableOnUiThread.run())
            .submit(AppExecutorUtil.getAppExecutorService());
    }

    private NonBlocking() {
        //Utility class
    }
}
