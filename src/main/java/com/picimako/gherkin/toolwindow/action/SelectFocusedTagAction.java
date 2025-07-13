//Copyright 2025 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.action;

import static com.intellij.openapi.application.ReadAction.compute;
import static com.picimako.gherkin.toolwindow.GherkinTagToolWindowUtil.getGherkinTagOverViewPanel;
import static com.picimako.gherkin.toolwindow.GherkinTagToolWindowUtil.getGherkinTagsToolWindow;
import static com.picimako.gherkin.toolwindow.LayoutType.NO_GROUPING;
import static com.picimako.gherkin.toolwindow.TagNameUtil.tagNameFrom;

import javax.swing.tree.TreePath;
import java.util.Optional;

import com.intellij.icons.AllIcons;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.AnActionButton;
import com.picimako.gherkin.resources.GherkinBundle;
import com.picimako.gherkin.toolwindow.GherkinTagTree;
import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;
import com.picimako.gherkin.toolwindow.TagCategoryRegistry;
import com.picimako.gherkin.toolwindow.nodetype.Category;
import com.picimako.gherkin.toolwindow.nodetype.ContentRoot;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import com.picimako.gherkin.toolwindow.nodetype.NodeType;
import com.picimako.gherkin.toolwindow.nodetype.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;

/**
 * This action, similar to the Project View's 'Select Opened File' tool bar action, locates and selects the Tag node
 * of a Gherkin Tag that is under the caret in the currently selected text editor.
 * <p>
 * This action is disabled for JBehave Story metas for now, or when there is more than one caret placed in the current editor.
 */
public final class SelectFocusedTagAction extends AnActionButton {

    public SelectFocusedTagAction() {
        super(
            GherkinBundle.message("gherkin.overview.toolwindow.select.focused.tag.tooltip"),
            GherkinBundle.message("gherkin.overview.toolwindow.select.focused.tag.description"),
            AllIcons.General.Locate);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getData(CommonDataKeys.PROJECT);
        var editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        var psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());

        var elementAtCaret = compute(() -> psiFile.findElementAt(editor.getCaretModel().getOffset()));
        locateAndSelectGherkinTag(project, psiFile, elementAtCaret);
    }

    /**
     * Locates the Gherkin tag at the {@code elementAtCaret} within the Gherkin Tags tool window, and selects the
     * corresponding tree node, also focusing on the tree itself.
     *
     * @param project        the current project
     * @param psiFile        the PSI file in which a Gherkin tag is under the caret
     * @param elementAtCaret the element at the current, single caret offset
     */
    private static void locateAndSelectGherkinTag(Project project, PsiFile psiFile, PsiElement elementAtCaret) {
        //Fetch the Gherkin tag tool window UI component, so that we can work the underlying JTree and its model
        var gherkinTagsToolWindow = getGherkinTagsToolWindow(project);
        if (gherkinTagsToolWindow == null) return;

        var tagLocator = new TagLocator(gherkinTagsToolWindow, elementAtCaret, project);
        var layout = GherkinTagsToolWindowSettings.getInstance(project).layout;
        if (layout == NO_GROUPING)
            tagLocator.locateAndSelectGherkinTagInProject();
        else
            tagLocator.locateAndSelectGherkinTagInContentRoot(psiFile);
    }

    @Override
    public void updateButton(@NotNull AnActionEvent e) {
        var project = e.getData(CommonDataKeys.PROJECT);

        var shouldBeEnabled = Optional.ofNullable(project)
            .map(__ -> FileEditorManager.getInstance(project).getSelectedTextEditor())
            .map(editor -> {
                //The action is disabled when there is less or more than 1 caret in the editor
                if (editor.getCaretModel().getCaretCount() != 1) return null;

                var psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
                return psiFile != null ? compute(() -> psiFile.findElementAt(editor.getCaretModel().getOffset())) : null;
            })
            .map(PsiElement::getNode)
            .map(ASTNode::getElementType)
            //The action is enabled only for Gherkin tag elements
            .filter(elementType -> elementType == GherkinTokenTypes.TAG)
            .isPresent();

        e.getPresentation().setEnabled(shouldBeEnabled);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    /**
     * Helpers class to locate and select the Gherkin tag inside the Gherkin Tags tool window.
     * <p>
     * The selection logic is a workaround because the retrieval of tree node paths for path-based selection in the JTree doesn't seem to be working.
     */
    private static final class TagLocator {
        private final Project project;
        private final PsiElement elementAtCaret;
        private final GherkinTagTree gherkinTagTree;
        private final ModelDataRoot root;

        private TagLocator(ToolWindow gherkinTagsToolWindow, PsiElement elementAtCaret, Project project) {
            this.gherkinTagTree = getGherkinTagOverViewPanel(gherkinTagsToolWindow).getTree();
            this.root = (ModelDataRoot) gherkinTagTree.getModel().getRoot();
            this.elementAtCaret = elementAtCaret;
            this.project = project;
        }

        /**
         * Locates and selects the Gherkin Tag when the tool window has no grouping set.
         */
        private void locateAndSelectGherkinTagInProject() {
            String tagName = tagNameFrom(compute(() -> (GherkinTag) elementAtCaret.getParent()));

            //Find the category node for the category of the selected Gherkin tag, or use the fallback category 'Other'
            var categoryNode = Optional.ofNullable(TagCategoryRegistry.getInstance(project).categoryOf(tagName))
                .flatMap(root::findCategory)
                .or(() -> Optional.of(root.getOther()));

            //When the Category node has a Tag node for the source tag name...
            categoryNode
                .flatMap(cat -> cat.get(tagName))
                .ifPresent(tag -> {
                    //Expand the root node, just in case someone would decide to collapse the entire tree
                    gherkinTagTree.expandRow(0);

                    /*
                     * Find the category, and expand it.
                     * Looping starts from 1, because the 0th item is always the root, and is never a Category
                     */
                    for (int categoryRow = 1; categoryRow < gherkinTagTree.getVisibleRowCount(); categoryRow++) {
                        if (isTagWithinCategoryFound(categoryNode.get(), tag, categoryRow)) break;
                    }
                });
        }

        /**
         * Locates and selects the Gherkin Tag when the tool window has 'Group by modules' grouping set.
         *
         * @param psiFile the PSI file in which the Gherkin tag is under the caret. Need for identifying the module/content root
         *                of the file, so that the tag node can be located properly under the corresponding content root node.
         */
        private void locateAndSelectGherkinTagInContentRoot(PsiFile psiFile) {
            String tagName = tagNameFrom((GherkinTag) elementAtCaret.getParent());

            /*
             * Find the category node for the category of the select Gherkin tag, inside the content root the source PSI file is located in,
             * or in 'Rootless'.
             * Or use the fallback category 'Other' within that content root.
             */
            var contentRootOrRootless = root.findContentRootOrRootless(psiFile);
            var categoryNode = Optional.ofNullable(TagCategoryRegistry.getInstance(project).categoryOf(tagName))
                .flatMap(contentRootOrRootless::findCategory)
                .or(() -> Optional.of(contentRootOrRootless.getOther()));

            //When the Category node has a Tag node for the source tag name...
            categoryNode
                .flatMap(cat -> cat.get(tagName))
                .ifPresent(tag -> {
                    //Expand the root node, just in case someone would decide to collapse the entire tree
                    gherkinTagTree.expandRow(0);

                    /*
                     * Find the content root, and expand it.
                     * Looping starts from 1, because the 0th item is always the upmost root, and is never a content root
                     */
                    for (int contentRootRow = 1; contentRootRow < gherkinTagTree.getVisibleRowCount(); contentRootRow++) {
                        if (isContentRootFound(contentRootOrRootless, contentRootRow)) {
                            //Expand the Content Root node, so that we can iterate over the newly appeared Category nodes
                            gherkinTagTree.expandRow(contentRootRow);

                            /*
                             * Find the category, and expand it.
                             * Looping starts from the node right after the current content root node, because categories always start from the next item.
                             */
                            for (int categoryRow = contentRootRow + 1; categoryRow < gherkinTagTree.getVisibleRowCount(); categoryRow++) {
                                if (isTagWithinCategoryFound(categoryNode.get(), tag, categoryRow)) break;
                            }
                            break;
                        }
                    }
                });
        }

        /**
         * Returns if the node at the given row index in the tree matches the provided {@code tag} under {@code category}.
         *
         * @param category    the focused tag element's corresponding Category node
         * @param tag         the focused tag element's corresponding Tag node
         * @param categoryRow the row index inspected
         */
        private boolean isTagWithinCategoryFound(Category category, Tag tag, int categoryRow) {
            if (isCategoryFound(category, categoryRow)) {
                //Expand the Category node, so that we can iterate over the newly appeared Tag nodes
                gherkinTagTree.expandRow(categoryRow);

                /*
                 * Find the tag, and select it.
                 * Looping starts from the node right after the current category node, because tags always start from the next item.
                 */
                for (int tagRow = categoryRow + 1; tagRow < gherkinTagTree.getVisibleRowCount(); tagRow++) {
                    if (isTagFound(tag, tagRow)) {
                        //Focusing on the tree, so that users can interact instantly with it via the keyboard, and don't have to focus on it manually.
                        gherkinTagTree.requestFocusInWindow();
                        gherkinTagTree.setSelectionRow(tagRow);
                        return true;
                    }
                }
            }

            return false;
        }

        private boolean isContentRootFound(ContentRoot contentRootOrRootless, int row) {
            return isNodeFound(contentRootOrRootless, ContentRoot.class, row);
        }

        private boolean isCategoryFound(Category categoryNode, int row) {
            return isNodeFound(categoryNode, Category.class, row);
        }

        private boolean isTagFound(Tag tag, int row) {
            return isNodeFound(tag, Tag.class, row);
        }

        /**
         * Returns if the node at the given row index in the tree is the node corresponding to {@code nodeType}
         * with the type {@code nodeClass}.
         */
        private boolean isNodeFound(NodeType nodeType, Class<? extends NodeType> nodeClass, int row) {
            return Optional.ofNullable(gherkinTagTree.getPathForRow(row))
                .map(TreePath::getLastPathComponent)
                .filter(nodeClass::isInstance)
                .filter(lastPathComponent -> lastPathComponent.equals(nodeType))
                .isPresent();
        }
    }
}
