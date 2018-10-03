# Module groovy-dsl-contingencies

The `groovy-dsl-contingencies` module is used by the `GroovyDslContingenciesProviderFactory`, which is a implementation of
the `ContingenciesProviderFactory` used in [security-analysis](../../tools/security-analysis.md) computations.

## Properties

| Property | Type | Required | Default value | Description |
| -------- | ---- | -------- | ------------- | ----------- |
| dsl-file | Path | true | - | The groovy script which describe the list of contingencies to simulate. |

## Examples

### YAML
```yaml
groovy-dsl-contingencies:
    dsl-file: /home/user/contingencies.groovy
```

### XML
```xml
<groovy-dsl-contingencies>
    <dsl-file>/home/user/contingencies.groovy</dsl-file>
</groovy-dsl-contingencies>
```

## References
See also:
[security-analysis](../../tools/security-analysis.md)
