//Copyright 2026 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static com.picimako.gherkin.toolwindow.nodetype.NodeType.asCategory;
import static com.picimako.gherkin.toolwindow.nodetype.NodeType.asContentRoot;
import static com.picimako.gherkin.toolwindow.nodetype.NodeType.asTag;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.picimako.gherkin.toolwindow.nodetype.CategoriesHolder;
import com.picimako.gherkin.toolwindow.nodetype.Category;
import com.picimako.gherkin.toolwindow.nodetype.ContentRoot;
import com.picimako.gherkin.toolwindow.nodetype.FeatureFile;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import com.picimako.gherkin.toolwindow.nodetype.Tag;

/**
 * Model object for displaying the structure of the underlying model data grouped by content roots.
 * <p>
 * This is a five-level model consisting of the following levels:
 * <pre>{@code
 * - Gherkin Tags                   <-- This is the root node. Permanent, can't be removed.
 *      - Content root              <-- A content root in the current IDE project.
 *          - Category              <-- The category a Gherkin tag is associated to for grouping.
 *              - Tag               <-- The Gherkin tag.
 *                  - Gherkin file  <-- One or more 'FeatureFile's.
 *      - Content root
 *          - Category
 *              - Tag
 *                  - Gherkin file
 * }</pre>
 */
final class ContentRootBasedGherkinTagTreeModel extends GherkinTagTreeModel {

    ContentRootBasedGherkinTagTreeModel(Project project) {
        super(project);
    }

    ContentRootBasedGherkinTagTreeModel(ModelDataRoot data, Project project) {
        super(data, project);
    }

    @Override
    protected CategoriesHolder getContentRoot(PsiFile file) {
        return data.findContentRootOrRootless(file);
    }

    // The methods below are responsible for building the actual tree model from the backing model data.

    @Override
    public Object getChild(Object parent, int index) {
        return switch (parent) {
            case ModelDataRoot __ -> data.getContentRootsByLayout().get(index);
            case ContentRoot __ -> asContentRoot(parent).getCategories().get(index);
            case Category __ -> asCategory(parent).getTags().get(index);
            case Tag __ -> asTag(parent).getFeatureFiles().get(index);
            case null, default -> null;
        };
    }

    @Override
    public int getChildCount(Object parent) {
        return switch (parent) {
            case ModelDataRoot __ -> data.getContentRootsByLayout().size();
            case ContentRoot __ -> asContentRoot(parent).getCategories().size();
            case Category __ -> asCategory(parent).getTags().size();
            case Tag __ -> asTag(parent).getFeatureFiles().size();
            case null, default -> 0;
        };
    }

    @Override
    public boolean isLeaf(Object node) {
        return switch (node) {
            case ContentRoot __ -> asContentRoot(node).getCategories().isEmpty();
            case Category __ -> !asCategory(node).hasTag();
            case Tag __ -> !asTag(node).hasFeatureFile();
            case FeatureFile __ -> true;
            case null, default -> data.getContentRootsByLayout().isEmpty();
        };
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        int indexOfChild = 0;
        if (parent != null && child != null) {
            switch (parent) {
                case ModelDataRoot __ -> indexOfChild = data.getContentRootsByLayout().indexOf(child);
                case ContentRoot __ -> indexOfChild = asContentRoot(parent).getCategories().indexOf(child);
                case Category __ -> indexOfChild = asCategory(parent).getTags().indexOf(child);
                case Tag __ -> indexOfChild = asTag(parent).getFeatureFiles().indexOf(child);
                default -> {
                }
            }
        } else {
            indexOfChild = -1;
        }
        return indexOfChild;
    }
}
