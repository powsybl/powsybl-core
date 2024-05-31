# Export

<span style="color: red">TODO</span>

## Options
These properties can be defined in the configuration file in the [import-export-parameters-default-value](../../user/configuration/import-export-parameters-default-value.md) module.

**ucte.export.naming-strategy**  
The `ucte.export.naming-strategy` property is an optional property that defines the naming strategy to be used for UCTE export.

Its default value is `Default`, which corresponds to an implementation that expects the network elements' ID to be totally compatible with UCTE-DEF norm (e.g. a network initially imported from a UCTE-DEF file), and throws an exception if any network element does not respect the norm. It does not do any ID modification.
