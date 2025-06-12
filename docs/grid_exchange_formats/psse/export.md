# Export

There are two main use-cases supported:
* Update.
* Full export.

The update option is only valid if the `psse.export.update` option is set to `true` and the case was previously imported using the same format. This option preserves the version of the initial case and the format if the case was imported using PSS®E version 35. The update creates a copy of the initial case and modifies it by updating the relevant values in each PSS®E data block.

The full export option creates a new PSS®E version 35 case from scratch, using only the information available in the PowSyBl model. By default, the case is exported in `raw` format, but the `rawx` format can be selected by setting the `psse.export.raw-format` option to `false`. The detailed connectivity is also exported, with each substation in the PowSyBl model corresponding to a substation in PSS®E.
A voltage level is exported as node-breaker when the topologyKind is `NODE_BREAKER`, there is at least one switch, and the number of buses within the voltage level is less than 999.

(psse-export-options)=
## Options
Parameters for the export can be defined in the configuration file in the [import-export-parameters-default-value](../../user/configuration/import-export-parameters-default-value.md#import-export-parameters-default-value) module.

**psse.export.update**  
The `psse.export.update` property is optional and defines whether the exporter should perform an update. The default value is `true`.

**psse.export.raw-format**  
The `psse.export.raw-format` property is optional and defines whether the exporter should use the `raw` format. The default value is `true`.
