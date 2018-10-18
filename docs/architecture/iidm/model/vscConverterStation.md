# IIDM - VSC Converter Station

The `com.powsybl.iidm.network.VscConverterStation` interface is used to model a VSC Converter Station. In IIDM, this is
a sub interface of [HVDC Converter Station](hvdcConverterStation.md).

## Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| VoltageRegulatorOn | boolean | yes | - | The voltage regulator status |
| VoltageSetpoint | double | no | - | The voltage setpoint |
| ReactivePowerSetpoint | double | no | - | The reactive power setpoint |

## Setpoints
The voltage setpoint is required if the voltage regulator is on.
The reactive power setpoint is required if the voltage regulator is off.

## Examples
This example shows how to create a new LCC Converter Station in a network:
```java
VscConverterStation vcs = voltageLevel.newVscConverterStation()
    .setId('VCS')
    .setConnectableBus('B1')
    .setBus('B1')
    .setLossFactor(0.011f)
    .setVoltageRegulatorOn(true)
    .setVoltageSetpoint(405.0)
    .add();
```

## References
See also:
- [HVDC Converter Station](hvdcConverterStation.md)
- [HVDC Line](hvdcLine.md)
- [Voltage Level](voltageLevel.md)