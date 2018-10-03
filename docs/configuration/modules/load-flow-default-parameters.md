# Module load-flow-default-parameters

The `load-flow-default-parameters` module is used everytime a load-flow is run. It defines the default values for the
most common parameters a LoadFlow implementation should be able to handle. 

## Properties

| Property | Type | Required | Default value | Description |
| -------- | ---- | -------- | ------------- | ----------- |
| noGeneratorReactiveLimits | Boolean | no | false | If true, the load-flow is allowed to find a setpoint outside the reactive limits |
| phaseShifterRegulationOn | Boolean | no | false | If true, the load-flow is allowed to change taps of PhaseTapChanger |
| specificCompatibility | Boolean | no | false | If true, the load-flow is run in a legacy mode (implementation specific) |
| transformerVoltageControlOn | Boolean | no | false | If true, the load-flow is allowed to change taps of RatioTapChanger |
| voltageInitMode | List of VoltageInitMode | no | UNIFORM_VALUES | The policy used by the load-flow to initialize the voltage values |

**voltageInitMode**: the available VoltageInitMode values are:
- UNIFORM_VALUES: v=1pu, theta=0
- PREVIOUS_VALUES: use previous computed value from the network
- DC_VALUES: preprocessing to compute DC angles

## Examples

### YAML
```yaml
load-flow-default-parameters:
    noGeneratorReactiveLimits: false
    phaseShifterRegulationOn: false
    specificCompatibility: false
    transformerVoltageControlOn: false
    voltageInitMode: UNIFORM_VALUES
```

### XML
```xml
<load-flow-default-parameters>
    <noGeneratorReactiveLimits>false</noGeneratorReactiveLimits>
    <phaseShifterRegulationOn>false</phaseShifterRegulationOn>
    <specificCompatibility>false</specificCompatibility>
    <transformerVoltageControlOn>false</transformerVoltageControlOn>
    <voltageInitMode>UNIFORM_VALUES</voltageInitMode>
</load-flow-default-parameters>
```
