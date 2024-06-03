//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.action;

import static com.intellij.openapi.ui.Messages.YES;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.picimako.gherkin.BDDUtil;
import com.picimako.gherkin.JBehaveStoryService;
import com.picimako.gherkin.resources.GherkinBundle;
import com.picimako.gherkin.toolwindow.GherkinTagTree;
import com.picimako.gherkin.toolwindow.TagNameUtil;
import com.picimako.gherkin.toolwindow.TagOccurrencesRegistry;
import com.picimako.gherkin.toolwindow.nodetype.FeatureFile;
import com.picimako.gherkin.toolwindow.nodetype.NodeType;
import com.picimako.gherkin.toolwindow.nodetype.Tag;
import icons.CollaborationToolsIcons;
import org.jetbrains.annotations.NotNull;

/**
 * This action deletes all occurrences of a tag/meta selected in the Gherkin Tag tool window, after users
 * confirm that they really want to delete them.
 * <p>
 * In case of Gherkin tags, the deletion matches whole tag names against the selected one, while in case of Story metas,
 * the meta key and all meta texts are matched.
 *
 * @since 1.2.0
 */
public class DeleteAllTagOccurrencesAction extends AnAction {
    private final Project project;

    public DeleteAllTagOccurrencesAction(Project project) {
        super(GherkinBundle.message("gherkin.overview.toolwindow.delete.tags"), "", CollaborationToolsIcons.Delete);
        this.project = project;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var tree = (GherkinTagTree) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);

        if (tree != null && isGherkinTag(tree.getLastSelectedPathComponent()) && isUserSureToDeleteAllOccurrencesOfTag(project)) {
            var tagOccurrencesRegistry = TagOccurrencesRegistry.getInstance(project);
            Tag selectedTagNode = NodeType.asTag(tree.getLastSelectedPathComponent());

            boolean isStoryLanguageSupported = BDDUtil.isStoryLanguageSupported();
            var service = project.getService(JBehaveStoryService.class);

            //Doing 'for (var featureFile : selectedTagNode.getFeatureFiles()) {}' results in concurrent modification exception
            // because there are listeners in the background updating the tree model based on PSI modification
            WriteCommandAction.runWriteCommandAction(project, () -> {
                var bddFiles = selectedTagNode.getFeatureFiles().stream().map(FeatureFile::getFile).toList();
                for (var bddFile : bddFiles) {
                    PsiElement[] tagsToDelete = PsiTreeUtil.collectElements(PsiManager.getInstance(project).findFile(bddFile), element -> {
                        String tagName = TagNameUtil.determineTagOrMetaName(element);
                        if (tagName != null) { //either a Gherkin tag or a Meta Key
                            return tagName.equals(selectedTagNode.getDisplayName());
                        } else if (isStoryLanguageSupported) {
                            return service.isMetaTextForMetaKeyWithName(element, selectedTagNode.getDisplayName());
                        }
                        return false;
                    });
                    //Delete the tags/metas in a single command action, so that it is easier to redo them
                    for (var tag : tagsToDelete) tag.delete();

                    //Update the occurrence counts once processing the file has finished
                    tagOccurrencesRegistry.updateOccurrenceCounts(bddFile);
                }
            });
            tree.updateUI();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        var tree = (GherkinTagTree) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
        e.getPresentation().setEnabled(tree != null && isGherkinTag(tree.getLastSelectedPathComponent()));
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private boolean isUserSureToDeleteAllOccurrencesOfTag(Project project) {
        return ApplicationManager.getApplication().isUnitTestMode()
            || Messages.showYesNoDialog(project,
            GherkinBundle.message("gherkin.overview.toolwindow.delete.are.you.sure"), GherkinBundle.message("gherkin.overview.toolwindow.delete.tags"),
            Messages.getQuestionIcon()) == YES;
    }

    private static boolean isGherkinTag(Object node) {
        return node instanceof Tag;
    }
}
