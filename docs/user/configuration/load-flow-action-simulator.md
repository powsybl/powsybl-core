# load-flow-action-simulator
The `load-flow-action-simulator` module is used by the [action-simulator]() tool if it's configured to use the `LoadFlowActionSimulator` implementation.

## Properties

**copy-strategy**  
Use the `copy-strategy` to define how the action-simulator will store and restore network state internally. This choice can greatly impact performances. Possible values are:
- `STATE`: will only save and restore state data. Optimizes performances, but will not behave correctly if some actions modify the structure of the network.
- `DEEP`: will save and restore all network data. Decreases performance, but allows to use any type of action.

**ignore-pre-contingency-violations**  
Set the `ignore-pre-contingency-violations` to `true` to ignore the pre-contingency violations and continue the simulation even if there are still violations after the pre-contingency simulation.

**load-flow-name**  
The `load-flow-name` property is an optional property that defines the implementation name to use for running the load flow.
If this property is not set, the default load flow implementation is used. See [Loadflow Configuration](load-flow.md) to
configure the default load flow.

**max-iterations**  
Use the `max-iterations` parameter to limit the number of iterations needed to solve the violations.

## Examples

**YAML configuration:**
```yaml
load-flow-action-simulator:
    copy-strategy: STATE
    debug: false
    ignore-pre-contingency-violations: false
    load-flow-name: Mock
    max-iterations: 10
```

**XML configuration:**
```xml
<load-flow-action-simulator>
    <copy-strategy>STATE</copy-strategy>
    <debug>false</debug>
    <ignore-pre-contingency-violations>false</ignore-pre-contingency-violations>
    <load-flow-name>Mock</load-flow-name>
    <max-iterations>10</max-iterations>
</load-flow-action-simulator>
```
