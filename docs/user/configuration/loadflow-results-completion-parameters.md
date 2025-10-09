# loadflow-results-completion-parameters
The `loadflow-results-completion-parameters` module is used by the [loadflow-validation](../itools/loadflow-validation.md)
command and the [LoadFlowResultsCompletion](../../grid_features/import_post_processor.md) post processor.

## Optional properties

**apply-reactance-correction**  
The `apply-reactance-correction` property is an optional property that defines whether the too small reactance values have to be fixed to `epsilon-x` value or not. To solve numeric issues with very small reactance values, it's necessary to set the too small values to a minimal value. The default value of this property is `false`.

**epsilon-x**  
The `epsilon-x` property is an optional property that defines the reactance value used for fixing. The default value of this property is `0.1`.
 
## Examples

**YAML configuration:**
```yaml
loadflow-results-completion-parameters:
    apply-reactance-correction: true
    epsilon-x: 0.1
```

**XML configuration:**
```xml
<loadflow-results-completion-parameters>
    <apply-reactance-correction>true</apply-reactance-correction>
    <epsilon-x>0.1</epsilon-x>
</loadflow-results-completion-parameters>
```
