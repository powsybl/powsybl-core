# Module loadflow-results-completion-parameters

The `loadflow-results-completion-parameters` module is used by the [loadflow-validation](../../tools/loadflow-validation.md)
tool and the [LoadFlowResultsCompletion](../../architecture/iidm/post-processor/loadflowResultsCompletion.md)
post-processor.

## Properties

| Property | Type | Required | Default value | Description |
| -------- | ---- | -------- | ------------- | ----------- |
| apply-reactance-correction | Boolean | no | false | If true, fix too small reactance values to epsilon-x |
| epsilon-x | Float | no | 0.1 | The minimal reactance value |

**apply-reactance-correction**: To solve numeric issues with very small reactance values, it's necessary to set the too
small values to a minimal value.

## Examples

### YAML
```yaml
loadflow-results-completion-parameters:
    apply-reactance-correction: true
    epsilon-x: 0.1
```

### XML
```xml
<loadflow-results-completion-parameters>
    <apply-reactance-correction>true</apply-reactance-correction>
    <epsilon-x>0.1</epsilon-x>
</loadflow-results-completion-parameters>
```

## References
See also:
[loadflow](../../tools/loadflow.md),
[loadflow-validation](../../tools/loadflow-validation.md)
[LoadFlowResultsCompletion](../../architecture/iidm/post-processor/loadflowResultsCompletion.md)
