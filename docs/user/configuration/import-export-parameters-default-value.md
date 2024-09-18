# import-export-parameters-default-value
The `import-export-parameters-default-value` module is an optional module used by the `com.powsybl.iidm.import_.Importers` class to initialize the parameters passed to configure the importer. This module supports 3 different types of properties:
- Boolean
- String
- List of Strings

As the parameters are different from an importer to another, it is impossible to give an exhaustive list of supported
properties. Please refer to the documentation of each [supported format](../../grid_exchange_formats/index.md) to know their specific configuration.

## Examples

**YAML configuration:**
```yaml
import-export-parameters-default-value:
    iidm.import.xml.throw-exception-if-extension-not-found: true
```

**XML configuration:**
```xml
<import-export-parameters-default-value>
    <iidm.import.xml.throw-exception-if-extension-not-found>true</iidm.import.xml.throw-exception-if-extension-not-found>
</import-export-parameters-default-value>
```
