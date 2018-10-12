# IIDM - Dangling line

The `com.powsybl.iidm.network.DanglingLine` interface is used to model a dangling line. A dangling line is a component
that aggregates a line chunk and a constant power injection. The active and reactive power setpoints are fixed.

## Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| P0 | double | yes | - | The active power setpoint |
| Q0 | double | yes | - | The reactive power setpoint |
| R | double | yes | - | The series resistance |
| X | double | yes | - | The series reactance |
| G | double | yes | - | The series conductance |
| B | double | yes | - | The shunt susceptance |
| UcteXnodeCode | String | no | - | The dangling line's UCTE Xnode code |

### Electrical characteristics
R, X, G and B correspond to a percent of the original line and have to be consistent with the declared length of
the dangling line.

### UCTE Xnode Code
The UCTE Xnode code is defined in the case where the line is a boundary.

## Examples

This example shows how to create a new DanglingLine in the network:
```java
DanglingLine danglingLine = network.getVoltageLevel('VL').newDanglingLine()
    .setId('DL')
    .setBus('BUS1')
    .setConnectableBus('BUS1')
    .setP0(50.0)
    .setQ0(60.0)
    .setR(10.0)
    .setX(20.0)
    .setG(30.0)
    .setB(40.0)
    .setUcteXnodeCode('CODE')
    .add();
```

## References
See also:
- [Injection](injection.md)
