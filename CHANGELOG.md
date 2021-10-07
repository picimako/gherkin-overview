# Changelog

### 0.2.0

#### NEW
- Added support for [JBehave](https://jbehave.org) Story files:
  - .story files are now also recognized in projects, given that the [JBehave Support](https://plugins.jetbrains.com/plugin/16716-gherkin-overview) plugin is installed, since that
  plugin provides the language support for JBehave Story files.
  - Renamed the `Gherkin Tags` tool window to simply `Tags` to reflect that it is not just Gherkin files that the tool
  window can display.
  - In the tool window, the root element's name reflects whether Gherkin and/or JBehave Story files are available in the
  project.

#### ENHANCEMENT
- The tree model in the Tags tool window is rebuilt when either the application or the project-level mapping
has been changed in Settings. This aims to provide a smoother user experience by not having to restart the IDE or
turn to other workarounds to reload the tool window, so that it displays data with the up-to-date mappings.

### 0.1.0

Initial release
