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

import static com.picimako.gherkin.GherkinUtil.isGherkinFile;
import static com.picimako.gherkin.toolwindow.TagNameUtil.metaNameFrom;
import static com.picimako.gherkin.toolwindow.TagNameUtil.tagNameFrom;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SlowOperations;
import com.intellij.util.SmartList;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;

import com.picimako.gherkin.GherkinUtil;
import com.picimako.gherkin.JBehaveStoryService;
import com.picimako.gherkin.toolwindow.nodetype.CategoriesHolder;
import com.picimako.gherkin.toolwindow.nodetype.Category;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import com.picimako.gherkin.toolwindow.nodetype.Tag;

/**
 * A base model class for the various Gherkin Tags tree model implementations.
 * <p>
 * It doesn't just hold the model data but also provides the logic for building it from scratch, and to update it based on
 * changes in {@link org.jetbrains.plugins.cucumber.psi.GherkinFile}s and {@link com.github.kumaraman21.intellijbehave.parser.StoryFile}s.
 * <p>
 * The logic takes into consideration where categories are stored (on project level or in content roots) and builds
 * and updates the underlying model data accordingly.
 * <p>
 * Content roots, categories, tags and Gherkin/Story files are sorted alphabetically by their names to make it easier to overview them.
 * <p>
 * There is a permanent category called {@code Other} whose purpose is to store all tags that are not explicitly mapped
 * to a custom category. In case of content root based grouping, each content root has its own Other category.
 * <p>
 * Tags and tag names are stored without their leading @ character.
 * <p>
 * Changes in Gherkin files are reflected immediately in the tree. For the update logic see {@link #updateModelForFile(PsiFile)}.
 *
 * @see GherkinTagTree
 * @see GherkinPsiChangeListener
 */
public abstract class GherkinTagTreeModel implements TreeModel, Disposable {

    private final Project project;
    private final TagCategoryRegistry registry;
    private final JBehaveStoryService storyService;
    protected ModelDataRoot data;

    protected GherkinTagTreeModel(Project project) {
        registry = TagCategoryRegistry.getInstance(project);
        this.project = project;
        storyService = project.getService(JBehaveStoryService.class);
    }

    /**
     * Using this, an already built model data can be reused. For example when the layout changes in the tool window.
     */
    protected GherkinTagTreeModel(ModelDataRoot data, Project project) {
        this(project);
        this.data = data;
    }

    @Override
    public Object getRoot() {
        return data;
    }

    @Override
    public void dispose() {
        data.dispose();
        data = null;
    }

    /**
     * Builds model data for storing the structure of the tree component in the Gherkin tags tool window.
     */
    public void buildModel() {
        if (ProjectUtil.guessProjectDir(project) != null) {
            if (data == null) {
                data = new ModelDataRoot(project);
            } else {
                //This is called when a layout switch happens in the tool window
                data.initData();
            }

            final List<PsiFile> gherkinFiles = new SmartList<>();
            final List<PsiFile> storyFiles = new SmartList<>();
            //NOTE: Handling the whole logic in one stream() call chain may not return and process all Gherkin files in the project, hence the separation
            //NOTE2: Reading the Gherkin and Story files in separate read actions is in place to ensure that all files are read consistently.
            SlowOperations.allowSlowOperations(() -> DumbService.getInstance(project).runReadActionInSmartMode(() -> gherkinFiles.addAll(GherkinUtil.collectGherkinFilesFromProject(project))));
            SlowOperations.allowSlowOperations(() -> DumbService.getInstance(project).runReadActionInSmartMode(() -> storyFiles.addAll(storyService.collectStoryFilesFromProject())));

            ProjectBDDTypeService service = project.getService(ProjectBDDTypeService.class);
            service.isProjectContainGherkinFile = !gherkinFiles.isEmpty();
            service.isProjectContainJBehaveStoryFile = !storyFiles.isEmpty();

            if (service.isProjectContainGherkinFile || service.isProjectContainJBehaveStoryFile) {
                TagOccurrencesRegistry.getInstance(project).init(gherkinFiles.size() + storyFiles.size());
            }

            persistGherkinTags(gherkinFiles);
            persistStoryMetas(storyFiles);

            data.updateDisplayName();
            data.sort();
        }
    }

    private void persistGherkinTags(List<PsiFile> gherkinFiles) {
        for (PsiFile file : gherkinFiles) {
            Collection<GherkinTag> gherkinTags = PsiTreeUtil.findChildrenOfType(file, GherkinTag.class);
            for (GherkinTag gherkinTag : gherkinTags) {
                addToContentRootAndCategory(tagNameFrom(gherkinTag), file);
            }
        }
    }

    private void persistStoryMetas(List<PsiFile> storyFiles) {
        for (PsiFile file : storyFiles) {
            for (var meta : storyService.collectMetasFromFile(file).entrySet()) {
                if (meta.getValue().isEmpty()) {
                    addToContentRootAndCategory(metaNameFrom(meta.getKey(), null), file);
                } else {
                    meta.getValue().forEach(metaText -> addToContentRootAndCategory(metaNameFrom(meta.getKey(), meta.getValue()), file));
                }
            }
        }
    }

    /**
     * Updates this model based on the tags and metas available in the argument Gherkin or Story file after it has changed.
     * <p>
     * It handles the cases of tag addition, removal and modification. This latter one means that if you e.g. deleted from or
     * added a character to a tag, it is manifested in the model as the removal of a tag's occurrence, and that another one
     * is added.
     * <p>
     * If at the end of updating this model, a tag has no Gherkin/Story file mapped to it, then the tag is removed as well from the model.
     * <p>
     * The same is applied to categories. If after updating the model, a category no longer has a tag mapped to it,
     * then the category is removed as well, except the catch-all {@code Other} category.
     * It is also applied to content roots with the difference that when it is only the Other category that remains in the content root with an
     * empty tag list, then the content root is removed as well.
     * <p>
     * If a tag is already mapped to the current file, than it only re-calculates the tags' occurrence count in the currently changed
     * Gherkin/Story file for each contained tag.
     * This happens when the tag has multiple occurrences in the file, and one occurrence is removed or added, but at least one remains.
     *
     * @param bddFile the Gherkin or Story file whose content has changed
     */
    public void updateModelForFile(PsiFile bddFile) {
        CategoriesHolder contentRoot = getContentRoot(bddFile);

        //Collect all tags that this file is bound to
        //Used Map instead of List, so that querying it in various ways is easier than with a List
        Map<String, Tag> tagsBddFileIsBoundTo = contentRoot.getCategories().stream()
            .flatMap(category -> category.getTags().stream())
            .filter(tag -> tag.contains(bddFile.getVirtualFile()))
            .collect(toMap(Tag::getDisplayName, Function.identity()));

        var service = TagOccurrencesRegistry.getInstance(project);

        //If the change is that the Gherkin or Story file has been removed
        if (!bddFile.getVirtualFile().isValid()) {
            tagsBddFileIsBoundTo.values().forEach(tag -> {
                tag.remove(bddFile.getVirtualFile());
                removeEmptyTagsAndCategories(tag, contentRoot);
            });
            service.remove(bddFile.getVirtualFile().getPath());
        } else {
            //Collect all Gherkin tag and/or Story meta names from the provided file
            List<String> tagNamesFromBDDFile = isGherkinFile(bddFile)
                ? GherkinUtil.collectGherkinTagsFromFile(bddFile)
                : storyService.collectMetasFromFileAsList(bddFile);

            //If a tag is present in the file but not in the tree, then add mapping
            for (String tagName : tagNamesFromBDDFile) {
                if (!tagsBddFileIsBoundTo.containsKey(tagName)) {
                    addToContentRootAndCategory(tagName, bddFile);
                }
            }

            //If a tag is not present in the file but present in the tree, then remove mapping
            //keySet() is used instead of entrySet() to have better readability of what's happening here
            for (String tagName : tagsBddFileIsBoundTo.keySet()) {
                if (!tagNamesFromBDDFile.contains(tagName)) {
                    Tag tag = tagsBddFileIsBoundTo.get(tagName);
                    tag.remove(bddFile.getVirtualFile());
                    removeEmptyTagsAndCategories(tag, contentRoot);
                }
            }

            //If a tag is already mapped, then re-calculate the tags' occurrence count in the currently changed Gherkin file for each contained tag
            tagNamesFromBDDFile.forEach(tagName -> 
                contentRoot.findTag(tagName).ifPresent(tag -> tag.getFeatureFiles().forEach(file -> service.updateOccurrenceCounts(file.getFile()))));

            //Update the display names of file with the same name as the changed file
            tagsBddFileIsBoundTo.values().forEach(tag -> tag.updateDisplayNames(bddFile.getVirtualFile()));
        }

        //In case there is any incorrect node left behind in the model, clean it up
        //This is useful when the same tag value or overlapping regex based tags are mapped to multiple different categories
        for (var category : contentRoot.getCategories()) {
            category.getTags().removeIf(tag -> !tag.hasFeatureFile());
        }
        contentRoot.getCategories().removeIf(Category::isNotOtherAndDoesntHaveAnyTag);
        if (data.isContentRootDataInitialized()) {
            data.getContentRoots().removeIf(projectModule -> projectModule.getCategories().size() == 1 && !projectModule.getOther().hasTag());
        }
    }

    private void removeEmptyTagsAndCategories(Tag tag, CategoriesHolder contentRoot) {
        Category category = contentRoot.findCategoryOrOther(registry.categoryOf(tag.getDisplayName()));
        //If there is no more Gherkin file existing under a Tag, then remove the Tag as well
        if (!tag.hasFeatureFile()) {
            category.getTags().remove(tag);
        }
        //If there is no more Tag existing under a Category, then remove the Category as well
        if (category.isNotOtherAndDoesntHaveAnyTag()) {
            contentRoot.getCategories().remove(category);
        }
        //If there is no more Categories and tags existing under a Content Root, then remove the Content Root as well
        if (data.isContentRootDataInitialized() && contentRoot.getCategories().size() == 1 && !contentRoot.getOther().hasTag()) {
            data.getContentRoots().remove(contentRoot); //here contentRoot is a ContentRoot object, so removal is fine
        }
    }

    /**
     * If there is an actual category mapped to the argument tag name (in the registry), then the tag with the Gherkin file is added to
     * that category, otherwise it is added to the default "Other" category.
     * <p>
     * Furthermore, if the category associated with the tag name is already added to this model, the tag is added to
     * that corresponding Category object, otherwise a new Category object is created and added.
     *
     * @param tagName the tag name to add to a category
     * @param file    the Gherkin file that will be added to the provided tag
     */
    private void addToContentRootAndCategory(String tagName, PsiFile file) {
        String categoryName = registry.categoryOf(tagName);
        CategoriesHolder contentRoot = getContentRoot(file);

        if (categoryName != null) {
            contentRoot.findCategory(categoryName)
                .ifPresentOrElse(
                    category -> category.addTagOrFileToTag(tagName, file.getVirtualFile()),
                    () -> contentRoot.addCategory(new Category(categoryName, project).add(new Tag(tagName, file.getVirtualFile(), project))));
        } else {
            contentRoot.getOther().addTagOrFileToTag(tagName, file.getVirtualFile());
        }
    }

    protected abstract CategoriesHolder getContentRoot(PsiFile file);

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
    }
}
