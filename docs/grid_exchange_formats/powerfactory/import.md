# Import
PowerFactory files are available through PowerFactory application. The internal format is `.pfd`. To use and generate PowerFactory files you need to own a licence. You can follow instructions on the [official PowerFactory website](https://www.digsilent.de/en/powerfactory.html).

## Handle PowerFactory exported format cases
PowerFactory files are encrypted (internal format `.pfd`), data are only readable by the PowerFactory application.
PowSyBl core only supports PowerFactory DGS V5 and only the ASCII format (`.dgs` files) which are PowerFactory formatted files. They are created from a PowerFactory application project export.

A basic DGS export configuration is provided with the PowerFactory application in <PowerFactory install dir>\Dgs\V5.X\DGS Export Definitions 5.pfd. This file has to be imported in your database. However, this basic configuration does not provide all necessary objects and attributes to PowSyBl converter to achieve a correct conversion of the initial data model (and get a calculated state when running a load flow close to the one calculated by PowerFactory).
So you need to complete the configuration with those following steps.

Two and three winding transformer taps definition attributes need to be added to existing configuration. Optionally, voltage magnitude and angle could be added to terminals to compare the calculated state between PowerFactory and PowSyBl. VSC and common impedance configuration are totally missing and need to be created. Optionally, in order to get the same slack, the attribute `ip_ctrl` can be provided.

```
- 2-Winding Transformer.IntMon (class ElmTr2)
    - e:mTaps

- 3-Winding Transformer.IntMon (class ElmTr3)
    - e:mTaps
    - e:iMeasTap

- Terminal.IntMon (class ElmTerm)
    - m:u
    - m:phiu

- PWM Converter/2 DC-Connections.IntMon (class ElmVsc)
    - e:loc_name
    - e:fold_id
    - e:psetp
    - e:qsetp
    - e:usetp
    - e:Pnold
    - e:Unom
    - e:P_max

- Common Impedance.IntMon (class ElmZpu)
    - e:loc_name
    - e:outserv
    - e:Sn
    - e:nphshift
    - e:ag
    - e:fold_id
    - e:r_pu
    - e:x_pu
    - e:r_pu_ji
    - e:x_pu_ji
    - e:gi_pu
    - e:bi_pu
    - e:gj_pu
    - e:bj_pu

- Synchronous Machine.IntMon (class ElmSym)
    - e:ip_ctrl
```

## Handling HVDC data

### Selecting reduced or detailed model

By default, the DGS file importer tries to import HVDC components as _reduced_ point-to-point representations. Parameter `powerfactory.import.dgs.HVDC-import-detailed` must be set to `true` to activate import of _detailed_ DC subnetworks with multi-terminal possibility.

### Additional attributes (_detailed_)
For _detailed_ network import, the following additional attributes are also requested in the DGS file for the VSCs (`ElmVsc`)

| Attribute     | Meaning                                                         |
|---------------|-----------------------------------------------------------------|
| i_acdc        | Control mode selector.<br/> Values 3, 4, 5 and 6 are supported. |
| usetpdc       | Control voltage in p.u. for DC voltage control.                 |
| Unomdc        | Nominal DC voltage for DC voltage control.                      |
| swtLossFactor | Swicthing loss factor (otherwise default to zero).              |
| resLossFactor | Resistive loss factor (otherwise default to zero).              |

Ground elements (`ElmGndswt`) are not exported by default by PowerFactory. Their export to the DGS file must be declared specifically if grounds are to be re-imported by PowSyBl. No additional attribute is mandatory for ground elements (`ElmGndswt`). If `on_off` is present and has value zero, the ground element is considered disconnected and it is not added to the network. `ciEarthed` is disregarded by the importer. The switch itself is not imported to PowSyBl. The ground resistance is assumed to be zero.

`ElmTerm`, `ElmLne` and `TypLne` are used by the importer. Apart from the optional operational limits described below, they require no additional data than the default attributes.

### Operational limits (_detailed_)
The following attributes are optional. When present, they are imported as operational limits of the DC equipment:

| Object    | Attribute | Unit | IIDM limit                                         | Conversion                       |
|-----------|-----------|------|----------------------------------------------------|----------------------------------|
| `ElmVsc`  | `Pmax_uc` | MW   | `minP` of the `VoltageSourceConverter`             | `minP = -Pmax_uc`                |
| `ElmVsc`  | `Pmin_uc` | MW   | `maxP` of the `VoltageSourceConverter`             | `maxP = -Pmin_uc`                |
| `ElmLne`  | `Imaxlim` | kA   | `permanentCurrentLimit` (A) of the `DcLine`        | `1000 * Imaxlim * nlnum`         |
| `ElmTerm` | `vmin`    | p.u. | `lowVoltageLimit` (kV) of the `DcNode`             | `vmin * uknom`                   |
| `ElmTerm` | `vmax`    | p.u. | `highVoltageLimit` (kV) of the `DcNode`            | `vmax * uknom`                   |

As for the active power setpoint, the sign of the converter active power limits is changed, because PowerFactory and
IIDM use opposite sign conventions. `nlnum` is the number of parallel circuits of the line (1 if absent).

PowerFactory expresses the absence of a limit with values of magnitude at least 9999. The corresponding IIDM limit
then keeps its default value, as when the attribute is absent:
- `minP` keeps its default value if `Pmax_uc` >= 9999, and `maxP` keeps its default value
  if `Pmin_uc` <= -9999;
- `permanentCurrentLimit` is undefined if `Imaxlim` >= 9999.

A `vmin` equal to zero also means that there is no low voltage limit: `lowVoltageLimit` is then undefined.

The import fails with a `PowerFactoryException` in case of inconsistent limits: `Pmin_uc` > `Pmax_uc` when both
are defined, `Imaxlim` <= 0, `vmin` < 0, `vmax` <= 0, or `vmin` >= `vmax` when both are defined. It also fails if
the `nlnum` of a DC line is present and not strictly positive.

### Control mode (_detailed_)

The following type of control mode is setup for VSCs, depending on the value of `i_acdc`:

| `i_acdc` | Behavior  | ControlMode | voltageRegulatorOn |
|----------|:---------:|:------------|--------------------|
| 3        |  Vdc - Q  | V_DC        | false              |
| 4        |  P - Vac  | P_PCC       | true               |
| 5        |   P - Q   | P_PCC       | false              |
| 6        | Vdc - Vac | V_DC        | true               |

Values 0, 1, 2, 7 and 8 are not supported and will raise a PowerFactoryException during the import.

### Switches and breakers
ElmCoup are imported as DcSwitch if they are connected to 2 DC terminals. They are assumed to be initially closed
unless `on_off` is present and set to 0. Their _kinds_ are `DISCONNECTOR` by default, which may be overridden
to `BREAKER` if `aUsage` is set to `cbk`. Kind may be explicitly set to `DISCONNECTOR` by specifying
`aUsage = dct`. Other `aUsage` values are not supported.

When a `TypSwitch` is provided with `typ_id`, the resistance value `R_on` is taken into account. Other
properties of the switch are disregarded for the DC network.

### Limitations (_detailed_)
- The only supported AC-DC converters are VSCs in `ElmVsc`.
- For now PCC control is not taken into account and the VSC is connected to a single terminal.
- Attribute `ciEarthed` of `ElmTerm` is ignored by the importer.
- The permanent current limit of DC lines only comes from `Imaxlim`. The other attributes PowerFactory uses for
  line loading (`sline`, `InomAir`, `fline`, thermal ratings `IntThrating`) are ignored, so the IIDM limit can be
  higher than the loading reference shown in PowerFactory.
- The number of parallel converters (`nparnum`) is ignored: `Pmin_uc` and `Pmax_uc` are not scaled.
- Voltage limits of AC terminals are not imported.
- PowerFactory calculation options, such as _Consider Active Power Limits_, are not taken into account: active
  power limits are imported whatever the study settings.

## Importing GPS coordinates

If the import `powerfactory.import.geodata` option is set to `true`, the importer will include geodata for both substations and lines.

GPS coordinates for substations are stored in a `SubstationPosition` extension attached to the corresponding substation. Similarly, line coordinates are stored in a `LinePosition` extension attached to the corresponding line, represented as a list of coordinates points.

## Import PowerFactory internal format

A [powsybl-powerfactory-db-native](https://github.com/powsybl/powsybl-powerfactory-db-native) repository has been created to import a proprietary PowerFactory file (`.pfd`) directly via a C++ API but the import can be slower and is not maintained for now.
