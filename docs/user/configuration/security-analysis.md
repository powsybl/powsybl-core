---
layout: default
---

# security-analysis

The `security-analysis` module is used to configure the execution of the [security-analysis](../itools/security-analysis.md) command.

## Optional property

**preprocessor**  
The `preprocessor` property is an optional property which requires that the `SecurityAnalysisPreprocessor` with specified name is used to preprocess inputs, based on the contingencies file, before actually running the security analysis.

If absent, the default behavior of the tool is used: the contingencies file is simply interpreted by the configured contingencies provider.

## Examples

**YAML configuration:**  
```yaml
security-analysis:
    preprocessor: my_custom_preprocessor_name
```

**XML configuration:**  
```xml
<security-analysis>
    <preprocessor>my_custom_preprocessor_name</preprocessor>
</security-analysis>
```

