# IIDM - HVDC line

The `com.powsybl.iidm.network.HvdcLine` interface is used to model a HVDC Line. A HVDC line is connected to two HVDC
converters on DC side.

## Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| R | double | yes | - | The resistance of the line |
| ConvertersMode | `ConvertersMode`| yes | - | The converter's mode |
| NominalV | double | yes | - | The nominal voltage |
| ActivePowerSetpoint | double | yes | - | The active power setpoint |
| MaxP | double | yes | - | The maximum active power |
| ConverterStationId1 | String | yes | - | The ID of the HVDC converter station connected on side 1 |
| ConverterStationId2 | String | yes | - | The ID of the HVDC converter station connected on side 2 |

### ConvertersMode
The `com.powsybl.iidm.network.HvdcLine.ConvertersMode` enum contains these two values:
- SIDE_1_RECTIFIER_SIDE_2_INVERTER,
- SIDE_1_INVERTER_SIDE_2_RECTIFIER

## Examples

This example shows how to create a new HvdcLine in the network:
```java
HvdcLine hvdcLine = network.newHvdcLine()
    .setId('HL')
    .setR(5.0)
    .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
    .setNominalV(440.0)
    .setMaxP(-50.0)
    .setActivePowerSetpoint(20.0)
    .setConverterStationId1("C1")
    .setConverterStationId2("C2")
    .add();
```
