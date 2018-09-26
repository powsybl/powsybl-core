# Module default-computation-manager

The `default-computation-manager` module is an optional module loaded when an `iTools` command starts to determine which
`ComputationManager` implementations should be used for short-time and long-time computations. If this module is not set,
the `LocalComputationManager` implementation is used.

## Properties

| Property | Type | Required | Default value | Description |
| -------- | ---- | -------- | ------------- | ----------- |
| long-time-execution-computation-manager-factory | Class | no | - | The class name of the `ComputationManagerFactory` implementation used for long-time computations. |
| short-time-execution-computation-manager-factory | Class | yes | - | The class name of the `ComputationManagerFactory` implementation used for short-time computations. |

**long-time-execution-computation-manager-factory**: if this property is not set, we use the same factory for both
short-time and long-time computations.

The choice of using the short-time or the long-time computation manager factory is done by the implementation of each
kind of computations (load-flow, security-analysis...).

## Examples

### YAML
```yaml
default-computation-manager:
    long-time-execution-computation-manager-factory: com.powsybl.computation.local.LocalComputationManagerFactory
    short-time-execution-computation-manager-factory: com.powsybl.computation.local.LocalComputationManagerFactory
```

### XML
```xml
<default-computation-manager>
    <long-time-execution-computation-manager-factory>com.powsybl.computation.local.LocalComputationManagerFactory</long-time-execution-computation-manager-factory>
    <short-time-execution-computation-manager-factory>com.powsybl.computation.local.LocalComputationManagerFactory</short-time-execution-computation-manager-factory>
</default-computation-manager>
```

## References
See also:
[computation-local](computation-local.md)
