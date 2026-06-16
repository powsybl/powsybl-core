# Configuration

(sensitivity-implementation-config)=
## Implementation
If you have several implementations in your classpath, you need to choose which implementation to use in your configuration file:
```yaml
sensitivity-analysis:
  default-impl-name: "<IMPLEMENTATION_NAME>"
```

Each implementation is identified by its name, that should be unique in the classpath.
Use "OpenLoadFlow" to use PowSyBl OpenLoadFlow's sensitivity analysis implementation.

(sensitivity-generic-parameter)=
## Parameters

(param-sensi-flow-flow-sensitivity-value-threshold)=
### flow-flow-sensitivity-value-threshold

The `flow-flow-sensitivity-value-threshold` is the threshold under which sensitivity values having a variable type among
`INJECTION_ACTIVE_POWER`, `INJECTION_REACTIVE_POWER` and `HVDC_LINE_ACTIVE_POWER` and function type among
`BRANCH_ACTIVE_POWER_1/2/3`, `BRANCH_REACTIVE_POWER_1/2/3` and `BRANCH_CURRENT_1/2/3` will be filtered from the
analysis results.

The default value is `0.0`.

(param-sensi-voltage-voltage-sensitivity-value-threshold)=
### voltage-voltage-sensitivity-value-threshold

The `voltage-voltage-sensitivity-value-threshold` is the threshold under which sensitivity values having variable type
`BUS_TARGET_VOLTAGE` and function type `BUS_VOLTAGE` will be filtered from the analysis results.

The default value is `0.0`.

(param-sensi-flow-voltage-sensitivity-value-threshold)=
### flow-voltage-sensitivity-value-threshold

The `flow-voltage-sensitivity-value-threshold` is the threshold under which sensitivity values having a variable type among
`INJECTION_REACTIVE_POWER` and function type among `BUS_VOLTAGE`, or variable type among `BUS_TARGET_VOLTAGE` and function type among
`BRANCH_REACTIVE_POWER_1/2/3`, `BRANCH_CURRENT_1/2/3` or `BUS_REACTIVE_POWER` will be filtered from the analysis results.

The default value is `0.0`.

(param-sensi-angle-flow-sensitivity-value-threshold)=
### angle-flow-sensitivity-value-threshold

The `angle-flow-sensitivity-value-threshold` is the threshold under which sensitivity values having a variable type among
`TRANSFORMER_PHASE` and `TRANSFORMER_PHASE_1/2/3` and a function type among `BRANCH_ACTIVE_POWER_1/2/3`, `BRANCH_REACTIVE_POWER_1/2/3`
and `BRANCH_CURRENT_1/2/3` will be filtered from the analysis results.

The default value is `0.0`.

(param-sensi-operator-strategies-calculation-mode)=
### operator-strategies-calculation-mode

The `operator-strategies-calculation-mode` represents the calculation mode used for operator strategies sensitivities.

The possible values are:
- `NONE`: deactivate calculation of operator strategies sensitivities and only calculate N and N-K sensitivities
- `ALL_CONTINGENCIES`: calculate operator strategies for all contingencies in addition to N, N-K sensitivities
- `ONLY_OPERATOR_STRATEGIES`: only calculate operator strategies sensitivities and skip N and N-K sensitivities

The default value is `NONE`.

(param-sensi-debug-dir)=
### debug-dir
This property specifies the directory path where debug files will be dumped. If `null`, no file will be dumped.

The default value is `null`.


## Examples

**YAML configuration:**
```yaml
sensitivity-analysis-default-parameters:
  flow-flow-sensitivity-value-threshold: 0.2
  flow-voltage-sensitivity-value-threshold: 0.1
  voltage-voltage-sensitivity-value-threshold: 0.1
  angle-flow-sensitivity-value-threshold: 0.1
  sensitivity-operator-strategies-calculation-mode: ONLY_OPERATOR_STRATEGIES
  debug-dir: /tmp/debugDir
```

**XML configuration:**
```xml
<sensitivity-analysis-default-parameters>
  <flow-flow-sensitivity-value-threshold>0.2</flow-flow-sensitivity-value-threshold>
  <flow-voltage-sensitivity-value-threshold>0.1</flow-voltage-sensitivity-value-threshold>
  <voltage-voltage-sensitivity-value-threshold>0.1</voltage-voltage-sensitivity-value-threshold>
  <angle-flow-sensitivity-value-threshold>0.1</angle-flow-sensitivity-value-threshold>
  <sensitivity-operator-strategies-calculation-mode>ONLY_OPERATOR_STRATEGIES</sensitivity-operator-strategies-calculation-mode>
  <debug-dir>/tmp/debugDir</debug-dir>
</sensitivity-analysis-default-parameters>
```