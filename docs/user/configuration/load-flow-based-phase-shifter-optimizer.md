# load-flow-based-phase-shifter-optimizer
The `load-flow-based-phase-shifter-optimizer` module is used by the `com.powsybl.action.util.LoadFlowBasedPhaseShifterOptimizer` class,
which is an implementation of the `com.powsybl.action.util.PhaseShifterOptimizer` interface. The `LoadFlowBasedPhaseShifterOptimizer`
tries to solve a current violation on a phase tap changer.

## Required properties

**load-flow-name**  
The `load-flow-name` property is an optional property that defines the implementation name to use for running the load flow.
If this property is not set, the default load flow implementation is used. See [Loadflow Configuration](load-flow.md) to configure the default load flow.

## Examples

**YAML configuration:**
```yaml
load-flow-based-phase-shifter-optimizer:
    load-flow-name: Mock
```

**XML configuration:**
```xml
<load-flow-based-phase-shifter-optimizer>
    <load-flow-name>Mock</load-flow-name>
</load-flow-based-phase-shifter-optimizer>
```
