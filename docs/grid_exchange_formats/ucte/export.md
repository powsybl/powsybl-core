# Export

PowSyBl supports the export of an IIDM network to the UCTE-DEF format. The exporter converts the network back to a UCTE-DEF file. It expects the network to be compatible with the UCTE-DEF naming convention: with the default naming strategy, the IDs of the network elements must comply with the norm (which is the case for a network initially imported from a UCTE-DEF file).

## Options
These properties can be defined in the configuration file in the [import-export-parameters-default-value](../../user/configuration/import-export-parameters-default-value.md#import-export-parameters-default-value) module.

**ucte.export.naming-strategy**<br>
The `ucte.export.naming-strategy` property is an optional property that defines the naming strategy to be used for UCTE export.

Its default value is `Default`, which corresponds to an implementation that expects the network elements' ID to be totally compatible with UCTE-DEF norm (e.g., a network initially imported from a UCTE-DEF file), and throws an exception if any network element does not respect the norm. It does not do any ID modification.

**ucte.export.combine-phase-angle-regulation**<br>
The `ucte.export.combine-phase-angle-regulation` property is an optional property that defines whether the phase and angle regulations of the two-winding transformers are combined when exporting. Its default value is `false`.
