# Module external-security-analysis-config

The `external-security-analysis-config` module is used in the `ExternalSecurityAnalysis` class, that  implements the `SecurityAnalysis` interface.
`ExternalSecurityAnalysis` submits the execution of a `security-analysis` command to the ComputationManager.


## Properties

| Property | Type | Required | Default value | Description |
| -------- | ---- | -------- | ------------- | ----------- |
| debug | boolean | no | false |  output debug |
| itools-command | String | yes | itools | command to run |

It throws a `ConfigurationException` if itools-command is empty .

To run the security analysis in external mode, you need to run the itools command: `security-analysis` --external arguments

## Examples

### YAML
```yaml
external-security-analysis-config:
    debug: false
    itools-command: tools
```

### XML
```xml
<external-security-analysis-config>
    <debug>false</debug>
    <itools-command>tools</itools-command>
</external-security-analysis-config>
```

## References
See also:
[security-analysis](../../tools/security-analysis.md)