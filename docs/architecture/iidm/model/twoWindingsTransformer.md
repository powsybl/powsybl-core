# IIDM - Two windings transformer

The `com.powsybl.iidm.network.TwoWindingsTransformer` interface is used to model a two windings power transformer.
A two windings power transformer is connected to two voltage levels (side 1 and side 2) that belong to a same substation.
A [Ratio Tap Changer](ratioTapChanger.md) or a [Phase Tap Changer](phaseTapChanger.md) can be associated to a two windings power transformer.

## Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| R | double | yes | - | The nominal series resistance |
| X | double | yes | - | The nominal series reactance |
| G | double | yes | - | The nominal magnetizing conductance |
| B | double | yes | - | The nominal magnetizing susceptance |
| RatedU1 | double | yes | - | The primary winding rated voltage |
| RatedU2 | double | yes | - | The secondary winding rated voltage |

B, G, R and X shall be specified at the secondary voltage side.

##Examples
This example shows how to create a TwoWindingsTransformer in the network:
TwoWindingsTransformer twoWindingsTransformer = substation.newTwoWindingsTransformer()
    .setId('TWT2')
    .setVoltageLevel1('VL1')
    .setVoltageLevel2('VL2')
    .setNode1(1)
    .setNode2(2)
    .setR(0.5)
    .setX(4)
    .setG(0)
    .setB(0)
    .setRatedU1(24)
    .setRatedU2(385)
    .add();
```

## References
See also:
- [Branch](branch.md)
- [Substation](substation.md)