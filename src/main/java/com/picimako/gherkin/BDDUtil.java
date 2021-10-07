/*
 *  Copyright 2021 Tamás Balog
 *
 *  Licensed under the Apache License, Version 2.0 \(the "License"\);
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.picimako.gherkin;

import static com.picimako.gherkin.GherkinUtil.isGherkinFile;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.psi.PsiFile;

/**
 * Utility for anything that is commonly BDD related.
 */
public final class BDDUtil {

    /**
     * Returns whether the argument file is one of the BDD file types: Gherkin or JBehave Story file.
     */
    public static boolean isABDDFile(PsiFile file) {
        return isGherkinFile(file) || file.getProject().getService(JBehaveStoryService.class).isJBehaveStoryFile(file);
    }

    /**
     * Returns whether the JBehave Support plugin is installed, enabled, and the .story file extension is
     * associated with a not unknown file type.
     * <p>
     * The reason it is checked this way, instead of querying the actual StoryLanguage is to prevent classloading
     * issues when referencing a class from the JBehave Support plugin when it is not installed, or installed but disabled.
     */
    public static boolean isStoryLanguageSupported() {
        PluginId jbehaveSupport = PluginId.getId("jbehave-support-plugin");
        return PluginManager.isPluginInstalled(jbehaveSupport)
            && PluginManager.getInstance().findEnabledPlugin(jbehaveSupport) != null
            && FileTypeRegistry.getInstance().getFileTypeByExtension("story") != UnknownFileType.INSTANCE;
    }

    private BDDUtil() {
        //Utility class
    }
}
