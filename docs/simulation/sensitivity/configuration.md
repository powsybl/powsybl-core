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

**flowFlowSensitivityValueThreshold**  

The `flowFlowSensitivityValueThreshold` is the threshold under which sensitivity values having a variable type among
`INJECTION_ACTIVE_POWER`, `INJECTION_REACTIVE_POWER` and `HVDC_LINE_ACTIVE_POWER` and function type among
`BRANCH_ACTIVE_POWER_1/2/3`, `BRANCH_REACTIVE_POWER_1/2/3` and `BRANCH_CURRENT_1/2/3` will be filtered from the
analysis results. The default value is 0.0.

**voltageVoltageSensitivityValueThreshold**  

The `voltageVoltageSensitivityValueThreshold` is the threshold under which sensitivity values having variable type
`BUS_TARGET_VOLTAGE` and function type `BUS_VOLTAGE` will be filtered from the analysis results. The default value is 0.0.

**flowVoltageSensitivityValueThreshold**  

The `flowVoltageSensitivityValueThreshold` is the threshold under which sensitivity values having a variable type among
`INJECTION_REACTIVE_POWER` and function type among `BUS_VOLTAGE`, or variable type among `BUS_TARGET_VOLTAGE` and function type among
`BRANCH_REACTIVE_POWER_1/2/3`, `BRANCH_CURRENT_1/2/3` or `BUS_REACTIVE_POWER` will be filtered from the analysis results. The default value is 0.0.

**angleFlowSensitivityValueThreshold**  

The `angleFlowSensitivityValueThreshold` is the threshold under which sensitivity values having a variable type among
`TRANSFORMER_PHASE` and `TRANSFORMER_PHASE_1/2/3` and a function type among `BRANCH_ACTIVE_POWER_1/2/3`, `BRANCH_REACTIVE_POWER_1/2/3`
and `BRANCH_CURRENT_1/2/3` will be filtered from the analysis results. The default value is 0.0.