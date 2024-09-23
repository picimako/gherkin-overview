//Copyright 2020 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import com.intellij.ui.PopupHandler;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * A custom popup handler that handles mouse events on the JTree displayed in the tool window.
 * <p>
 * When the JTree encounters a mouse right-click, it does the following:
 * <ul>
 *     <li>selects the tree node at the click's location if it's a certain node type, so that a visual confirmation is in place that
 *     context menu actions are supposed to happen on that particular node,</li>
 *     <li>displays the context menu (popup menu) aligned to the click's location</li>
 * </ul>
 * <p>
 * <b>Related articles</b>
 * <p>
 * <ul>
 *     <li><a href="https://intellij-support.jetbrains.com/hc/en-us/community/posts/206140779-Context-menu-howto">Context-menu-howto</a></li>
 *     <li><a href="https://stackoverflow.com/questions/27468337/jtree-select-item-on-right-click">JTree select item on right-click</a></li>
 *     <li><a href="https://stackoverflow.com/questions/517704/right-click-context-menu-for-java-jtree">Right-click context menu for java JTree</a></li>
 *     <li><a href="https://stackoverflow.com/questions/38468270/how-to-implement-a-popup-menu-in-swing-that-works-both-under-windows-and-linux">Swing popup menu for Windows and Linux</a></li>
 *     <li><a href="https://docs.oracle.com/javase/tutorial/uiswing/components/menu.html#popup">Oracle popup menu tutorial</a></li>
 * </ul>
 */
@RequiredArgsConstructor
final class MouseListeningPopupMenuInvoker extends PopupHandler {
    @NotNull
    private final ToolWindowPopupMenuInvoker menuInvoker;

    @Override
    public void invokePopup(Component comp, int x, int y) {
        menuInvoker.invokePopup(comp, x, y);
    }
}
