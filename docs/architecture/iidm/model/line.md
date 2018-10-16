# IIDM - Line

The `com.powsybl.iidm.network.Line` interface is used to model an AC line.
A line can also be a [Tie Line](tieLine.md).

## Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| R | double | yes | - | The series resistance |
| X | double | yes | - | The series reactance |
| G1 | double | yes | - | The first side shunt conductance |
| B1 | double | yes | - | The first side shunt susceptance |
| G2 | double | yes | - | The second side shunt conductance |
| B2 | double | yes | - | The second side shunt susceptance |

## Examples
This example shows how to create a new Line in the network:
```java
Line line = network.newLine()
    .setId('L')
    .setVoltageLevel1('VL1')
    .setVoltageLevel2('VL2')
    .setNode1(1)
    .setNode2(2)
    .setR(4.0)
    .setX(200.0)
    .setG1(0.0)
    .setB1(0.0)
    .setG2(0.0)
    .setB2(0.0)
    .add();
```

## References
See also:
- [Branch](branch.md)
