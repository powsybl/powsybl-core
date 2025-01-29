# Export

<span style="color: red">TODO</span>

## IIDM elements conversion

### Generators
Each bus in the network is exported as a UCTE node. The generation fields of this node are filled with the details of 
the generators connected to the associated bus:
- The node type code of the UCTE node is 2 (PU node) in the case of one generator regulating voltage connected to the bus, 
3 if the bus is the slack node and 0 (PQ node) by default.
- The voltage reference of the UCTE node is obtained from the `TargetV` of the regulating generators connected to the bus.
If multiple generators are regulating voltage with a different `TargetV`, then the `TargetV` that is the closest to the
`nominalV` of the `VoltageLevel` is kept. 
- The active power generation of the UCTE node is the sum of the `TargetP` of every connected generator that are not `NaN`.
- The reactive power generation of the UCTE node is the sum of the `TargetQ` of every connected generator that are not `NaN`.
- The minimum permissible generation in active power of the UCTE node is the sum of the `minP` of every connected generator 
that are not `NaN` or `-9999`. This field being optional in UCTE but compulsory in IIDM, during the import of a UCTE network, 
it is set to `-9999`. This value is thus ignored during export to stay consistent with the import.
- The maximum permissible generation in active power of the UCTE node is the sum of the `maxP` of every connected generator
that are not `NaN` or `9999`. This field being optional in UCTE but compulsory in IIDM, during the import of a UCTE network,
it is set to `9999`. This value is thus ignored during export to stay consistent with the import.
- The minimum permissible generation in reactive power of the UCTE node is obtained by summing the `minQ` of the reactive
limits of each generator at their `TargetP`. The `minQ` that are `NaN` or `-9999` are filtered out of the sum.
- The maximum permissible generation in reactive power of the UCTE node is is obtained by summing the `maxQ` of the reactive
limits of each generator at their `TargetP`. The `maxQ` that are `NaN` or `9999` are filtered out of the sum.
- The power plant type of the UCTE node depends on the power plant type of the connected generators. If they all have the
same type, then this type is used for the UCTE node. Otherwise, it is `F`.

## Options
These properties can be defined in the configuration file in the [import-export-parameters-default-value](../../user/configuration/import-export-parameters-default-value.md#import-export-parameters-default-value) module.

**ucte.export.naming-strategy**  
The `ucte.export.naming-strategy` property is an optional property that defines the naming strategy to be used for UCTE export.

Its default value is `Default`, which corresponds to an implementation that expects the network elements' ID to be totally compatible with UCTE-DEF norm (e.g., a network initially imported from a UCTE-DEF file), and throws an exception if any network element does not respect the norm. It does not do any ID modification.
