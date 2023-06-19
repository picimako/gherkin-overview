//Copyright 2023 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow.nodetype;

import static com.picimako.gherkin.toolwindow.LayoutType.GROUP_BY_MODULES;
import static com.picimako.gherkin.toolwindow.nodetype.Category.OTHER_CATEGORY_NAME;
import static com.picimako.gherkin.toolwindow.nodetype.ContentRoot.Type.CONTENT_ROOT;
import static com.picimako.gherkin.toolwindow.nodetype.ContentRoot.Type.MODULE;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.SmartList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.picimako.gherkin.resources.GherkinBundle;
import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;
import com.picimako.gherkin.toolwindow.LayoutType;
import com.picimako.gherkin.toolwindow.ProjectBDDTypeService;

/**
 * Represents the root element of the tree in the Gherkin Tags tool window.
 */
@Getter
public class ModelDataRoot extends AbstractNodeType implements CategoriesHolder {

    private static final String ROOTLESS_CONTENT_ROOT_NAME = "Rootless";

    /**
     * Stores model data for the overall project.
     * <p>
     * This separation is in place, so that categories from each module don't have to be merged each time they
     * are queried, which can happen a lot when interacting with the tool window.
     */
    private List<Category> categories;

    /**
     * Stores model data grouped by various content roots. These roots may be Modules, Content Roots, external sources
     * like .jar files, etc.
     */
    private List<ContentRoot> contentRoots;

    public ModelDataRoot(Project project) {
        super(GherkinBundle.toolWindow("root.name.tags"), project);
        initData();
    }

    /**
     * Updates the display name of the tree's root element to reflect the contents of the project in terms of
     * the types of BDD files it contains.
     */
    public void updateDisplayName() {
        ProjectBDDTypeService service = project.getService(ProjectBDDTypeService.class);
        if (service.hasOnlyJBehaveStoryFiles()) {
            displayName = GherkinBundle.toolWindow("root.name.metas");
        } else if (service.hasBothGherkinAndStoryFiles()) {
            displayName = GherkinBundle.toolWindow("root.name.tags.and.metas");
        } else {
            displayName = GherkinBundle.toolWindow("root.name.tags");
        }
    }

    /**
     * Initializes the proper model data based on the currently selected layout in the tool window.
     * <p>
     * The model data is initialized only when it hasn't been initialized.
     */
    public void initData() {
        if (GherkinTagsToolWindowSettings.getInstance(project).layout == LayoutType.NO_GROUPING) {
            if (!isProjectDataInitialized()) {
                categories = new SmartList<>(Category.createOther(project));
            }
        } else if (!isContentRootDataInitialized()) {
            contentRoots = new SmartList<>();
        }
    }

    public boolean isProjectDataInitialized() {
        return categories != null;
    }

    public boolean isContentRootDataInitialized() {
        return contentRoots != null;
    }

    /**
     * Adds the argument content root to this node.
     */
    public ModelDataRoot add(ContentRoot contentRoot) {
        contentRoots.add(contentRoot);
        return this;
    }

    /**
     * Returns the list of content roots filtered by the content root type corresponding to the current layout selected
     * in the tool window.
     */
    public List<ContentRoot> getContentRootsByLayout() {
        return contentRoots.stream()
            .filter(contentRoot -> GherkinTagsToolWindowSettings.getInstance(project).layout == GROUP_BY_MODULES
                ? contentRoot.isModule()
                : contentRoot.isContentRoot())
            .collect(toList());
    }

    /**
     * Finds the {@link ContentRoot} the argument file is contained by.
     * <p>
     * If the file is not linked to any content root yet, then based on whether it actually belongs to a project content root,
     * it is added to a new {@code ContentRoot}, or to a catch-all content root called {@code Rootless}.
     * <p>
     * If the provided file is not valid anymore, it means it has just been deleted, thus the logic is slightly
     * different to locate the ContentRoot it was linked to.
     *
     * @param bddFile the file to find the content root of
     * @return the content root the file is/was linked to, or the catch-all content root
     */
    @Nullable
    public ContentRoot findContentRootOrRootless(PsiFile bddFile) {
        Module contentRootForFile = ModuleUtilCore.findModuleForFile(bddFile);
        if (bddFile.getVirtualFile().isValid()) {
            return contentRootForFile == null
                ? getContentRoot(getRootless(), ROOTLESS_CONTENT_ROOT_NAME)  //if file doesn't belong to any content root
                : getContentRoot(getContentRoot(contentRootForFile.getName()), contentRootForFile.getName()); //if has content root added with name
        }

        //If Gherkin or Story file is not valid, thus has just been deleted
        for (ContentRoot contentRoot : contentRoots) {
            if (contentRoot.hasFileMapped(bddFile.getVirtualFile())) {
                return contentRoot;
            }
        }

        return null; //This should never happen given the fact the content root called Rootless should exist
    }

    private Optional<ContentRoot> getRootless() {
        return getContentRoot(ROOTLESS_CONTENT_ROOT_NAME);
    }

    /**
     * Returns the content root for the provided name, or empty Optional if none found.
     *
     * @param moduleName the module name to look for
     */
    @VisibleForTesting
    public Optional<ContentRoot> getContentRoot(String moduleName) {
        return contentRoots.stream()
            .filter(module -> module.hasName(moduleName))
            .findFirst();
    }

    private ContentRoot getContentRoot(Optional<ContentRoot> root, String moduleName) {
        return root.orElseGet(() -> {
            ContentRoot contentRoot = new ContentRoot(
                moduleName,
                GherkinTagsToolWindowSettings.getInstance(project).layout == GROUP_BY_MODULES ? MODULE : CONTENT_ROOT,
                project);
            add(contentRoot);
            return contentRoot;
        });
    }

    /**
     * Returns the module type content roots.
     */
    public List<ContentRoot> getModules() {
        return contentRoots.stream().filter(ContentRoot::isModule).collect(toList());
    }

    //categories

    /**
     * get() is called on the Optional because the category Other should be available.
     */
    @Override
    public @NotNull Category getOther() {
        return findCategory(OTHER_CATEGORY_NAME).get();
    }

    //sort

    @Override
    public void sort() {
        if (isProjectDataInitialized()) {
            categories.forEach(Category::sort);
            sortIfContainsMultiple(categories);
        }
        if (isContentRootDataInitialized()) {
            contentRoots.forEach(ContentRoot::sort);
            sortIfContainsMultiple(contentRoots);
        }
    }

    //toString

    /**
     * This doesn't show the number of all Gherkin files in the project, only the number of those containing tags.
     */
    @Override
    public String toString() {
        return getToString(
            () -> GherkinBundle.toolWindow("statistics.root.simplified", displayName, tagCount(), bddFileCount()),
            () -> GherkinBundle.toolWindow("statistics.root.detailed", displayName, tagCount(), bddFileCount()));
    }

    /**
     * Counts the distinct number of tags stored in this model.
     */
    private long tagCount() {
        return (isProjectDataInitialized()
            ? this.categories.stream()
            : contentRoots.stream().flatMap(contentRoot -> contentRoot.getCategories().stream()))
            .flatMap(category -> category.getTags().stream())
            .distinct()
            .count();
    }

    /**
     * Counts the distinct number of Gherkin files stored in this model.
     */
    private long bddFileCount() {
        return (isProjectDataInitialized()
            ? categories.stream()
            : contentRoots.stream().flatMap(contentRoot -> contentRoot.getCategories().stream()))
            .flatMap(category -> category.getTags().stream())
            .flatMap(tag -> tag.getFeatureFiles().stream())
            .distinct()
            .count();
    }

    @Override
    public void dispose() {
        if (categories != null) {
            categories.forEach(Category::dispose);
            categories.clear();
        }
        if (contentRoots != null) {
            contentRoots.forEach(ContentRoot::dispose);
            contentRoots.clear();
        }
    }
}
