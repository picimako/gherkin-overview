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

package com.picimako.gherkin.settings;

import static com.picimako.gherkin.SoftAsserts.assertSoftly;
import static com.picimako.gherkin.ToolWindowTestSupport.getToolWindowModel;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import com.intellij.openapi.options.ConfigurationException;

import com.picimako.gherkin.MediumBasePlatformTestCase;
import com.picimako.gherkin.ToolWindowTestSupport;
import com.picimako.gherkin.toolwindow.GherkinTagOverviewPanel;
import com.picimako.gherkin.toolwindow.GherkinTagsToolWindowSettings;
import com.picimako.gherkin.toolwindow.StatisticsType;
import com.picimako.gherkin.toolwindow.TagCategoryRegistry;
import com.picimako.gherkin.toolwindow.nodetype.Category;
import com.picimako.gherkin.toolwindow.nodetype.ModelDataRoot;

/**
 * Unit test for {@link GherkinOverviewProjectConfigurable}.
 * <p>
 * TODO: fix tests due to lack of application level service cleanup. Test methods executed individually pass.
 */
public class GherkinOverviewProjectConfigurableTest extends MediumBasePlatformTestCase {

    private GherkinOverviewProjectConfigurable configurable;

    @Override
    protected String getTestDataPath() {
        return "testdata/features";
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        configurable = new GherkinOverviewProjectConfigurable(getProject());
        configurable.createComponent();
        ToolWindowTestSupport.registerToolWindow(new GherkinTagOverviewPanel(getProject()), getProject());
    }

    @Override
    public void tearDown() throws Exception {
        configurable.disposeUIResources();
        super.tearDown();
    }

    //createComponent

    public void testCreateComponent() {
        var component = configurable.getComponent();
        assertThat(component.isUseProjectLevelMappings()).isFalse();

        assertThat(component.getApplicationLevelMappings())
            .contains(
                new CategoryAndTags("Device", "desktop,tablet,mobile,iphone,ipad,android,winphone,tab,device"),
                new CategoryAndTags("Browser", "ie,ie10,ie11,edge,internetexplorer,edge,firefox,ff,chrome,googlechrome,opera,safari,browser")
            ).doesNotContain(new CategoryAndTags("Breakpoint", "small"));

        assertThat(component.getProjectLevelMappings()).isEmpty();
    }

    //isModified

    public void testIsNotModifiedByDefault() {
        assertThat(configurable.isModified()).isFalse();
    }

    public void testModifiedWhenUsingProjectLevelMappingsIsEnabled() {
        configurable.getComponent().setUseProjectLevelMappings(true);

        assertThat(configurable.isModified()).isTrue();
    }

    public void testModifiedWhenUsingApplicationLevelMappings() {
        configurable.getComponent().setApplicationLevelMappings(List.of(new CategoryAndTags("Breakpoint", "small,medium")));

        assertThat(configurable.isModified()).isTrue();
    }

    public void testModifiedWhenUsingProjectLevelMappings() {
        configurable.getComponent().setProjectLevelMappings(List.of(new CategoryAndTags("Breakpoint", "small,medium")));
        assertThat(configurable.isModified()).isTrue();
    }

    //apply

    public void testAppliesSettingsWithoutProjectLevelMappings() throws ConfigurationException {
        CategoryAndTags breakpoint = new CategoryAndTags("Breakpoint", "small,medium");
        CategoryAndTags media = new CategoryAndTags("Media", "image");
        configurable.getComponent().setApplicationLevelMappings(List.of(breakpoint));
        configurable.getComponent().setProjectLevelMappings(List.of(media));

        configurable.apply();

        var appSettings = GherkinOverviewApplicationState.getInstance();
        var projectSettings = GherkinOverviewProjectState.getInstance(getProject());
        var registry = TagCategoryRegistry.getInstance(getProject());

        assertSoftly(
            softly -> softly.assertThat(appSettings.mappings).containsExactly(breakpoint),
            softly -> softly.assertThat(projectSettings.useProjectLevelMappings).isFalse(),
            softly -> softly.assertThat(projectSettings.mappings).containsExactly(media),
            softly -> softly.assertThat(registry.categoryOf("small")).isEqualTo("Breakpoint"),
            softly -> softly.assertThat(registry.categoryOf("image")).isNull()
        );
    }

    public void testAppliesSettingsWithProjectLevelMappings() throws ConfigurationException {
        ToolWindowTestSupport.registerToolWindow(new GherkinTagOverviewPanel(getProject()), getProject());
        CategoryAndTags breakpoint = new CategoryAndTags("Breakpoint", "small,medium");
        CategoryAndTags media = new CategoryAndTags("Media", "image");
        configurable.getComponent().setApplicationLevelMappings(List.of(breakpoint));
        configurable.getComponent().setProjectLevelMappings(List.of(media));
        configurable.getComponent().setUseProjectLevelMappings(true);

        configurable.apply();

        var appSettings = GherkinOverviewApplicationState.getInstance();
        var projectSettings = GherkinOverviewProjectState.getInstance(getProject());
        var registry = TagCategoryRegistry.getInstance(getProject());

        assertSoftly(
            softly -> softly.assertThat(appSettings.mappings).containsExactly(breakpoint),
            softly -> softly.assertThat(projectSettings.useProjectLevelMappings).isTrue(),
            softly -> softly.assertThat(projectSettings.mappings).containsExactly(media),
            softly -> softly.assertThat(registry.categoryOf("small")).isEqualTo("Breakpoint"),
            softly -> softly.assertThat(registry.categoryOf("image")).isEqualTo("Media")
        );
    }

    public void testRebuildsModelIfAppLevelMappingsChanged() throws ConfigurationException {
        myFixture.configureByFile("the_gherkin.feature");
        ToolWindowTestSupport.registerToolWindow(new GherkinTagOverviewPanel(getProject()), getProject());
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.SIMPLIFIED;
        configurable.getComponent().setApplicationLevelMappings(List.of(new CategoryAndTags("Web Browser", "chrome,edge")));

        validateCategories(getToolWindowModel(getProject()), "Browser", "Web Browser");

        configurable.apply();

        validateCategories(getToolWindowModel(getProject()), "Web Browser", "Browser");
    }

    public void testRebuildsModelIfProjectLevelMappingsChanged() throws ConfigurationException {
        myFixture.configureByFile("the_gherkin.feature");
        ToolWindowTestSupport.registerToolWindow(new GherkinTagOverviewPanel(getProject()), getProject());
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.SIMPLIFIED;
        configurable.getComponent().setUseProjectLevelMappings(true);
        configurable.getComponent().setProjectLevelMappings(List.of(new CategoryAndTags("Web Browser", "chrome,edge")));

        validateCategories(getToolWindowModel(getProject()), "Browser", "Web Browser");

        configurable.apply();

        validateCategories(getToolWindowModel(getProject()), "Web Browser", "Browser");
    }

    public void testDoesntRebuildModelIfNoMappingHasChanged() throws ConfigurationException {
        myFixture.configureByFile("the_gherkin.feature");
        ToolWindowTestSupport.registerToolWindow(new GherkinTagOverviewPanel(getProject()), getProject());
        GherkinTagsToolWindowSettings.getInstance(getProject()).statisticsType = StatisticsType.SIMPLIFIED;

        validateCategories(getToolWindowModel(getProject()), "Browser", "Web Browser");

        configurable.apply();

        validateCategories(getToolWindowModel(getProject()), "Browser", "Web Browser");
    }

    //reset

    public void testResetsSettings() {
        configurable.getComponent().setUseProjectLevelMappings(true);
        CategoryAndTags breakpoint = new CategoryAndTags("Breakpoint", "small,medium");
        configurable.getComponent().setApplicationLevelMappings(List.of(breakpoint));
        configurable.getComponent().setProjectLevelMappings(List.of(breakpoint));

        configurable.reset();

        assertSoftly(
            softly -> softly.assertThat(configurable.getComponent().isUseProjectLevelMappings()).isFalse(),
            softly -> softly.assertThat(configurable.getComponent().getApplicationLevelMappings()).doesNotContain(breakpoint),
            softly -> softly.assertThat(configurable.getComponent().getProjectLevelMappings()).isEmpty()
        );
    }

    //Helper methods

    private void validateCategories(ModelDataRoot model, String nonEmptyCategoryName, String emptyCategoryName) {
        Optional<Category> nonEmptyCategory = model.findCategory(nonEmptyCategoryName);
        assertThat(nonEmptyCategory).isPresent();
        assertThat(nonEmptyCategory.get().getTags()).hasSize(2);
        assertThat(model.findCategory(emptyCategoryName)).isEmpty();
    }
}
