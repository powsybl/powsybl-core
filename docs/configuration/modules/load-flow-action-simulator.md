# Module load-flow-action-simulator

The `load-flow-action-simulator` module is used by the [action-simulator](../../tools/action-simulator.md) tool if it's
configured to use the `LoadFlowActionSimulator` implementation.

## Properties

| Property | Type | Required | Default value | Description |
| -------- | ---- | -------- | ------------- | ----------- |
| debug | Boolean | no | false | Not used |
| ignore-pre-contingency-violations | Boolean | false | If false, stop the simulation if there are still violations after the pre-contingency simulation |
| load-flow-factory | String | yes | - | The `LoadFlowFactory` implementation to use for the simulation |
| max-iterations | Integer | yes | - | The maximal number of iteration to solve the violations |

## Examples

### YAML
```yaml
load-flow-action-simulator:
    debug: false
    ignore-pre-contingency-violations: false
    load-flow-factory: com.powsybl.loadflow.mock.LoadFlowFactoryMock
    max-iterations: 10
```

### XML
```xml
<load-flow-action-simulator>
    <debug>false</debug>
    <ignore-pre-contingency-violations>false</ignore-pre-contingency-violations>
    <load-flow-factory>com.powsybl.loadflow.mock.LoadFlowFactoryMock</load-flow-factory>
    <max-iterations>10</max-iterations>
</load-flow-action-simulator>
```
