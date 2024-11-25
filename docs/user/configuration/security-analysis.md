# security-analysis

The `security-analysis` module is used to configure the execution of the [security-analysis](../itools/security-analysis.md) command.

## Optional property

**preprocessor**  
The `preprocessor` property is an optional property which requires that the `SecurityAnalysisPreprocessor` with specified name is used to preprocess inputs, based on the contingency file, before actually running the security analysis.

Such a preprocessor will have the possibility to programmatically transform the following objects before the security analysis is actually executed :
- The `Network`
- The `ContingenciesProvider`
- The `LimitViolationDetector`
- The `LimitViolationFilter`
- The `SecurityAnalysisParameters`
- The `SecurityAnalysisInterceptor`s

It enables, for example, to customize what should be considered a limit violation and what should not.

If absent, the default behavior of the tool is used: the contingency file is simply interpreted by the configured contingency provider.

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

