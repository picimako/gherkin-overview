//Copyright 2024 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.gherkin.toolwindow;

import static com.picimako.gherkin.toolwindow.nodetype.NodeType.asCategory;
import static com.picimako.gherkin.toolwindow.nodetype.NodeType.asTag;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.picimako.gherkin.toolwindow.nodetype.CategoriesHolder;
import com.picimako.gherkin.toolwindow.nodetype.Category;
import com.picimako.gherkin.toolwindow.nodetype.FeatureFile;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;
import com.picimako.gherkin.toolwindow.nodetype.Tag;

/**
 * Model object for displaying the structure of the underlying model data without grouping them by any type of
 * content root.
 * <p>
 * This is a four-level model consisting of the following levels:
 * <pre>
 * - Gherkin Tags               <-- This is the root node. It is permanent and cannot be removed.
 *      - Category              <-- The category a Gherkin tag is associated to for grouping.
 *          - Tag               <-- The Gherkin tag.
 *              - Gherkin file  <-- One or more {@link FeatureFile}s.
 * </pre>
 */
public final class ProjectSpecificGherkinTagTreeModel extends GherkinTagTreeModel {

    public ProjectSpecificGherkinTagTreeModel(Project project) {
        super(project);
    }

    public ProjectSpecificGherkinTagTreeModel(ModelDataRoot data, Project project) {
        super(data, project);
    }

    @Override
    protected CategoriesHolder getContentRoot(PsiFile file) {
        return data;
    }

    // The methods below are responsible for building the actual tree model from the backing model data.

    @Override
    public Object getChild(Object parent, int index) {
        Object child = null;
        if (parent instanceof ModelDataRoot) {
            child = data.getCategories().get(index);
        } else if (parent instanceof Category) {
            child = asCategory(parent).getTags().get(index);
        } else if (parent instanceof Tag) {
            child = asTag(parent).getFeatureFiles().get(index);
        }
        return child;
    }

    @Override
    public int getChildCount(Object parent) {
        int count = 0;
        if (parent instanceof ModelDataRoot) {
            count = data.getCategories().size();
        } else if (parent instanceof Category) {
            count = asCategory(parent).getTags().size();
        } else if (parent instanceof Tag) {
            count = asTag(parent).getFeatureFiles().size();
        }
        return count;
    }

    @Override
    public boolean isLeaf(Object node) {
        boolean isLeaf = data.getCategories().isEmpty();
        if (node instanceof Category) {
            isLeaf = !asCategory(node).hasTag();
        } else if (node instanceof Tag) {
            isLeaf = !asTag(node).hasFeatureFile();
        } else if (node instanceof FeatureFile) {
            isLeaf = true;
        }
        return isLeaf;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        int indexOfChild = 0;
        if (parent != null && child != null) {
            if (parent instanceof ModelDataRoot) {
                indexOfChild = data.getCategories().indexOf(child);
            } else if (parent instanceof Category) {
                indexOfChild = asCategory(parent).getTags().indexOf(child);
            } else if (parent instanceof Tag) {
                indexOfChild = asTag(parent).getFeatureFiles().indexOf(child);
            }
        } else {
            indexOfChild = -1;
        }
        return indexOfChild;
    }
}
