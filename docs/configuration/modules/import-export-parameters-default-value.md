# Module import-export-parameters-default-value

The `import-export-parameters-default-value` module is an optional module used by the `Importers` class to initialize the
parameters passed to configure the `Importer`. This module support 3 different types of properties:
- Boolean
- String
- List of String

While the parameters are different from an `Importer` to another, it is impossible to give an exhaustive list of supported
properties. Please refers to the documentation of each [importer](../../architecture/iidm/importer/README.md).

## Examples

### YAML
```yaml
import-export-parameters-default-value:
    throwExceptionIfExtensionNotFound: true
```

### XML
```xml
<import-export-parameters-default-value>
    <throwExceptionIfExtensionNotFound>true</throwExceptionIfExtensionNotFound>
</import-export-parameters-default-value>
```

## References
See also:
[Importers](../../architecture/iidm/importer/README.md)
