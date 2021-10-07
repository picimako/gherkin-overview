## JBehave Story support

[JBehave](https://jbehave.org) for Java, besides Cucumber, is a well-known and widely used BDD framework. Beside support for Gherkin feature files,
it has its own file type and syntax called Story files.

### Preconditions

For these Story files it is the [JBehave Support](https://plugins.jetbrains.com/plugin/7268-jbehave-support) plugin that adds support,
thus having it installed is required if you want the Gherkin Overview plugin to recognize them.

Having JBehave Support installed is optional, thus it doesn't interfere with the rest of the plugin that is responsible
for handling Gherkin files.

## How metas are handled

Although standalone meta texts are recognized as valid metas by the JBehave Support plugin, they are not
recognized as such by JBehave itself. A valid meta is either a standalone key, or a key-value pair.

The conversion from Story metas to ones used by this plugin happens according to the table below:

| Meta in .story file | Meta in the plugin |
|---|---|
| `Meta: @suite` | suite |
| `Meta: @suite smoke` | suite:smoke |
| `Meta: @suite smoke regression` | suite:smoke regression |
| `Meta: smoke` | *Such meta is not considered valid.* |

Mapping to categories in the plugin settings can be done based on these values.

## Distinguishing multiple Story files with the same name

Since Story files don't have a unique keyword like the `Feature` one in Gherkin, when there is more than one Story file
with the same name within the same meta, they are distinguished based on their file paths.

## Collecting Story metas from the current project

In the plugin settings, collecting the mapped and unmapped metas from the project happens the same way as for Gherkin files.

In the tag collection table, unmapped Gherkin Tags and Story metas are not distinguished, since it is unlikely that both
JBehave and Cucumber are used in the same project. This way it also makes it simpler.
