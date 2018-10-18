# IIDM - LCC Converter Station

The `com.powsybl.iidm.network.LccConverterStation` interface is used to model a LCC Converter Station. In IIDM, this is
a sub interface of [HVDC Converter Station](hvdcConverterStation.md).

## Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| PowerFactor | float | yes | - | The power factor |

## Examples
This example shows how to create a new LCC Converter Station in a network:
```java
LccConverterStation lcs = voltageLevel.newLccConverterStation()
    .setId('LCS')
    .setConnectableBus('B1')
    .setBus('B1')
    .setLossFactor(0.011f)
    .setPowerFactor(0.5f)
    .add();
```

## References
See also:
- [HVDC Converter Station](hvdcConverterStation.md)
- [HVDC Line](hvdcLine.md)
- [Voltage Level](voltageLevel.md)

