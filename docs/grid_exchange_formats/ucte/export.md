# Export

<span style="color: red">TODO</span>

During the export of an IIDM network in the UCTE format, the network is first converted into an `UCTENetwork` which is 
then written in UCTE format.

## IIDM elements conversion

### Loads
Every bus in the network is exported as a UCTE Node. The active and reactive load of this UCTE Node is calculated from the
active and reactive power set points of the loads connected to the bus represented by the UCTE Node:
- the active load of the UCTE Node is the sum of the active power set points `P0` of the connected loads.
- the reactive load of the UCTE Node is the sum of the reactive power set points `Q0` of the connected loads.

## Options
These properties can be defined in the configuration file in the [import-export-parameters-default-value](../../user/configuration/import-export-parameters-default-value.md#import-export-parameters-default-value) module.

**ucte.export.naming-strategy**  
The `ucte.export.naming-strategy` property is an optional property that defines the naming strategy to be used for UCTE export.

Its default value is `Default`, which corresponds to an implementation that expects the network elements' ID to be totally compatible with UCTE-DEF norm (e.g., a network initially imported from a UCTE-DEF file), and throws an exception if any network element does not respect the norm. It does not do any ID modification.
