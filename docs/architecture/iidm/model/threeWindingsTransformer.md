# IIDM - Three windings transformer

The `com.powsybl.iidm.network.ThreeWindingsTransformer` interface is used to model a three windings power transformer.
A three windings power transformer is connected to three voltage levels (side 1, side 2 and side 3) that belong to the same
substation:
- Side 1 is the primary side (high voltage)
- Side 2 and Side 3 can indifferently be the secondary side (medium voltage) or the tertiary side (low voltage)
A [Ratio Tap Changer](ratioTapChanger.md) can be associated to the side 2 or the side 3 of a three windings power transformer.

## Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| Leg1 | `ThreeWindingsTransformer.Leg1` | yes | - | The leg at the primary side |
| Leg2 | `ThreeWindingsTransformer.Leg2or3` | yes | - | The leg at the secondary side |
| Leg3 | `ThreeWindingsTransformer.Leg2or3` | yes | - | The leg at the tertiary side |

## Leg1
`ThreeWindingsTransformer.Leg1` is a nested interface used to model the primary side of a three windings power transformer.

### Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| Terminal | `Terminal` | yes | - | The terminal the leg is connected to |
| R | double | yes | - | The nominal series resistance specified at the voltage of the leg |
| X | double | yes | - | The nominal series reactance specified at the voltage of the leg |
| G | double | yes | - | The nominal magnetizing conductance specified at the voltage of the leg |
| B | double | yes | - | The nominal magnetizing susceptance specified at the voltage of the leg |
| RatedU | double | yes | - | The rated voltage |

## Leg2or3
`ThreeWindingsTransformer.Leg2or3` is a nested interface used to model the secondary or the tertiary side of a three windings
power transformer.

### Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| Terminal | `Terminal` | yes | - | The terminal the leg is connected to |
| R | double | yes | - | The nominal series resistance specified at the voltage of the leg |
| X | double | yes | - | The nominal series reactance specified at the voltage of the leg |
| RatedU | double | yes | - | The rated voltage |

## Examples
This is an example of how to create a new ThreeWindingsTransformer in the network:
```java
ThreeWindingsTransformer threeWindingsTransformer = substation.newThreeWindingsTransformer()
    .setId('TWT3')
    .newLeg1()
        .setVoltageLevel('VL1')
        .setNode(11)
        .setR(17.424)
        .setX(1.7424)
        .setG(0.00573921028466483)
        .setB(0.000573921028466483)
        .setRatedU(132.0)
        .add()
    .newLeg2()
        .setVoltageLevel('VL2')
        .setNode(22)
        .setR(1.089)
        .setX(0.1089)
        .setRatedU(33.0)
        .add()
    .newLeg3()
        .setVoltageLevel('VL3')
        .setNode(33)
        .setR(0.121)
        .setX(0.0121)
        .setRatedU(11.0)
        .add()
    .add();

```

## References
See also:
- [Substation](substation.md)
- [Terminal](terminal.md)