# import-export-parameters-default-value
The `import-export-parameters-default-value` module is an optional module used to configure the network importers and exporters.

The parameters are different from a format importer/exporter to another, please refer to the documentation of
each [supported format](../../grid_exchange_formats/index.md) to learn more about their specific configuration.

## Examples

In this example we configure:
- the IIDM importer to throw an exception on trying to import an unknown or not deserializable extension
- the IIDM exporter to export to IIDM in its version 1.12

**YAML configuration:**
```yaml
import-export-parameters-default-value:
    iidm.import.xml.throw-exception-if-extension-not-found: true
    iidm.export.xml.version: "1.12"
```

**XML configuration:**
```xml
<import-export-parameters-default-value>
    <iidm.import.xml.throw-exception-if-extension-not-found>true</iidm.import.xml.throw-exception-if-extension-not-found>
    <iidm.export.xml.version>1.12</iidm.export.xml.version>
</import-export-parameters-default-value>
```
