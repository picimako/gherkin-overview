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
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.intellij.openapi.options.ConfigurationException;

import com.picimako.gherkin.MediumBasePlatformTestCase;
import com.picimako.gherkin.toolwindow.TagCategoryRegistry;

/**
 * Unit test for {@link GherkinOverviewProjectConfigurable}.
 * <p>
 * TODO: fix tests due to lack of application level service cleanup. Test methods in this class executed individually
 *  pass.
 */
public class GherkinOverviewProjectConfigurableTest extends MediumBasePlatformTestCase {

    //createComponent

    public void testCreateComponent() {
        var configurable = new GherkinOverviewProjectConfigurable(getProject());

        configurable.createComponent();

        var component = configurable.getComponent();
        assertThat(component.isUseProjectLevelMappings()).isFalse();

        assertThat(component.getApplicationLevelMappings()).contains(
            new CategoryAndTags("Device", "desktop,tablet,mobile,iphone,ipad,android,winphone,tab,device"),
            new CategoryAndTags("Browser", "ie,ie10,ie11,edge,internetexplorer,edge,firefox,ff,chrome,googlechrome,opera,safari,browser")
        ).doesNotContain(new CategoryAndTags("Breakpoint", "small"));

        assertThat(component.getProjectLevelMappings()).isEmpty();
    }

    //isModified

    public void testNotModified() {
        var configurable = new GherkinOverviewProjectConfigurable(getProject());
        configurable.createComponent();

        assertThat(configurable.isModified()).isFalse();
    }

    public void testModifiedUseProjectLevelMappings() {
        var configurable = new GherkinOverviewProjectConfigurable(getProject());
        configurable.createComponent();
        configurable.getComponent().setUseProjectLevelMappings(true);

        assertThat(configurable.isModified()).isTrue();
    }

    public void testModifiedApplicationLevelMappings() {
        var configurable = new GherkinOverviewProjectConfigurable(getProject());
        configurable.createComponent();
        configurable.getComponent().setApplicationLevelMappings(List.of(new CategoryAndTags("Breakpoint", "small,medium")));

        assertThat(configurable.isModified()).isTrue();
    }

    public void testModifiedProjectLevelMappings() {
        var configurable = new GherkinOverviewProjectConfigurable(getProject());
        configurable.createComponent();

        configurable.getComponent().setProjectLevelMappings(List.of(new CategoryAndTags("Breakpoint", "small,medium")));
        assertThat(configurable.isModified()).isTrue();
    }

    //apply

    public void testAppliesSettingsWithoutProjectLevelMappings() throws ConfigurationException {
        var configurable = new GherkinOverviewProjectConfigurable(getProject());
        configurable.createComponent();
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
        var configurable = new GherkinOverviewProjectConfigurable(getProject());
        configurable.createComponent();
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

    //reset

    public void testResetsSettings() {
        var configurable = new GherkinOverviewProjectConfigurable(getProject());
        configurable.createComponent();
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
}
