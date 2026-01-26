# iTools plugins-info

The `plugins-info` command prints for each kind of plugin, the currently available implementations Ids. The available
kind of plugins are:
- [exporter](../iidm/exporter/index.md)
- [import-post-processor](../iidm/importer/post-processor/index.md)
- [importer](../iidm/importer/index.md)
- [loadflow-validation computation](../loadflow/validation.md)

## Usage
```shell
$> itools plugins-info
Plugins:
+---------------------------------+-----------------------------------------------------+
| Plugin type name                | Available plugin IDs                                |
+---------------------------------+-----------------------------------------------------+
| exporter                        | AMPL, XIIDM                                         |
| import-post-processor           | groovyScript, javaScript, loadflowResultsCompletion |
| importer                        | CGMES, UCTE, XIIDM                                   |
| loadflow-validation computation | loadflow, loadflowResultsCompletion                 |
+---------------------------------+-----------------------------------------------------+
```

## Maven configuration
To use the `plugins-info` command, add the following dependencies to the `pom.xml` file:
```xml
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-tools</artifactId>
    <version>${powsybl.version}</version>
</dependency>
```
