# IIDM - Substation

The `com.powsybl.iidm.network.Substation` is used to model a substation. A substation is a collection of equipments
located in the same geographical site. It contains several [voltage levels](voltageLevel.md),
[two windings transformers](twoWindingsTransformer.md) and [three windings transformers](threeWindingsTransformer.md).

A substation is located in a single country and belongs to one TSO.

## Characteristics
| Attribute | Type | Required | Default value | Description |
| --------- | ---- | -------- | ------------- | ----------- |
| id | String | yes | - | The ID of the network |
| name | String | no | - | The name of the network |
| country | `Country` | yes | - | The country where this substation is located |
| tso | String | no | - | The TSO this substations belongs to |
| geographicalTags | List of String | no | - | A list of geographical tags |

## Example
This example shows how to create a new `Substation` object:
```java
Substation substation = network.newSubstation()
    .setId("id")
    .setName("name") // optional
    .setCountry(Country.US)
    .add();
```

## References
See also:
- [Network](network.md)
- [VoltageLevel](voltageLevel.md)
- [TwoWindingsTransformer](twoWindingsTransformer.md)
- [ThreeWindingsTransformer](threeWindingsTransformer.md)
