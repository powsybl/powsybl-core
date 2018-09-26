# Module load-flow-based-phase-shifter-optimizer

The `load-flow-based-phase-shifter-optimizer` module is used by the `LoadFlowBasedPhaseShifterOptimizer` class which is
an implementation of the `PhaseShifterOptimizer` interface. The `LoadFlowBasedPhaseShifterOptimizer` try to solve a 
current violation on a phase tap changer.

## Properties

| Property | Type | Required | Default value | Description |
| -------- | ---- | -------- | ------------- | ----------- |
| load-flow-factory | String | yes | - | The `LoadFlowFactory` implementation to use for the optimization |

## Examples

### YAML
```yaml
load-flow-based-phase-shifter-optimizer:
    load-flow-factory: com.powsybl.loadflow.mock.LoadFlowFactoryMock
```

### XML
```xml
<load-flow-based-phase-shifter-optimizer>
    <load-flow-factory>com.powsybl.loadflow.mock.LoadFlowFactoryMock</load-flow-factory>
</load-flow-based-phase-shifter-optimizer>
```

## Reference
See also:
[phaseShifterOptimizerTap](../../architecture/ial/actions/phaseShifterOptimizerTap.md)
