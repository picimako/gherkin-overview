//Copyright 2026 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Returns a group of actions for the Tag node type.
 */
public final class TagActionsGroup extends DefaultActionGroup {
    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        var project = e.getData(CommonDataKeys.PROJECT);
        return project != null ? new AnAction[]{new DeleteAllTagOccurrencesAction(project)} : AnAction.EMPTY_ARRAY;
    }
}
