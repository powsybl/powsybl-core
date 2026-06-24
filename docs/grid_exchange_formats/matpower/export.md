# Export

PowSyBl supports the export of an IIDM network to the [MATPOWER](https://matpower.org/) format. The exporter writes a MATPOWER case file, converting the network buses, loads, shunts, generators, branches and transformers to the MATPOWER model.

## Options
These properties can be defined in the configuration file in the [import-export-parameters-default-value](../../user/configuration/import-export-parameters-default-value.md) module.

| Property | Type | Default value | Description |
|----------|---------|---------------|-------------|
| `matpower.export.with-bus-names` | boolean | `false` | Export the bus names in addition to the bus numbers |
| `matpower.export.max-generator-active-power-limit` | double | `10000` | Maximum generator active power limit (MW) to export |
| `matpower.export.max-generator-reactive-power-limit` | double | `10000` | Maximum generator reactive power limit (MVar) to export |
