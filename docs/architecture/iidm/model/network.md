# IIDM - Network

The `com.powsybl.iidm.network.Network` interface is used to model a power grid.

The `Network` class contains [substations](substation.md), [AC lines](line.md), [HVDC lines](hvdcLine.md) and [tie lines](tieLine.md).

## Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| id | String | yes | - | The ID of the network |
| name | String | no | - | The name of the network |
| caseDate | `DateTime` | no | Now | The date of the the case |
| ForecastDistance | Integer | no | 0 | The number of minutes between the date of the case generation and the case date |

## Example
This example shows how to create a new `Network` object:
```java
Network network = NetworkFactory.create("network-ID", "test")
    .setCaseDate(DateTime.parse("2018-02-11T12:00:55+01:00"))
    .setForecastDistance(60);
```

## References
See also:
- [Substation](substation.md)
- [Line](line.md)
- [TieLine](tieLine.md)
- [HvdcLine](hvdcLine.md)
