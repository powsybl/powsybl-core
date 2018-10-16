# IIDM - Tie lines

The `com.powsybl.iidm.network.TieLine` interface is used to model a Tie Line. In IIDM, it is a sub class of [Line](line.md).
A Tie Line is an AC line sharing power between two neighbouring regional grids.

## Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| HalfLine1 | `TieLine.HalfLine` | yes | - | The first half of the line characteristics |
| HalfLine2 | `TieLine.HalfLine` | yes | - | The second half of the line characteristics |
| UcteXnodeCode | String | no | - | The UCTE Xnode code corresponding to this line |

### Half Line
An Half Line has the following characteristics:
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| ID | String | yes | - | The ID of the Half Line |
| Name | String | no | - | The name of the Half Line |
| R | double | yes | - | The series resistance |
| X | double | yes | - | The series reactance |
| G1 | double | yes | - | The first side shunt conductance |
| B1 | double | yes | - | The first side shunt susceptance |
| G2 | double | yes | - | The second side shunt conductance |
| B2 | double | yes | - | The second side shunt susceptance |
| XnodeP | double | yes | - | The active power consumption |
| XnodeQ | double | yes | - | The reactive power consumption |

A Tie Line is composed of two Half Lines.

### UCTE Xnode Code
It is only required in the case where the line is a boundary.

## Examples
This example shows how to create a new Tie Line in the network:
```java
TieLine tieLine = network.newTieLine()
    .line1()
        .setId('L')
        .setR(4.0)
        .setX(200.0)
        .setG1(0.0)
        .setB1(0.0)
        .setG2(0.0)
        .setB2(0.0)
        .setXnodeP()
        .setXnodeQ()
    .line2()
        .setId('L')
        .setR(4.0)
        .setX(200.0)
        .setG1(0.0)
        .setB1(0.0)
        .setG2(0.0)
        .setB2(0.0)
        .setXnodeP()
        .setXnodeQ()
    .setUcteXnodeCode(ucteXnodeCode)
    .setVoltageLevel1('VL1')
    .setVoltageLevel2('VL2')
    .setNode1(1)
    .setNode2(2)
    .add();
```

## References
See also:
- [Branch](branch.md)
