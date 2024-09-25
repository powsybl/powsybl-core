# Export

## Available exporters

According to one's needs, there are several AMPL exporters available, each with a version number.

At the moment, there are:

- The `BasicAmplExporter` (associated with the `AmplExportVersion` `V1_0`);
- The `ExtendedAmplExporter` (associated with the `AmplExportVersion` `V1_1`) that inherits from the `BasicAmplExporter`.

The default version is the `V1_1`.

Exporters define the information written in text files and fed to AMPL regarding:

- Buses;
- Tap changers;
- Branches;
- Current limits;
- Generators;
- Batteries;
- Loads;
- Shunts;
- Static VAR Compensators;
- Substations;
- VSC Converter stations;
- LCC Converter stations;
- HVDC lines.

### The `BasicAmplExporter`
This exporter is the "historical" version, the first that has been designed.

### The `ExtendedAmplExporter`

This exporter adds the following information to the `BasicAmplExporter`:

- In the bus tables, a boolean indicating if the bus is a slack one;
- `r`, `g` and `b` in tap tables as it is already done for `x`;
- The regulating bus id for generators and static VAR compensators that are in voltage regulation mode.


(ampl-export-options)=
## Options

These properties can be defined in the configuration file in the [import-export-parameters-default-value](../../user/configuration/import-export-parameters-default-value.md#import-export-parameters-default-value) module.

**iidm.export.ampl.export-ratio-tap-changer-voltage-target**  
The `iidm.export.ampl.export-ratio-tap-changer-voltage-target` property is an optional property that defines whether the AMPL exporter exports the ratio tap changer voltage setpoint or not. Its default value is `false`.

### Deprecated properties

**iidm.export.ampl.exportRatioTapChangerVoltageTarget**  
The `iidm.export.ampl.exportRatioTapChangerVoltageTarget` property is deprecated since V2.4.0. Use the `iidm.export.ampl.export-ratio-tap-changer-voltage-target` property instead.
