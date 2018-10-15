# IIDM - Static VAR compensator

The `com.powsybl.iidm.network.StaticVarCompensator` interface is used to model a static VAR compensator.

## Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| Bmin | double | yes | - | The minimum susceptance |
| Bmax | double | yes | - | The maximum susceptance |
| VoltageSetpoint | double | no | - | The voltage setpoint |
| ReactivePowerSetpoint | double | no | - | The reactive power setpoint |
| RegulationMode | `RegulationMode` | yes | - | The regulation mode |

### RegulationMode
The `com.powsybl.iidm.network.StaticVarCompensator.RegulationMode` enum contains these three values:
- VOLTAGE
- REACTIVE_POWER
- OFF

### Setpoints
The voltage setpoint is required when the regulation mode is set to VOLTAGE.
The reactive power setpoint is required when the regulation mode is set to REACTIVE_POWER.

## Examples

This example shows how to create a new StaticVarCompensator in the network:
```java
StaticVarCompensator staticVarCompensator = network.getVoltageLevel('VL')
    .newStaticVarCompensator()
        .setId('SVC')
        .setBus('BUS1')
        .setConnectableBus('BUS1')
        .setBmin(0.0002)
        .setBmax(0.0008)
        .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
        .setVoltageSetPoint(390)
        .add();
```

## References
See also:
- [Injection](injection.md)