# Export

## Available exporters

According to one's needs, there are several AMPL exporters available, each with a version number.

At the moment, there are:

- The `BasicAmplExporter` (associated with the `AmplExportVersion` `V1_0`);
- The `ExtendedAmplExporter` (associated with the `AmplExportVersion` `V1_1`) that inherits from the `BasicAmplExporter`.
- The `ExtendedAmplExporterV2` (associated with the `AmplExportVersion` `V1_2`) that inherits from the `ExtendedAmplExporter`.
- The `ExtendedAmplExporterV3` (associated with the `AmplExportVersion` `V1_3`) that inherits from the `ExtendedAmplExporterV2`.

The default version is the `V1_3`.

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

- In the bus tables, a boolean indicating if the bus is a slack one and an integer identifying the synchronous component;
- `r`, `g` and `b` in tap tables as it is already done for `x`;
- The regulating bus id for generators and static VAR compensators that are in voltage regulation mode.

### The `ExtendedAmplExporterV2`

This exporter adds the following information to the `ExtendedAmplExporter`:

- In the generator tables, a boolean indicating if the generator is a condenser;
- In LCC converter station tables, the load target Q of the converter station;
- In HVDC line tables, the AC emulation parameters, along with a boolean to indicate whether AC emulation is active.

This exporter also corrects the unit of the load target Q (MVar) in the battery tables.

### The `ExtendedAmplExporterV3`

This exporter adds the following information to the `ExtendedAmplExporterV2` in the battery tables:

- A boolean indicating if the battery is regulating voltage;
- A double containing the targetV of the battery;
- An integer representing the bus where the regulating terminal is connected. A value of -1 means that the battery is not regulating or that its regulating terminal is not connected.


(ampl-export-options)=
## Options

These properties can be defined in the configuration file in the [import-export-parameters-default-value](../../user/configuration/import-export-parameters-default-value.md#import-export-parameters-default-value) module.

**iidm.export.ampl.export-ratio-tap-changer-voltage-target**<br>
The `iidm.export.ampl.export-ratio-tap-changer-voltage-target` property is an optional property that defines whether the AMPL exporter exports the ratio tap changer voltage setpoint or not. Its default value is `false`.

### Deprecated properties

**iidm.export.ampl.exportRatioTapChangerVoltageTarget**<br>
The `iidm.export.ampl.exportRatioTapChangerVoltageTarget` property is deprecated since V2.4.0. Use the `iidm.export.ampl.export-ratio-tap-changer-voltage-target` property instead.
