# Configuration

## Implementation
If you have several implementation in your classpath, you need to choose which implementation to use in your configuration file with the `default-impl-name` property.
Each implementation is identified by its name, that may be unique in the classpath:
- use "DynaWaltz" to use powsybl-dynawo implementation

**YAML configuration:**
```yaml
dynamic-security-analysis:
  default-impl-name: Mock
```

**XML configuration:**
```xml
<dynamic-security-analysis>
  <default-impl-name>Mock</default-impl-name>
</dynamic-security-analysis>
```
